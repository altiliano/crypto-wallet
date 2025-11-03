package com.cryptowallet.job;

import com.crypto.wallet.management.ManagementApplication;
import com.crypto.wallet.management.PricingScheduledJob;
import com.crypto.wallet.management.PricingApiClient;
import com.crypto.wallet.management.PriceAssets;
import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.repository.entities.Asset;
import com.crypto.wallet.management.repository.entities.Wallet;
import com.crypto.wallet.management.service.AssetCacheService;
import com.crypto.wallet.management.service.AssetPriceUpdateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import repository.InMemoryAssetRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ManagementApplication.class)
public class PricingScheduledJobTest {

    @MockitoBean
    private PricingApiClient pricingApiClient;

    @Autowired
    private Scheduler scheduler;

    private AssetRepository assetRepository;
    private PricingScheduledJob pricingScheduledJob;

    @BeforeEach
    public void setUp() {
        assetRepository = new InMemoryAssetRepository();
        AssetPriceUpdateService assetPriceUpdateService = new AssetPriceUpdateService(assetRepository);
        pricingScheduledJob = new PricingScheduledJob(pricingApiClient, assetRepository, assetPriceUpdateService);

        assetRepository.deleteAll();

        Wallet testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setEmail("test@example.com");

        Asset btcAsset1 = Asset.builder()
                .symbol("BTC")
                .quantity(new BigDecimal("1.5"))
                .price(new BigDecimal("50000"))
                .value(new BigDecimal("75000"))
                .wallet(testWallet)
                .build();

        Asset ethAsset = Asset.builder()
                .symbol("ETH")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("3000"))
                .value(new BigDecimal("30000"))
                .wallet(testWallet)
                .build();

        Asset btcAsset2 = Asset.builder()
                .symbol("BTC")
                .quantity(new BigDecimal("0.5"))
                .price(new BigDecimal("50000"))
                .value(new BigDecimal("25000"))
                .wallet(testWallet)
                .build();

        List<Asset> testAssets = Arrays.asList(btcAsset1, ethAsset, btcAsset2);

        assetRepository.saveAll(testAssets);
    }

    @Test
    public void testPricingJobExecutesSuccessfully() throws JobExecutionException {
        PriceAssets mockBtcPrice = PriceAssets.builder()
                .timestamp(System.currentTimeMillis())
                .data(List.of("55000.50"))
                .build();

        PriceAssets mockEthPrice = PriceAssets.builder()
                .timestamp(System.currentTimeMillis())
                .data(List.of("3200.75"))
                .build();

        when(pricingApiClient.getPriceBySymbol("BTC")).thenReturn(mockBtcPrice);
        when(pricingApiClient.getPriceBySymbol("ETH")).thenReturn(mockEthPrice);

        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);


        pricingScheduledJob.execute(mockContext);


        verify(pricingApiClient, times(1)).getPriceBySymbol("BTC");
        verify(pricingApiClient, times(1)).getPriceBySymbol("ETH");


        List<Asset> btcAssets = assetRepository.findBySymbol("BTC");
        List<Asset> ethAssets = assetRepository.findBySymbol("ETH");

        assertThat(btcAssets).hasSize(2);
        assertThat(ethAssets).hasSize(1);


        for (Asset btcAsset : btcAssets) {
            assertThat(btcAsset.getPrice()).isEqualByComparingTo(new BigDecimal("55000.50"));
            assertThat(btcAsset.getValue()).isEqualByComparingTo(
                btcAsset.getQuantity().multiply(new BigDecimal("55000.50"))
            );
        }

        Asset ethAsset = ethAssets.getFirst();
        assertThat(ethAsset.getPrice()).isEqualByComparingTo(new BigDecimal("3200.75"));
        assertThat(ethAsset.getValue()).isEqualByComparingTo(
            ethAsset.getQuantity().multiply(new BigDecimal("3200.75"))
        );
    }

    @Test
    public void testSchedulerIsConfiguredCorrectly() throws SchedulerException {
        scheduler.start();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(scheduler.isStarted()).isTrue()
        );

        scheduler.shutdown();
    }

    @Test
    public void testPricingJobHandlesException() throws JobExecutionException {
        when(pricingApiClient.getPriceBySymbol(anyString())).thenThrow(new RuntimeException("API Error"));


        List<Asset> originalBtcAssets = assetRepository.findBySymbol("BTC");
        List<Asset> originalEthAssets = assetRepository.findBySymbol("ETH");
        BigDecimal originalBtcPrice = originalBtcAssets.getFirst().getPrice();
        BigDecimal originalEthPrice = originalEthAssets.getFirst().getPrice();

        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);

        pricingScheduledJob.execute(mockContext);

        verify(pricingApiClient, atLeastOnce()).getPriceBySymbol(anyString());

        List<Asset> btcAssetsAfter = assetRepository.findBySymbol("BTC");
        List<Asset> ethAssetsAfter = assetRepository.findBySymbol("ETH");

        for (Asset btcAsset : btcAssetsAfter) {
            assertThat(btcAsset.getPrice()).isEqualByComparingTo(originalBtcPrice);
        }

        for (Asset ethAsset : ethAssetsAfter) {
            assertThat(ethAsset.getPrice()).isEqualByComparingTo(originalEthPrice);
        }
    }

    @Test
    public void testPricingJobWithEmptyAssets() throws JobExecutionException {
        assetRepository.deleteAll();

        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);

        pricingScheduledJob.execute(mockContext);

        verify(pricingApiClient, never()).getPriceBySymbol(anyString());

        assertThat(assetRepository.findAll()).isEmpty();
        assertThat(assetRepository.findDistinctSymbols()).isEmpty();
    }
}
