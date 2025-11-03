package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.CreateWalletRequest;
import com.crypto.wallet.management.dto.SimulationAsset;
import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.service.WalletSimulationService;
import com.crypto.wallet.management.services.WalletManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletSimulationService walletSimulationService;

    @MockitoBean
    private WalletManagementService walletManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void createWallet() throws Exception {
        // Given
        CreateWalletRequest request = new CreateWalletRequest("test@example.com");
        WalletDto expectedWallet = WalletDto.builder()
                .id("123")
                .email("test@example.com")
                .total(new BigDecimal("0"))
                .assets(List.of())
                .build();

        when(walletManagementService.create("test@example.com")).thenReturn(expectedWallet);

        // When & Then
        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.assets").isArray())
                .andExpect(jsonPath("$.assets.length()").value(0));
    }

    @Test
    void createWallet_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateWalletRequest request = new CreateWalletRequest("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWallet_WithBlankEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateWalletRequest request = new CreateWalletRequest("");

        // When & Then
        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void simulateWalletProfit() throws Exception {
        // Given
        SimulationAsset asset1 = new SimulationAsset("BTC", new BigDecimal("1.0"), new BigDecimal("50000"));
        SimulationAsset asset2 = new SimulationAsset("ETH", new BigDecimal("10.0"), new BigDecimal("3000"));

        SimulationRequest request = new SimulationRequest(
                List.of(asset1, asset2),
                LocalDate.of(2023, 1, 1)
        );

        SimulationResponse expectedResponse = new SimulationResponse(
                new BigDecimal("80000.00"),
                "BTC",
                new BigDecimal("25.5"),
                "ETH",
                new BigDecimal("15.2")
        );

        when(walletSimulationService.simulateWalletPerformance(any(SimulationRequest.class)))
                .thenReturn(expectedResponse);


        mockMvc.perform(post("/api/wallets/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(closeTo(80000.00, 0.01)))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.bestPerformance").value(closeTo(25.5, 0.01)))
                .andExpect(jsonPath("$.worstAsset").value("ETH"))
                .andExpect(jsonPath("$.worstPerformance").value(closeTo(15.2, 0.01)));
    }

    @Test
    void simulateWalletProfit_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        SimulationRequest invalidRequest = new SimulationRequest(null, LocalDate.of(2023, 1, 1));

        mockMvc.perform(post("/api/wallets/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void simulateWalletProfit_WithEmptyAssets_ShouldReturnOk() throws Exception {
        SimulationRequest request = new SimulationRequest(List.of(), LocalDate.of(2023, 1, 1));

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

    @Test
    void simulateWalletProfit_WithNullDate_ShouldDefaultToToday() throws Exception {
        // Given
        SimulationAsset asset = new SimulationAsset("BTC", new BigDecimal("1.0"), new BigDecimal("50000"));
        SimulationRequest requestWithNullDate = new SimulationRequest(List.of(asset), null);

        SimulationResponse expectedResponse = new SimulationResponse(
                new BigDecimal("55000.00"),
                "BTC",
                new BigDecimal("10.0"),
                "BTC",
                new BigDecimal("10.0")
        );

        when(walletSimulationService.simulateWalletPerformance(any(SimulationRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/wallets/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNullDate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(closeTo(55000.00, 0.01)))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.bestPerformance").value(closeTo(10.0, 0.01)))
                .andExpect(jsonPath("$.worstAsset").value("BTC"))
                .andExpect(jsonPath("$.worstPerformance").value(closeTo(10.0, 0.01)));
    }

    @Test
    void simulateWalletProfit_WithSpecificDate_ShouldUseProvidedDate() throws Exception {
        SimulationAsset asset = new SimulationAsset("BTC", new BigDecimal("1.0"), new BigDecimal("50000"));
        LocalDate specificDate = LocalDate.of(2023, 6, 15);
        SimulationRequest requestWithSpecificDate = new SimulationRequest(List.of(asset), specificDate);

        SimulationResponse expectedResponse = new SimulationResponse(
                new BigDecimal("60000.00"),
                "BTC",
                new BigDecimal("20.0"),
                "BTC",
                new BigDecimal("20.0")
        );

        when(walletSimulationService.simulateWalletPerformance(any(SimulationRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/wallets/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithSpecificDate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(closeTo(60000.00, 0.01)))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.bestPerformance").value(closeTo(20.0, 0.01)))
                .andExpect(jsonPath("$.worstAsset").value("BTC"))
                .andExpect(jsonPath("$.worstPerformance").value(closeTo(20.0, 0.01)));
    }

    @Test
    void simulateWalletProfit_WithMultipleAssetsAndNullDate() throws Exception {
        // Given - test with different asset combination from the simulation test
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

        // When & Then
        mockMvc.perform(post("/api/wallets/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(closeTo(63097.33, 0.01)))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.bestPerformance").value(closeTo(35.35, 0.01)))
                .andExpect(jsonPath("$.worstAsset").value("ETH"))
                .andExpect(jsonPath("$.worstPerformance").value(closeTo(2.70, 0.01)));
    }

    @Test
    void simulateWalletProfit_WithFutureDate() throws Exception {
        // Given - test with future date from the simulation test
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

        // When & Then
        mockMvc.perform(post("/api/wallets/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(closeTo(70000.00, 0.01)))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.worstAsset").value("BTC"));
    }

}

