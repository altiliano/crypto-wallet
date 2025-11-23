package com.crypto.wallet.management.service;

import com.crypto.wallet.management.PriceAssets;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PricingService {
    Optional<BigDecimal> getCurrentPrice(String symbol);
    Optional<BigDecimal> getHistoricalPrice(String symbol, LocalDate date);
    List<PriceAssets> getPrices(List<String> symbols);
}
