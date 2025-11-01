package com.crypto.wallet.management.services;

import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.dto.WalletDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletManagementServiceTest {
    private WalletManagementService walletManagementService;

    @BeforeEach
    void setUp() {
        walletManagementService = new WalletManagementServiceImpl();
    }

    @Test
    void createWalletForGivenEmail() {
        String email = "test@example.com";

        WalletDto wallet = walletManagementService.create(email);

        assertNotNull(wallet, "Wallet should not be null");
        assertEquals(email, wallet.getEmail(), "Wallet email should match input");
        assertTrue(wallet.getAssets().isEmpty(), "Assets list should be empty");
    }

    @Test
    void addAssetToWallet_addsAssetSuccessfully() {
        String email = "test2@example.com";
        walletManagementService.create(email);
        AssetDto asset = AssetDto.builder()
                .symbol("BTC")
                .quantity(BigDecimal.valueOf(1.5))
                .price(BigDecimal.TEN)
                .value(BigDecimal.ZERO)
                .build();

        var walletDto = walletManagementService.addAsset(email, asset);

        assertNotNull(walletDto.getAssets(), "Assets list should not be null");
        assertEquals(1, walletDto.getAssets().size(), "Wallet should have one asset");
        AssetDto addedAsset = walletDto.getAssets().get(0);
        assertEquals("BTC", addedAsset.getSymbol(), "Asset symbol should match");
        assertEquals(1.5, addedAsset.getQuantity().doubleValue(), 0.0001, "Asset quantity should match");
    }

    @Test
    void showWalletInformation() {
        String email = "wallet@example.com";
        WalletDto wallet = walletManagementService.create(email);
        wallet.setId("123");

        AssetDto btc = AssetDto.builder()
                .symbol("BTC")
                .quantity(BigDecimal.valueOf(1.5))
                .price(BigDecimal.valueOf(100000.00))
                .value(BigDecimal.valueOf(150000.00))
                .build();
        AssetDto eth = AssetDto.builder()
                .symbol("ETH")
                .quantity(BigDecimal.valueOf(2))
                .price(BigDecimal.valueOf(4000.00))
                .value(BigDecimal.valueOf(8000.00))
                .build();

        walletManagementService.addAsset(email, btc);
        walletManagementService.addAsset(email, eth);

        WalletDto result = walletManagementService.getWallet(email);
        assertNotNull(result, "Wallet should not be null");
        assertEquals("123", result.getId(), "Wallet id should match");
        assertEquals(2, result.getAssets().size(), "Wallet should have two assets");
        assertEquals(new BigDecimal("158000.00"), result.getTotal(), "Total should be the sum of asset values");

        AssetDto resultBtc = result.getAssets().stream().filter(a -> a.getSymbol().equals("BTC")).findFirst().orElse(null);
        AssetDto resultEth = result.getAssets().stream().filter(a -> a.getSymbol().equals("ETH")).findFirst().orElse(null);
        assertNotNull(resultBtc, "BTC asset should be present");
        assertNotNull(resultEth, "ETH asset should be present");
        assertEquals(BigDecimal.valueOf(1.5), resultBtc.getQuantity());
        assertEquals(BigDecimal.valueOf(100000.00), resultBtc.getPrice());
        assertEquals(BigDecimal.valueOf(150000.00), resultBtc.getValue());
        assertEquals(BigDecimal.valueOf(2), resultEth.getQuantity());
        assertEquals(BigDecimal.valueOf(4000.00), resultEth.getPrice());
        assertEquals(BigDecimal.valueOf(8000.00), resultEth.getValue());
    }
}
