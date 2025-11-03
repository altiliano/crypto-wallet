package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.SimulationAsset;
import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import com.crypto.wallet.management.service.WalletSimulationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerSimulationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletSimulationService walletSimulationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSimulateWalletProfit() throws Exception {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("0.5"), new BigDecimal("35000")),
            new SimulationAsset("ETH", new BigDecimal("4.25"), new BigDecimal("15310.71"))
        );

        SimulationRequest request = new SimulationRequest(assets, null);

        SimulationResponse expectedResponse = new SimulationResponse(
            new BigDecimal("63097.33"),
            "BTC",
            new BigDecimal("35.35"),
            "ETH",
            new BigDecimal("2.70")
        );

        when(walletSimulationService.simulateWalletPerformance(any(SimulationRequest.class)))
            .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/wallets/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(63097.33))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.bestPerformance").value(35.35))
                .andExpect(jsonPath("$.worstAsset").value("ETH"))
                .andExpect(jsonPath("$.worstPerformance").value(2.70));
    }

    @Test
    void shouldSimulateWalletProfitWithHistoricalDate() throws Exception {
        List<SimulationAsset> assets = List.of(
            new SimulationAsset("BTC", new BigDecimal("1"), new BigDecimal("70000"))
        );

        SimulationRequest request = new SimulationRequest(assets, LocalDate.of(2025, 1, 7));

        SimulationResponse expectedResponse = new SimulationResponse(
            new BigDecimal("70000.00"),
            "BTC",
            new BigDecimal("0.00"),
            "BTC",
            new BigDecimal("0.00")
        );

        when(walletSimulationService.simulateWalletPerformance(any(SimulationRequest.class)))
            .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/wallets/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(70000.00))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.worstAsset").value("BTC"));
    }

    @Test
    void shouldHandleEmptyAssetsList() throws Exception {
        SimulationRequest request = new SimulationRequest(List.of(), null);

        SimulationResponse expectedResponse = new SimulationResponse(
            BigDecimal.ZERO,
            null,
            BigDecimal.ZERO,
            null,
            BigDecimal.ZERO
        );

        when(walletSimulationService.simulateWalletPerformance(any(SimulationRequest.class)))
            .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/wallets/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.bestAsset").doesNotExist())
                .andExpect(jsonPath("$.worstAsset").doesNotExist());
    }
}
