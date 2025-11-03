package com.crypto.wallet.management;

import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.repository.WalletRepository;
import com.crypto.wallet.management.repository.entities.Asset;
import com.crypto.wallet.management.repository.entities.Wallet;
import com.crypto.wallet.management.service.CoinCapPricingService;
import com.crypto.wallet.management.mapper.WalletMapper;
import com.crypto.wallet.management.mapper.AssetMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PricingScheduledJobPerformanceTest {

    @Autowired
    private AssetRepository assetRepository;

    @MockitoBean
    private CoinCapPricingService coinCapPricingService;

    @Autowired
    private WalletRepository walletRepository;

    @MockitoBean
    private WalletMapper walletMapper;

    @MockitoBean
    private AssetMapper assetMapper;

    @Test
    public void testOptimizedSymbolRetrieval() {
        Wallet testWallet = new Wallet("test@example.com");
        // Save the wallet first to avoid TransientObjectException
        testWallet = walletRepository.save(testWallet);

        List<Asset> assets = new ArrayList<>();


        String[] symbols = {"BTC", "ETH", "ADA"};

        for (int i = 0; i < 1000; i++) {
            String symbol = symbols[i % 3];
            Asset asset = Asset.builder()
                    .symbol(symbol)
                    .quantity(new BigDecimal("1.0"))
                    .price(new BigDecimal("1000"))
                    .value(new BigDecimal("1000"))
                    .wallet(testWallet)
                    .build();
            assets.add(asset);
        }


        assetRepository.saveAll(assets);


        long startTime = System.currentTimeMillis();
        List<String> distinctSymbols = assetRepository.findDistinctSymbols();
        long endTime = System.currentTimeMillis();


        assertThat(distinctSymbols).hasSize(3);
        assertThat(distinctSymbols).containsExactlyInAnyOrder("BTC", "ETH", "ADA");

        System.out.println("Optimized query took: " + (endTime - startTime) + "ms");
        System.out.println("Returned " + distinctSymbols.size() + " unique symbols from " + assets.size() + " assets");


        startTime = System.currentTimeMillis();
        List<Asset> allAssets = assetRepository.findAll();
        List<String> symbolsFromStream = allAssets.stream()
                .map(Asset::getSymbol)
                .distinct()
                .toList();
        endTime = System.currentTimeMillis();

        System.out.println("Inefficient approach took: " + (endTime - startTime) + "ms");
        System.out.println("Loaded " + allAssets.size() + " assets to get " + symbolsFromStream.size() + " unique symbols");

        assetRepository.deleteAll();
    }
}
