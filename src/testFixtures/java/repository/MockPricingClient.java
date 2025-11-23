package repository;

import com.crypto.wallet.management.service.PricingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class MockPricingClient implements PricingService {
    private final Map<String, BigDecimal> currentPrices = Map.of(
        "BTC", new BigDecimal("94749.27"),
        "ETH", new BigDecimal("3700.02"),
        "ADA", new BigDecimal("0.35"),
        "SOL", new BigDecimal("165.00")
    );

    private final Map<String, BigDecimal> historicalPrices = Map.of(
        "BTC", new BigDecimal("94745.00"),
        "ETH", new BigDecimal("3700.19"),
        "ADA", new BigDecimal("0.30"),
        "SOL", new BigDecimal("140.00")
    );

    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        return Optional.ofNullable(currentPrices.get(symbol.toUpperCase()));
    }

    public Optional<BigDecimal> getHistoricalPrice(String symbol, LocalDate date) {
        return Optional.ofNullable(historicalPrices.get(symbol.toUpperCase()));
    }
}
