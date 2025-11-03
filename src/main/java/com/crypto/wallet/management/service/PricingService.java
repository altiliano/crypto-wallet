package com.crypto.wallet.management.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface PricingService {
    Optional<BigDecimal> getCurrentPrice(String symbol);
    Optional<BigDecimal> getHistoricalPrice(String symbol, LocalDate date);
}
