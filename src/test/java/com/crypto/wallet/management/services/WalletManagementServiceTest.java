package com.crypto.wallet.management.services;

import com.crypto.wallet.management.PriceAssets;
import com.crypto.wallet.management.service.CoinCapPricingService;
import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.mapper.AssetMapper;
import com.crypto.wallet.management.mapper.WalletMapper;
import com.crypto.wallet.management.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.InMemoryWalletRepository;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WalletManagementServiceTest {
    private WalletManagementService walletManagementService;

    @Mock
    private CoinCapPricingService coinCapPricingService;

    private final WalletRepository walletRepository = new InMemoryWalletRepository();
    private final WalletMapper walletMapper = WalletMapper.INSTANCE;
    private final AssetMapper assetMapper = AssetMapper.INSTANCE;

    @BeforeEach
    void setUp() {
        walletManagementService = new WalletManagementServiceImpl(walletRepository, walletMapper, assetMapper, coinCapPricingService);
    }

    @Test
    void createWallet() {
        String email = "test@example.com";
        WalletDto wallet = walletManagementService.create(email);

        assertNotNull(wallet, "Wallet should not be null");
        assertEquals(email, wallet.getEmail(), "Wallet email should match input");
        assertTrue(wallet.getAssets().isEmpty(), "Assets list should be empty");
    }

    @Test
    void addAssetToWallet() {
        String symbol = "BTC";
        String apiPrice = "0.613999999999999990";
        BigDecimal userPrice = new BigDecimal("50000.00");
        getTheSymbolPrice(apiPrice, symbol);

        String email = "test2@example.com";
        walletManagementService.create(email);
        AssetDto asset = AssetDto.builder()
                .symbol(symbol)
                .quantity(new BigDecimal("1.50"))
                .price(userPrice)
                .build();

        var walletDto = walletManagementService.addAsset(email, asset);

        assertNotNull(walletDto.getAssets(), "Assets list should not be null");
        assertEquals(1, walletDto.getAssets().size(), "Wallet should have one asset");
        AssetDto addedAsset = walletDto.getAssets().getFirst();
        assertEquals(symbol, addedAsset.getSymbol(), "Asset symbol should match");
        assertEquals(new BigDecimal("1.50"), addedAsset.getQuantity(), "Asset quantity should match");
        assertEquals(userPrice, addedAsset.getPrice(), "Should keep user-provided price");
        assertEquals(new BigDecimal("75000.00"), addedAsset.getValue(), "Value should be quantity × price");
    }


    @Test
    void showWalletInformation() {
        String btcSymbol = "BTC";
        String btcSymbolPrice = "0.613999999999999990";
        String ethSymbol = "ETH";
        String ethSymbolPrice = "1.613999999999999990";
        String email = "wallet@example.com";
        walletManagementService.create(email);

        AssetDto btc = AssetDto.builder()
                .symbol(btcSymbol)
                .quantity(new BigDecimal("1.5"))
                .price(new BigDecimal("100000.00"))
                .value(new BigDecimal("150000.00"))
                .build();
        AssetDto eth = AssetDto.builder()
                .symbol(ethSymbol)
                .quantity(new BigDecimal("2.00"))
                .price(new BigDecimal("4000.00"))
                .value(new BigDecimal("8000.00"))
                .build();

        getTheSymbolPrice(btcSymbolPrice, btcSymbol);
        walletManagementService.addAsset(email, btc);
        getTheSymbolPrice(ethSymbolPrice, ethSymbol);
        walletManagementService.addAsset(email, eth);

        WalletDto result = walletManagementService.getWallet(email);
        assertNotNull(result, "Wallet should not be null");
        assertNotNull(result.getId(), "Wallet should not be null");
        assertEquals(2, result.getAssets().size(), "Wallet should have two assets");
        assertEquals(new BigDecimal("158000.00"), result.getTotal(), "Total should be the sum of asset values");

        AssetDto resultBtc = result.getAssets().stream().filter(a -> a.getSymbol().equals("BTC")).findFirst().orElse(null);
        AssetDto resultEth = result.getAssets().stream().filter(a -> a.getSymbol().equals("ETH")).findFirst().orElse(null);
        assertNotNull(resultBtc, "BTC asset should be present");
        assertNotNull(resultEth, "ETH asset should be present");
        assertEquals(new BigDecimal("1.5"), resultBtc.getQuantity());
        assertEquals(new BigDecimal("100000.00"), resultBtc.getPrice());  // User-provided price should be kept
        assertEquals(new BigDecimal("150000.00"), resultBtc.getValue());
        assertEquals(new BigDecimal("2.00"), resultEth.getQuantity());
        assertEquals(new BigDecimal("4000.00"), resultEth.getPrice());    // User-provided price should be kept
        assertEquals(new BigDecimal("8000.00"), resultEth.getValue());
    }

    @Test
    void dontAddAssetIfPriceNotFoundOnPricingApi() {
        getTheSymbolPrice(null, "INVALID");

        String email = "test@example.com";
        walletManagementService.create(email);

        AssetDto invalidAsset = AssetDto.builder()
                .symbol("INVALID")
                .quantity(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(100))
                .build();

        // Should throw exception when symbol is not found in pricing API
        assertThrows(IllegalArgumentException.class, () -> {
            walletManagementService.addAsset(email, invalidAsset);
        }, "Should throw exception when symbol price is not found");

        verify(coinCapPricingService, times(1)).getPriceBySymbol("INVALID");
    }

    @Test
    void throwExceptionWhenWalletNotFound() {
        AssetDto asset = AssetDto.builder()
                .symbol("BTC")
                .quantity(BigDecimal.valueOf(1.0))
                .price(BigDecimal.valueOf(50000.00))
                .build();

        // Should throw exception when wallet doesn't exist
        assertThrows(IllegalArgumentException.class, () -> {
            walletManagementService.addAsset("nonexistent@example.com", asset);
        }, "Should throw exception when wallet is not found");
    }

    private void getTheSymbolPrice(String symbolPrice, String symbol) {
        PriceAssets priceResponse = PriceAssets.builder()
                .timestamp(System.currentTimeMillis())
                .data(Collections.singletonList(symbolPrice))
                .build();

        when(coinCapPricingService.getPriceBySymbol(symbol)).thenReturn(priceResponse);
    }
}
