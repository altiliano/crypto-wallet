package com.crypto.wallet.management;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PricingScheduledJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(PricingScheduledJob.class);

    private final PricingApiClient pricingApiClient;

    public PricingScheduledJob(PricingApiClient pricingApiClient) {
        this.pricingApiClient = pricingApiClient;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.info("Executing scheduled pricing job");

            var btcPrice = pricingApiClient.getPriceBySymbol("BTC");
            var ethPrice = pricingApiClient.getPriceBySymbol("ETH");

            logger.info("Retrieved prices - BTC: {}, ETH: {}", btcPrice, ethPrice);
        } catch (Exception e) {
            logger.error("Error executing pricing job", e);
            throw new JobExecutionException(e);
        }
    }
}
