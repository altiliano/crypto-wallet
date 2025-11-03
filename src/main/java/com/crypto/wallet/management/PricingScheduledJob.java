package com.crypto.wallet.management;

import com.crypto.wallet.management.service.CoinCapPricingService;
import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.service.AssetPriceUpdateService;
import org.jetbrains.annotations.NotNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class PricingScheduledJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(PricingScheduledJob.class);
    private static final int MAX_THREADS = 3;

    private final CoinCapPricingService coinCapPricingService;
    private final AssetRepository assetRepository;
    private final AssetPriceUpdateService assetPriceUpdateService;

    public PricingScheduledJob(CoinCapPricingService coinCapPricingService,
                               AssetRepository assetRepository,
                               AssetPriceUpdateService assetPriceUpdateService) {
        this.coinCapPricingService = coinCapPricingService;
        this.assetRepository = assetRepository;
        this.assetPriceUpdateService = assetPriceUpdateService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.info("Executing scheduled pricing job");

            List<String> uniqueSymbols = assetRepository.findDistinctSymbols();

            logger.info("Found {} unique symbols to update: {}", uniqueSymbols.size(), uniqueSymbols);

            if (uniqueSymbols.isEmpty()) {
                logger.info("No assets found to update");
                return;
            }

            ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

            try {
                List<CompletableFuture<Map.Entry<String, String>>> priceFutures = uniqueSymbols.stream()
                        .map(symbol -> CompletableFuture.supplyAsync(() -> {
                            try {
                                logger.debug("Fetching price for symbol: {}", symbol);
                                PriceAssets priceResponse = coinCapPricingService.getPriceBySymbol(symbol);

                                if (priceResponse.getData() != null &&
                                    !priceResponse.getData().isEmpty() &&
                                    priceResponse.getData().getFirst() != null) {

                                    String priceStr = priceResponse.getData().getFirst();
                                    logger.debug("Retrieved price for {}: {}", symbol, priceStr);
                                    return Map.entry(symbol, priceStr);
                                } else {
                                    logger.warn("No price data available for symbol: {}", symbol);
                                    return null;
                                }
                            } catch (Exception e) {
                                logger.error("Error fetching price for symbol: {}", symbol, e);
                                return null;
                            }
                        }, executorService))
                        .toList();


                CompletableFuture<Void> allPrices = waitToFetchPriceToCompleted(priceFutures);

                allPrices.join();

                Map<String, String> symbolPrices = priceFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(entry -> entry != null && entry.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                logger.info("Successfully fetched prices for {} symbols", symbolPrices.size());

                assetPriceUpdateService.updateAssetPrices(symbolPrices);

                logger.info("Pricing job completed successfully");

            } finally {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

        } catch (Exception e) {
            logger.error("Error executing pricing job", e);
            throw new JobExecutionException(e);
        }
    }

    @NotNull
    private static CompletableFuture<Void> waitToFetchPriceToCompleted(List<CompletableFuture<Map.Entry<String, String>>> priceFutures) {
        return CompletableFuture.allOf(
                priceFutures.toArray(new CompletableFuture[0])
        );
    }


}
