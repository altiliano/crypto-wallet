package com.crypto.wallet.management.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Bean
    public CacheManager cacheManager() {

        SimpleCacheManager cacheManager = new SimpleCacheManager();

        long expiredTimeInMinutes = 5 * 60 * 1000L;
        TTLConcurrentMapCache distinctSymbolsCache = new TTLConcurrentMapCache("distinctSymbols", expiredTimeInMinutes);

        cacheManager.setCaches(List.of(distinctSymbolsCache));
        return cacheManager;
    }


    public static class TTLConcurrentMapCache extends ConcurrentMapCache {
        private final long ttl;
        private final ConcurrentHashMap<Object, Long> timestamps = new ConcurrentHashMap<>();
        private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

        public TTLConcurrentMapCache(String name, long ttlMillis) {
            super(name);
            this.ttl = ttlMillis;

            cleanupExecutor.scheduleAtFixedRate(this::evictExpired, 60, 60, TimeUnit.SECONDS);
        }

        @Override
        public void put(Object key, Object value) {
            super.put(key, value);
            timestamps.put(key, System.currentTimeMillis());
        }

        @Override
        public ValueWrapper get(@NotNull Object key) {
            Long timestamp = timestamps.get(key);
            if (timestamp != null && System.currentTimeMillis() - timestamp > ttl) {
                // Entry expired, remove it
                evict(key);
                return null;
            }
            return super.get(key);
        }

        @Override
        public void evict(@NotNull Object key) {
            super.evict(key);
            timestamps.remove(key);
        }

        private void evictExpired() {
            long now = System.currentTimeMillis();
            timestamps.entrySet().removeIf(entry -> {
                if (now - entry.getValue() > ttl) {
                    super.evict(entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }
}
