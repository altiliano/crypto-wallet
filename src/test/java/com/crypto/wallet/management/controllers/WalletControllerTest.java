package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletManagementService walletManagementService;

    @MockitoBean
    private WalletSimulationService walletSimulationService;

    @Test
    void createWallet_ShouldReturnCreatedWallet() throws Exception {
        String email = "test@example.com";
        CreateWalletRequest request = new CreateWalletRequest();
        request.setEmail(email);

        WalletDto expectedWallet = new WalletDto("1", email, BigDecimal.ZERO, List.of());
        when(walletManagementService.create(email)).thenReturn(expectedWallet);


        mockMvc.perform(post("/api/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.assets").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void createWallet_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setEmail("");


        mockMvc.perform(post("/api/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void simulateWalletProfit_ShouldReturnSimulationResponse() throws Exception {
        SimulationAsset asset1 = new SimulationAsset("BTC", new BigDecimal("1.0"), new BigDecimal("50000"));
        SimulationAsset asset2 = new SimulationAsset("ETH", new BigDecimal("10.0"), new BigDecimal("3000"));

        SimulationRequest request = new SimulationRequest();
        request.setAssets(List.of(asset1, asset2));
        request.setDate(LocalDate.of(2024, 1, 1));

        SimulationResponse expectedResponse = new SimulationResponse();

        when(walletSimulationService.simulateWalletPerformance(any(SimulationRequest.class)))
                .thenReturn(expectedResponse);


        mockMvc.perform(post("/api/wallets/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addAssetToWallet_ShouldReturnUpdatedWallet() throws Exception {

        String email = "test@example.com";
        AssetDto request = new AssetDto("BTC", new BigDecimal("1.5"), new BigDecimal("50000"), new BigDecimal("75000"));

        AssetDto assetDto = new AssetDto("BTC", new BigDecimal("1.5"), new BigDecimal("50000"), new BigDecimal("75000"));
        WalletDto updatedWallet = new WalletDto("1", email, new BigDecimal("75000"), List.of(assetDto));

        when(walletManagementService.addAsset(eq(email), any(AssetDto.class)))
                .thenReturn(updatedWallet);


        mockMvc.perform(post("/api/wallets/{email}/assets", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.assets").hasJsonPath())
                .andExpect(jsonPath("$.assets[0].symbol").value("BTC"))
                .andExpect(jsonPath("$.assets[0].quantity").value(1.5))
                .andExpect(jsonPath("$.assets[0].price").value(50000))
                .andExpect(jsonPath("$.assets[0].value").value(75000))
                .andExpect(jsonPath("$.total").value(75000));
    }

    @Test
    void addAssetToWallet_WithInvalidSymbol_ShouldReturnBadRequest() throws Exception {
        String email = "test@example.com";
        AssetDto request = new AssetDto("", new BigDecimal("1.5"), new BigDecimal("50000"), null); // Invalid empty symbol

        mockMvc.perform(post("/api/wallets/{email}/assets", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssetToWallet_WithZeroQuantity_ShouldReturnBadRequest() throws Exception {
        String email = "test@example.com";
        AssetDto request = new AssetDto("BTC", BigDecimal.ZERO, new BigDecimal("50000"), null); // Invalid zero quantity

        mockMvc.perform(post("/api/wallets/{email}/assets", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssetToWallet_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        String email = "test@example.com";
        AssetDto request = new AssetDto("BTC", new BigDecimal("1.5"), new BigDecimal("-50000"), null); // Invalid negative price


        mockMvc.perform(post("/api/wallets/{email}/assets", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssetToWallet_WithNullValues_ShouldReturnBadRequest() throws Exception {
        String email = "test@example.com";
        AssetDto request = new AssetDto("BTC", null, null, null); // Invalid null values


        mockMvc.perform(post("/api/wallets/{email}/assets", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
