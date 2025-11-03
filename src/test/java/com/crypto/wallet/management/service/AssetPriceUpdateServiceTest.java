package com.crypto.wallet.management.service;

import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.repository.entities.Asset;
import com.crypto.wallet.management.repository.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.InMemoryAssetRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetPriceUpdateServiceTest {

    private final AssetRepository assetRepository = new InMemoryAssetRepository();
    private final AssetPriceUpdateService assetPriceUpdateService = new AssetPriceUpdateService(assetRepository);

    @BeforeEach
    public void setUp() {
        Wallet testWallet = new Wallet("concurrency-test@example.com");

        Asset btcAsset1 = Asset.builder()
                .symbol("BTC")
                .quantity(new BigDecimal("1.0"))
                .price(new BigDecimal("50000"))
                .value(new BigDecimal("50000"))
                .wallet(testWallet)
                .build();

        Asset btcAsset2 = Asset.builder()
                .symbol("BTC")
                .quantity(new BigDecimal("0.5"))
                .price(new BigDecimal("50000"))
                .value(new BigDecimal("25000"))
                .wallet(testWallet)
                .build();

        Asset ethAsset = Asset.builder()
                .symbol("ETH")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("3000"))
                .value(new BigDecimal("30000"))
                .wallet(testWallet)
                .build();

        assetRepository.saveAll(List.of(btcAsset1, btcAsset2, ethAsset));
    }

    @Test
    public void testSingleSymbolPriceUpdate() {
        Map<String, String> prices = new HashMap<>();
        prices.put("BTC", "55000.00");

        assetPriceUpdateService.updateAssetPrices(prices);


        List<Asset> btcAssets = assetRepository.findBySymbol("BTC");
        assertThat(btcAssets).hasSize(2);
        assertThat(btcAssets.get(0).getPrice()).isEqualByComparingTo("55000.00");
        assertThat(btcAssets.get(1).getPrice()).isEqualByComparingTo("55000.00");
    }

    @Test
    public void canUpdateMoreTheOneSymbol() {
        Map<String, String> prices = new HashMap<>();
        prices.put("BTC", "60000.00");
        prices.put("ETH", "3500.00");

        assetPriceUpdateService.updateAssetPrices(prices);


        List<Asset> btcAssets = assetRepository.findBySymbol("BTC");
        assertThat(btcAssets).allMatch(asset -> asset.getPrice().compareTo(new BigDecimal("60000.00")) == 0);

        List<Asset> ethAssets = assetRepository.findBySymbol("ETH");
        assertThat(ethAssets).hasSize(1);
        assertThat(ethAssets.getFirst().getPrice()).isEqualByComparingTo("3500.00");
        assertThat(ethAssets.getFirst().getValue()).isEqualByComparingTo("35000.00");
    }

    @Test
    public void OneInvalidPriceShouldNotBlockOthersToBeUpdated() {
        Map<String, String> prices = new HashMap<>();
        prices.put("BTC", "invalid-price");
        prices.put("ETH", "3500.00");

        assetPriceUpdateService.updateAssetPrices(prices);


        List<Asset> ethAssets = assetRepository.findBySymbol("ETH");
        assertThat(ethAssets.getFirst().getPrice()).isEqualByComparingTo("3500.00");

        List<Asset> btcAssets = assetRepository.findBySymbol("BTC");
        assertThat(btcAssets).allMatch(asset -> asset.getPrice().compareTo(new BigDecimal("50000")) == 0);
    }

    @Test
    public void testHasAssetsForSymbol() {
        assertThat(assetPriceUpdateService.hasAssetsForSymbol("BTC")).isTrue();
        assertThat(assetPriceUpdateService.hasAssetsForSymbol("ETH")).isTrue();
        assertThat(assetPriceUpdateService.hasAssetsForSymbol("NONEXISTENT")).isFalse();
    }
}
