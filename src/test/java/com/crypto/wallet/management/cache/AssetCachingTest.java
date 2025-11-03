package com.crypto.wallet.management.cache;

import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.service.AssetCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
    "spring.quartz.auto-startup=false",
    "spring.task.scheduling.enabled=false"
})
@EnableCaching
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AssetCachingTest {

    @Autowired
    private AssetCacheService assetCacheService;

    @MockitoBean
    private AssetRepository assetRepository;

    @BeforeEach
    void setUp() {
        assetCacheService.clearDistinctSymbolsCache();
        reset(assetRepository);
    }

    @Test
    public void weShouldHitTheCacheAfterBeingPopulated() {
        List<String> mockSymbols = Arrays.asList("BTC", "ETH");
        given(assetRepository.findDistinctSymbols()).willReturn(mockSymbols);

        int numberOfRepositoryCalls = 0;
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        List<String> firstCall = assetCacheService.getDistinctSymbols();
        numberOfRepositoryCalls++;
        assertThat(firstCall).containsExactlyInAnyOrder("BTC", "ETH");
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();

        List<String> secondCall = assetCacheService.getDistinctSymbols();
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        assertThat(secondCall).isEqualTo(firstCall);

        List<String> thirdCall = assetCacheService.getDistinctSymbols();
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        assertThat(thirdCall).isEqualTo(firstCall);
    }

    @Test
    public void weShouldHitTheRepositoryAfterCleaningTheCache() {
        List<String> mockSymbols = Arrays.asList("BTC", "ETH");
        given(assetRepository.findDistinctSymbols()).willReturn(mockSymbols);


        int numberOfRepositoryCalls = 1;
        List<String> firstCall = assetCacheService.getDistinctSymbols();
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        assertThat(firstCall).containsExactlyInAnyOrder("BTC", "ETH");

        List<String> secondCall = assetCacheService.getDistinctSymbols();
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        assertThat(secondCall).isEqualTo(firstCall);

        assetCacheService.clearDistinctSymbolsCache();

        List<String> afterClear = assetCacheService.getDistinctSymbols();
        verify(assetRepository, times(numberOfRepositoryCalls + 1)).findDistinctSymbols();
        assertThat(afterClear).containsExactlyInAnyOrder("BTC", "ETH");
    }

    @Test
    public void weShouldNotCacheIfThereIsNotAssets() {
        List<String> emptySymbols = List.of();
        given(assetRepository.findDistinctSymbols()).willReturn(emptySymbols);

        int numberOfRepositoryCalls = 0;


        List<String> firstCall = assetCacheService.getDistinctSymbols();
        numberOfRepositoryCalls++;
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        assertThat(firstCall).isEmpty();

        List<String> secondCall = assetCacheService.getDistinctSymbols();
        numberOfRepositoryCalls++;
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        assertThat(secondCall).isEmpty();

        List<String> thirdCall = assetCacheService.getDistinctSymbols();
        numberOfRepositoryCalls++;
        verify(assetRepository, times(numberOfRepositoryCalls)).findDistinctSymbols();
        assertThat(thirdCall).isEmpty();
    }

}
