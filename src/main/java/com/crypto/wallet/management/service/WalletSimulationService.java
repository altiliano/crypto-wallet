package com.crypto.wallet.management.service;

import com.crypto.wallet.management.dto.SimulationAsset;
import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class WalletSimulationService {

    private final PricingService pricingService;

    @Autowired(required = false)
    public WalletSimulationService(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    public SimulationResponse simulateWalletPerformance(SimulationRequest request) {
        LocalDate targetDate = request.getDate() != null ? request.getDate() : LocalDate.now();

        List<AssetPerformance> performances = new ArrayList<>();
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (SimulationAsset asset : request.getAssets()) {
            Optional<BigDecimal> currentPrice = targetDate.equals(LocalDate.now())
                ? pricingService.getCurrentPrice(asset.getSymbol())
                : pricingService.getHistoricalPrice(asset.getSymbol(), targetDate);

            if (currentPrice.isPresent()) {
                BigDecimal currentValue = currentPrice.get().multiply(asset.getQuantity());
                BigDecimal performancePercentage = calculatePerformance(asset.getValue(), currentValue);

                performances.add(new AssetPerformance(asset.getSymbol(), performancePercentage));
                totalCurrentValue = totalCurrentValue.add(currentValue);
            }
        }

        return buildSimulationResponse(totalCurrentValue, performances);
    }

    private BigDecimal calculatePerformance(BigDecimal originalValue, BigDecimal currentValue) {
        return currentValue.subtract(originalValue)
                .divide(originalValue, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private SimulationResponse buildSimulationResponse(BigDecimal total, List<AssetPerformance> performances) {
        if (performances.isEmpty()) {
            return new SimulationResponse(total, null, BigDecimal.ZERO, null, BigDecimal.ZERO);
        }

        Optional<AssetPerformance> best = performances.stream()
                .max(Comparator.comparing(AssetPerformance::getPerformance));

        Optional<AssetPerformance> worst = performances.stream()
                .min(Comparator.comparing(AssetPerformance::getPerformance));

        return new SimulationResponse(
                total,
                best.map(AssetPerformance::getSymbol).orElse(null),
                best.map(AssetPerformance::getPerformance).orElse(BigDecimal.ZERO),
                worst.map(AssetPerformance::getSymbol).orElse(null),
                worst.map(AssetPerformance::getPerformance).orElse(BigDecimal.ZERO)
        );
    }

    private static class AssetPerformance {
        private final String symbol;
        private final BigDecimal performance;

        public AssetPerformance(String symbol, BigDecimal performance) {
            this.symbol = symbol;
            this.performance = performance;
        }

        public String getSymbol() {
            return symbol;
        }

        public BigDecimal getPerformance() {
            return performance;
        }
    }
}
