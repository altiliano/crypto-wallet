package com.crypto.wallet.management.service;

import com.crypto.wallet.management.dto.SimulationAsset;
import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.MockPricingClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WalletSimulationServiceTest {

    private WalletSimulationService simulationService;

    @BeforeEach
    void setUp() {
        PricingService pricingService = new MockPricingClient();
        simulationService = new WalletSimulationService(pricingService);
    }

    @Test
    void shouldSimulateWalletPerformanceWithCurrentPrices() {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("0.5"), new BigDecimal("35000")),
            new SimulationAsset("ETH", new BigDecimal("4.25"), new BigDecimal("15310.71"))
        );

        SimulationRequest request = new SimulationRequest(assets, null);

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        assertTrue(response.getTotal().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(response.getBestAsset());
        assertNotNull(response.getWorstAsset());
        assertNotNull(response.getBestPerformance());
        assertNotNull(response.getWorstPerformance());
    }

    @Test
    void shouldSimulateWalletPerformanceWithHistoricalPrices() {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("0.5"), new BigDecimal("35000")),
            new SimulationAsset("ETH", new BigDecimal("4.25"), new BigDecimal("15310.71"))
        );

        SimulationRequest request = new SimulationRequest(assets, LocalDate.of(2025, 1, 7));

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        assertTrue(response.getTotal().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(response.getBestAsset());
        assertNotNull(response.getWorstAsset());
    }

    @Test
    void shouldCalculateCorrectPerformancePercentages() {
        MockPricingClient pricingClient = new MockPricingClient();

        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("1"), new BigDecimal("70000")),
            new SimulationAsset("ETH", new BigDecimal("1"), new BigDecimal("3602.52"))
        );

        SimulationRequest request = new SimulationRequest(assets, null);

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        assertEquals("BTC", response.getBestAsset());
        assertEquals("ETH", response.getWorstAsset());

        BigDecimal btcCurrentPrice = pricingClient.getCurrentPrice("BTC").orElse(BigDecimal.ZERO);
        BigDecimal ethCurrentPrice = pricingClient.getCurrentPrice("ETH").orElse(BigDecimal.ZERO);

        BigDecimal expectedBtcTotal = btcCurrentPrice.multiply(new BigDecimal("1"));
        BigDecimal expectedEthTotal = ethCurrentPrice.multiply(new BigDecimal("1"));
        BigDecimal expectedTotal = expectedBtcTotal.add(expectedEthTotal);

        assertEquals(0, response.getTotal().compareTo(expectedTotal));

        BigDecimal expectedBtcPerformance = btcCurrentPrice.subtract(new BigDecimal("70000"))
                .divide(new BigDecimal("70000"), 6, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        assertEquals(0, response.getBestPerformance().compareTo(expectedBtcPerformance));
    }

    @Test
    void shouldHandleEmptyAssetsList() {
        SimulationRequest request = new SimulationRequest(List.of(), null);

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getTotal());
        assertNull(response.getBestAsset());
        assertNull(response.getWorstAsset());
        assertEquals(BigDecimal.ZERO, response.getBestPerformance());
        assertEquals(BigDecimal.ZERO, response.getWorstPerformance());
    }

    @Test
    void shouldHandleSingleAsset() {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("1"), new BigDecimal("70000"))
        );

        SimulationRequest request = new SimulationRequest(assets, null);

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        assertEquals("BTC", response.getBestAsset());
        assertEquals("BTC", response.getWorstAsset());
        assertEquals(response.getBestPerformance(), response.getWorstPerformance());
    }

    @Test
    void shouldHandleMultipleAssetsWithDifferentPerformances() {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("1"), new BigDecimal("70000")),
            new SimulationAsset("ETH", new BigDecimal("1"), new BigDecimal("3602.52")),
            new SimulationAsset("ADA", new BigDecimal("100"), new BigDecimal("30")),
            new SimulationAsset("SOL", new BigDecimal("1"), new BigDecimal("140"))
        );

        SimulationRequest request = new SimulationRequest(assets, null);

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        assertNotNull(response.getBestAsset());
        assertNotNull(response.getWorstAsset());
        assertTrue(response.getBestPerformance().compareTo(response.getWorstPerformance()) >= 0);
    }

    @Test
    void shouldUseHistoricalPricesWhenDateProvided() {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("1"), new BigDecimal("94749"))
        );

        SimulationRequest request = new SimulationRequest(assets, LocalDate.of(2025, 1, 1));

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        BigDecimal expectedTotal = new BigDecimal("100.00");
        assertEquals(0, response.getTotal().compareTo(expectedTotal));
    }

    @Test
    void shouldMatchRequirementsExamplePerformance() {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("0.5"), new BigDecimal("35000")),
            new SimulationAsset("ETH", new BigDecimal("4.25"), new BigDecimal("15310.71"))
        );

        SimulationRequest request = new SimulationRequest(assets, null);

        SimulationResponse response = simulationService.simulateWalletPerformance(request);

        assertNotNull(response);
        assertEquals("BTC", response.getBestAsset());
        assertEquals("ETH", response.getWorstAsset());

        assertTrue(response.getBestPerformance().compareTo(new BigDecimal("35")) > 0);
        assertTrue(response.getWorstPerformance().compareTo(new BigDecimal("2")) > 0);

        assertTrue(response.getTotal().compareTo(new BigDecimal("60000")) > 0);
    }
}
