package com.crypto.wallet.management;

import com.crypto.wallet.management.service.CoinCapPricingService;
import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.service.AssetPriceUpdateService;
import com.crypto.wallet.management.service.PricingService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class PricingScheduledJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(PricingScheduledJob.class);
    private static final int MAX_THREADS = 3;

    private final PricingService coinCapPricingService;
    private final AssetRepository assetRepository;
    private final AssetPriceUpdateService assetPriceUpdateService;

    public PricingScheduledJob(PricingService coinCapPricingService,
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
                List<List<String>> batches = partitionSymbols(uniqueSymbols, 100);
                logger.info("Partitioned {} symbols into {} batches of up to 100 symbols each",
                        uniqueSymbols.size(), batches.size());


                List<CompletableFuture<Map<String, String>>> batchFutures = batches.stream()
                        .map(batch -> CompletableFuture.supplyAsync(() ->
                            processBatch(batch), executorService))
                        .toList();


                CompletableFuture<Void> allBatches = CompletableFuture.allOf(
                        batchFutures.toArray(new CompletableFuture[0])
                );

                allBatches.join();


                Map<String, String> symbolPrices = batchFutures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(map -> map.entrySet().stream())
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


    private List<List<String>> partitionSymbols(List<String> symbols, int batchSize) {
        List<List<String>> batches = new java.util.ArrayList<>();
        for (int i = 0; i < symbols.size(); i += batchSize) {
            batches.add(symbols.subList(i, Math.min(i + batchSize, symbols.size())));
        }
        return batches;
    }


    private Map<String, String> processBatch(List<String> symbols) {
        try {
            logger.debug("Fetching prices for batch of {} symbols", symbols.size());

            List<PriceAssets> priceResponses = coinCapPricingService.getPrices(symbols);

            if (priceResponses == null || priceResponses.isEmpty() || priceResponses.get(0) == null) {
                logger.warn("No price data received for batch of {} symbols", symbols.size());
                return Map.of();
            }


            List<String> prices = priceResponses.get(0).getData();

            int maxIndex = Math.min(symbols.size(), prices.size());
            Map<String, String> result = IntStream.range(0, maxIndex)
                    .mapToObj(index -> {
                        String symbol = symbols.get(index);
                        String price = prices.get(index);
                        if (price != null && !price.isEmpty()) {
                            logger.debug("Retrieved price for {}: {}", symbol, price);
                            return Map.entry(symbol, price);
                        } else {
                            logger.warn("No price available for symbol: {}", symbol);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            logger.info("Successfully fetched prices for {}/{} symbols in batch", result.size(), symbols.size());
            return result;

        } catch (Exception e) {
            logger.error("Error fetching prices for batch of {} symbols, attempting individual processing", symbols.size(), e);
            // Fall back to individual processing to avoid one symbol blocking the entire batch
            return processBatchIndividually(symbols);
        }
    }

    private Map<String, String> processBatchIndividually(List<String> symbols) {
        logger.info("Processing {} symbols individually", symbols.size());
        Map<String, String> result = new java.util.HashMap<>();

        for (String symbol : symbols) {
            try {
                List<PriceAssets> priceResponses = coinCapPricingService.getPrices(List.of(symbol));

                if (priceResponses != null && !priceResponses.isEmpty() &&
                    priceResponses.get(0) != null && priceResponses.get(0).getData() != null &&
                    !priceResponses.get(0).getData().isEmpty()) {

                    String price = priceResponses.get(0).getData().get(0);
                    if (price != null && !price.isEmpty()) {
                        result.put(symbol, price);
                        logger.debug("Successfully fetched price for {}: {}", symbol, price);
                    } else {
                        logger.warn("No price available for symbol: {}", symbol);
                    }
                } else {
                    logger.warn("No price data received for symbol: {}", symbol);
                }
            } catch (Exception e) {
                logger.error("Failed to fetch price for symbol: {}", symbol, e);
                // Continue processing other symbols
            }
        }

        logger.info("Successfully fetched prices for {}/{} symbols individually", result.size(), symbols.size());
        return result;
    }
}
