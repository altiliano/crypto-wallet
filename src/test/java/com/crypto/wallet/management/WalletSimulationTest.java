package com.crypto.wallet.management;

import com.crypto.wallet.management.dto.SimulationAsset;
import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import com.crypto.wallet.management.service.WalletSimulationService;
import org.junit.jupiter.api.Test;
import repository.MockPricingClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WalletSimulationTest {

    @Test
    void shouldCompleteWalletSimulationEndToEnd() {
        SimulationResponse response = getSimulationResponse();

        assertNotNull(response);
        assertTrue(response.getTotal().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("BTC", response.getBestAsset());
        assertEquals("ETH", response.getWorstAsset());

        BigDecimal expectedBtcValue = new BigDecimal("94749.00").multiply(new BigDecimal("0.5"));
        BigDecimal expectedEthValue = new BigDecimal("3696.00").multiply(new BigDecimal("4.25"));
        BigDecimal expectedTotal = expectedBtcValue.add(expectedEthValue);

        assertEquals(1, response.getTotal().compareTo(expectedTotal));

    }

    private static SimulationResponse getSimulationResponse() {
        MockPricingClient pricingClient = new MockPricingClient();
        WalletSimulationService simulationService = new WalletSimulationService(pricingClient);

        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("0.5"), new BigDecimal("35000")),
            new SimulationAsset("ETH", new BigDecimal("4.25"), new BigDecimal("15310.71"))
        );

        SimulationRequest request = new SimulationRequest(assets, LocalDate.EPOCH);

        return simulationService.simulateWalletPerformance(request);
    }

    @Test
    void shouldDemonstrateExampleFromRequirements() {
        SimulationResponse response = getSimulationResponse();
        assertNotNull(response);
        assertEquals("BTC", response.getBestAsset());
        assertEquals("ETH", response.getWorstAsset());
        assertEquals(new BigDecimal("35.35"),response.getBestPerformance());
        assertEquals(new BigDecimal("2.71"),response.getWorstPerformance());

    }
}
