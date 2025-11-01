package com.crypto.wallet.management.services;

import com.crypto.wallet.management.dto.WalletDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
