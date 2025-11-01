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
}
