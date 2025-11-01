package com.cryptowallet.job;

import com.crypto.wallet.management.ManagementApplication;
import com.crypto.wallet.management.PricingScheduledJob;
import com.crypto.wallet.management.PricingApiClient;
import com.crypto.wallet.management.PriceAssets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = {ManagementApplication.class, PricingScheduledJob.class})
public class PricingScheduledJobTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private PricingScheduledJob pricingScheduledJob;

    @MockitoBean
    private PricingApiClient pricingApiClient;

    @Test
    public void testPricingJobExecutesSuccessfully() throws JobExecutionException {

        // Arrange
        PriceAssets mockBtcPrice = new PriceAssets();
        PriceAssets mockEthPrice = new PriceAssets();

        when(pricingApiClient.getPriceBySymbol("BTC")).thenReturn(mockBtcPrice);
        when(pricingApiClient.getPriceBySymbol("ETH")).thenReturn(mockEthPrice);

        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);


        pricingScheduledJob.execute(mockContext);


        verify(pricingApiClient, times(2)).getPriceBySymbol("BTC");
        verify(pricingApiClient, times(2)).getPriceBySymbol("ETH");
    }

    @Test
    public void testSchedulerIsConfiguredCorrectly() throws SchedulerException {
        scheduler.start();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(scheduler.isStarted()).isTrue();
        });

        scheduler.shutdown();
    }

    @Test
    public void testPricingJobHandlesException() {

        when(pricingApiClient.getPriceBySymbol(anyString())).thenThrow(new RuntimeException("API Error"));

        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);

        assertThrows(JobExecutionException.class, () -> {
            pricingScheduledJob.execute(mockContext);
        });

        verify(pricingApiClient, atLeastOnce()).getPriceBySymbol(anyString());
    }
}
