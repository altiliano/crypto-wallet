package com.crypto.wallet.management.repository;

import com.crypto.wallet.management.repository.entities.Asset;
import com.crypto.wallet.management.repository.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AssetRepositoryContractTest {
    protected AssetRepository assetRepository;
    private Asset testAsset1;
    private Asset testAsset2;
    private Asset testAsset3;

    protected abstract AssetRepository createAssetRepository();

    @BeforeEach
    void setUp() {
        assetRepository = createAssetRepository();

        Wallet wallet = createTestWallet("test@example.com");


        testAsset1 = createTestAsset("BTC", new BigDecimal("50000.00"), new BigDecimal("0.1"), wallet);
        testAsset2 = createTestAsset("ETH", new BigDecimal("3000.00"), new BigDecimal("2.5"), wallet);
        testAsset3 = createTestAsset("BTC", new BigDecimal("51000.00"), new BigDecimal("0.05"), wallet);
    }

    @Test
    void shouldSaveAndRetrieveAsset() {
        Asset savedAsset = assetRepository.save(testAsset1);

        Optional<Asset> retrievedAsset = assetRepository.findById(savedAsset.getId());
        assertThat(retrievedAsset).isPresent();
        assertThat(retrievedAsset.get().getSymbol()).isEqualTo("BTC");
        assertThat(retrievedAsset.get().getPrice()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(retrievedAsset.get().getQuantity()).isEqualByComparingTo(new BigDecimal("0.1"));

    }

    @Test
    void shouldFindAssetsBySymbol() {
        assetRepository.save(testAsset1);
        assetRepository.save(testAsset2);
        assetRepository.save(testAsset3);

        List<Asset> btcAssets = assetRepository.findBySymbol("BTC");
        List<Asset> ethAssets = assetRepository.findBySymbol("ETH");
        List<Asset> adaAssets = assetRepository.findBySymbol("ADA");

        assertThat(btcAssets).hasSize(2);
        assertThat(btcAssets).allMatch(asset -> "BTC".equals(asset.getSymbol()));

        assertThat(ethAssets).hasSize(1);
        assertThat(ethAssets.getFirst().getSymbol()).isEqualTo("ETH");

        assertThat(adaAssets).isEmpty();
    }

    @Test
    void shouldReturnAllAssets() {
        assetRepository.save(testAsset1);
        assetRepository.save(testAsset2);
        assetRepository.save(testAsset3);

        List<Asset> allAssets = assetRepository.findAll();

        assertThat(allAssets).hasSize(3);
    }


    @Test
    void shouldUpdateAsset() {
        Asset savedAsset = assetRepository.save(testAsset1);

        BigDecimal newPrice = new BigDecimal("52000.00");
        savedAsset.setPrice(newPrice);
        BigDecimal newQuantity = new BigDecimal("0.2");
        savedAsset.setQuantity(newQuantity);
        Asset updatedAsset = assetRepository.save(savedAsset);

        assertThat(updatedAsset.getPrice()).isEqualByComparingTo(newPrice);
        assertThat(updatedAsset.getQuantity()).isEqualByComparingTo(newQuantity);

    }

    @Test
    void shouldReturnEmptyListWhenNoAssetsFound() {
        List<Asset> assets = assetRepository.findBySymbol("NONEXISTENT");

        assertThat(assets).isEmpty();
    }



    // Helper methods to create test objects
    protected Asset createTestAsset(String symbol, BigDecimal price, BigDecimal quantity, Wallet wallet) {
        Asset asset = new Asset();
        asset.setSymbol(symbol);
        asset.setPrice(price);
        asset.setQuantity(quantity);
        asset.setWallet(wallet);
        return asset;
    }

    protected Wallet createTestWallet(String email) {
        Wallet wallet = new Wallet();
        wallet.setEmail(email);
        return wallet;
    }
}
