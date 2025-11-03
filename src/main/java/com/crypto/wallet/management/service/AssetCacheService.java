package com.crypto.wallet.management.service;

import com.crypto.wallet.management.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetCacheService {

    private static final Logger logger = LoggerFactory.getLogger(AssetCacheService.class);

    private final AssetRepository assetRepository;
    private final CacheManager cacheManager;

    public AssetCacheService(AssetRepository assetRepository, CacheManager cacheManager) {
        this.assetRepository = assetRepository;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "distinctSymbols", unless = "#result == null or #result.isEmpty()")
    public List<String> getDistinctSymbols() {
        return assetRepository.findDistinctSymbols();
    }

    public void clearDistinctSymbolsCache() {
        var cache = cacheManager.getCache("distinctSymbols");
        if (cache != null) {
            cache.clear();
            logger.info("Manually cleared distinct symbols cache");
        }
    }
}
