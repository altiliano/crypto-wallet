package com.crypto.wallet.management;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ManagementApplication {

	@Value("${price.update.interval.in.minutes:5}")
	private int priceUpdateIntervalInMinutes;

	public static void main(String[] args) {
		SpringApplication.run(ManagementApplication.class, args);
	}


	@Bean
	public JobDetail pricingJobDetail() {
		return JobBuilder.newJob(PricingScheduledJob.class)
				.withIdentity("pricingJob")
				.storeDurably()
				.build();
	}

	@Bean
	public Trigger pricingJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInMinutes(priceUpdateIntervalInMinutes)
				.repeatForever();

		return TriggerBuilder.newTrigger()
				.forJob(pricingJobDetail())
				.withIdentity("pricingTrigger")
				.withSchedule(scheduleBuilder)
				.build();
	}

}
