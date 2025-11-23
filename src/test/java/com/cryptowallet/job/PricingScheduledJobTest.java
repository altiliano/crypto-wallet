package com.cryptowallet.job;

import com.crypto.wallet.management.ManagementApplication;
import com.crypto.wallet.management.PricingScheduledJob;
import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.repository.WalletRepository;
import com.crypto.wallet.management.repository.entities.Asset;
import com.crypto.wallet.management.repository.entities.Wallet;
import com.crypto.wallet.management.service.AssetPriceUpdateService;
import com.crypto.wallet.management.mapper.WalletMapper;
import com.crypto.wallet.management.mapper.AssetMapper;
import com.crypto.wallet.management.service.PricingService;
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
import repository.StubPricingClient;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ManagementApplication.class)
public class PricingScheduledJobTest {

    private final PricingService coinCapPricingService = new StubPricingClient();

    @MockitoBean
    private WalletRepository walletRepository;

    @MockitoBean
    private WalletMapper walletMapper;

    @MockitoBean
    private AssetMapper assetMapper;

    @Autowired
    private Scheduler scheduler;

    private AssetRepository assetRepository;
    private PricingScheduledJob pricingScheduledJob;

    @BeforeEach
    public void setUp() {
        assetRepository = new InMemoryAssetRepository();
        AssetPriceUpdateService assetPriceUpdateService = new AssetPriceUpdateService(assetRepository);
        pricingScheduledJob = new PricingScheduledJob(coinCapPricingService, assetRepository, assetPriceUpdateService);

        assetRepository.deleteAll();

        Wallet testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setEmail("test@example.com");

        Asset btcAsset = Asset.builder()
                .symbol("BTC")
                .quantity(new BigDecimal("1.5"))
                .price(new BigDecimal("2000"))
                .value(new BigDecimal("75000"))
                .wallet(testWallet)
                .build();

        Asset btcAsset2 = Asset.builder()
                .symbol("BTC")
                .quantity(new BigDecimal("0.5"))
                .price(new BigDecimal("2000"))
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

        Asset errorAsset = Asset.builder()
                .symbol("ERROR")
                .quantity(new BigDecimal("0.5"))
                .price(new BigDecimal("1000"))
                .value(new BigDecimal("25000"))
                .wallet(testWallet)
                .build();

        assetRepository.saveAll(Arrays.asList(btcAsset, btcAsset2, ethAsset, errorAsset));
    }

    @Test
    public void testPricingJobExecutesSuccessfully() throws JobExecutionException {
        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);

        pricingScheduledJob.execute(mockContext);


        List<Asset> btcAssets = assetRepository.findBySymbol("BTC");
        List<Asset> ethAssets = assetRepository.findBySymbol("ETH");

        assertThat(btcAssets).hasSize(2);
        assertThat(ethAssets).hasSize(1);



        for (Asset btcAsset : btcAssets) {
            assertThat(btcAsset.getPrice()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(btcAsset.getValue()).isEqualByComparingTo(
                    btcAsset.getQuantity().multiply(new BigDecimal("50000"))
            );
        }


        Asset ethAsset = ethAssets.getFirst();
        assertThat(ethAsset.getPrice()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(ethAsset.getValue()).isEqualByComparingTo(
                ethAsset.getQuantity().multiply(new BigDecimal("3000"))
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

        List<Asset> originalErrorAssets = assetRepository.findBySymbol("ERROR");
        BigDecimal originalErrorPrice = originalErrorAssets.getFirst().getPrice();

        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);

        pricingScheduledJob.execute(mockContext);


        List<Asset> errorAssetsAfter = assetRepository.findBySymbol("ERROR");
        for (Asset errorAsset : errorAssetsAfter) {
            assertThat(errorAsset.getPrice()).isEqualByComparingTo(originalErrorPrice);
            assertThat(errorAsset.getPrice()).isEqualByComparingTo(new BigDecimal("1000"));
        }

        List<Asset> btcAssetsAfter = assetRepository.findBySymbol("BTC");
        List<Asset> ethAssetsAfter = assetRepository.findBySymbol("ETH");

        for (Asset btcAsset : btcAssetsAfter) {
            assertThat(btcAsset.getPrice()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        for (Asset ethAsset : ethAssetsAfter) {
            assertThat(ethAsset.getPrice()).isEqualByComparingTo(new BigDecimal("3000"));
        }
    }

    @Test
    public void testPricingJobWithEmptyAssets() throws JobExecutionException {
        assetRepository.deleteAll();

        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);

        pricingScheduledJob.execute(mockContext);


        assertThat(assetRepository.findAll()).isEmpty();
        assertThat(assetRepository.findDistinctSymbols()).isEmpty();
    }
}
