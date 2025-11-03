package com.crypto.wallet.management.service;

import com.crypto.wallet.management.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AssetPriceUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(AssetPriceUpdateService.class);

    private final AssetRepository assetRepository;

    public AssetPriceUpdateService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }


    @Transactional
    public void updateAssetPrices(Map<String, String> symbolPrices) {
        int totalUpdated = 0;

        for (Map.Entry<String, String> entry : symbolPrices.entrySet()) {
            String symbol = entry.getKey();
            String priceStr = entry.getValue();

            try {
                BigDecimal newPrice = new BigDecimal(priceStr);
                logger.info("Updating price for symbol {} to {}", symbol, newPrice);

                int updatedCount = assetRepository.updatePriceBySymbol(symbol, newPrice);

                if (updatedCount == 0) {
                    logger.warn("No assets found for symbol: {}", symbol);
                }

                totalUpdated += updatedCount;

                logger.info("Updated {} assets for symbol: {} to price: {}", updatedCount, symbol, newPrice);
            } catch (NumberFormatException e) {
                logger.error("Invalid price format for symbol {}: {}", symbol, priceStr, e);
            } catch (Exception e) {
                logger.error("Failed to update price for symbol {}", symbol, e);
            }
        }

        logger.info("Total updated {} assets with new prices", totalUpdated);
    }


    @Transactional(readOnly = true)
    public boolean hasAssetsForSymbol(String symbol) {
        return !assetRepository.findBySymbol(symbol).isEmpty();
    }
}
