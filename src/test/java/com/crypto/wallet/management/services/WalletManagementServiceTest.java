package com.crypto.wallet.management.services;

import com.crypto.wallet.management.PriceAssets;
import com.crypto.wallet.management.PricingApiClient;
import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.dto.WalletDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WalletManagementServiceTest {
    private WalletManagementService walletManagementService;

    @Mock
    private PricingApiClient pricingApiClient;

    @BeforeEach
    void setUp() {
        walletManagementService = new WalletManagementServiceImpl(pricingApiClient);
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
        String symbol = "BTC";
        String symbolPrice = "0.613999999999999990";
        getTheSymbolPrice(symbolPrice, symbol);

        String email = "test2@example.com";
        walletManagementService.create(email);
        AssetDto asset = AssetDto.builder()
                .symbol(symbol)
                .quantity(BigDecimal.valueOf(1.5))
                .value(BigDecimal.ZERO)
                .build();

        var walletDto = walletManagementService.addAsset(email, asset);

        assertNotNull(walletDto.getAssets(), "Assets list should not be null");
        assertEquals(1, walletDto.getAssets().size(), "Wallet should have one asset");
        AssetDto addedAsset = walletDto.getAssets().getFirst();
        assertEquals(symbol, addedAsset.getSymbol(), "Asset symbol should match");
        assertEquals(1.5, addedAsset.getQuantity().doubleValue(), 0.0001, "Asset quantity should match");
        assertEquals(new BigDecimal(symbolPrice), addedAsset.getPrice(), "Asset symbol should match");
    }


    @Test
    void showWalletInformation() {
        String btcSymbol = "BTC";
        String btcSymbolPrice = "0.613999999999999990";
        String ethSymbol = "ETH";
        String ethSymbolPrice = "1.613999999999999990";
        String email = "wallet@example.com";
        WalletDto wallet = walletManagementService.create(email);
        wallet.setId("123");

        AssetDto btc = AssetDto.builder()
                .symbol(btcSymbol)
                .quantity(BigDecimal.valueOf(1.5))
                .price(BigDecimal.valueOf(100000.00))
                .value(BigDecimal.valueOf(150000.00))
                .build();
        AssetDto eth = AssetDto.builder()
                .symbol(ethSymbol)
                .quantity(BigDecimal.valueOf(2))
                .price(BigDecimal.valueOf(4000.00))
                .value(BigDecimal.valueOf(8000.00))
                .build();

        getTheSymbolPrice(btcSymbolPrice, btcSymbol);
        walletManagementService.addAsset(email, btc);
        getTheSymbolPrice(ethSymbolPrice, ethSymbol);
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
        assertEquals(new BigDecimal(btcSymbolPrice), resultBtc.getPrice());
        assertEquals(BigDecimal.valueOf(150000.00), resultBtc.getValue());
        assertEquals(BigDecimal.valueOf(2), resultEth.getQuantity());
        assertEquals(new BigDecimal(ethSymbolPrice), resultEth.getPrice());
        assertEquals(BigDecimal.valueOf(8000.00), resultEth.getValue());
    }

    @Test
    void addAssetToWallet_shouldNotSaveAssetWhenSymbolNotFoundInPricingApi() {

        getTheSymbolPrice(null, "INVALID");


        String email = "test@example.com";
        walletManagementService.create(email);

        AssetDto invalidAsset = AssetDto.builder()
                .symbol("INVALID")
                .quantity(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(100))
                .value(BigDecimal.ZERO)
                .build();



        WalletDto wallet = walletManagementService.addAsset(email, invalidAsset);


        assertTrue(wallet.getAssets().isEmpty(), "Asset should not be added when symbol is invalid");
        verify(pricingApiClient, times(1)).getPriceBySymbol("INVALID");
    }

    private void getTheSymbolPrice(String symbolPrice, String symbol) {
        PriceAssets priceResponse = PriceAssets.builder()
                .timestamp(System.currentTimeMillis())
                .data(Collections.singletonList(symbolPrice))
                .build();

        when(pricingApiClient.getPriceBySymbol(symbol)).thenReturn(priceResponse);
    }
}
