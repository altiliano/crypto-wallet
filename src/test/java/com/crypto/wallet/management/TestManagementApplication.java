package com.crypto.wallet.management;

import org.springframework.boot.SpringApplication;

public class TestManagementApplication {

	public static void main(String[] args) {
		SpringApplication.from(ManagementApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
