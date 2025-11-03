package com.crypto.wallet.management;

import com.crypto.wallet.management.repository.WalletRepository;
import com.crypto.wallet.management.service.CoinCapPricingService;
import com.crypto.wallet.management.mapper.WalletMapper;
import com.crypto.wallet.management.mapper.AssetMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ManagementApplicationTests {

	@MockitoBean
	private CoinCapPricingService coinCapPricingService;

	@MockitoBean
	private WalletRepository walletRepository;

	@MockitoBean
	private WalletMapper walletMapper;

	@MockitoBean
	private AssetMapper assetMapper;

	@Test
	void contextLoads() {
	}

}
