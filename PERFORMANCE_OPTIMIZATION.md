# Performance Optimization Guide

## 📊 Current System Analysis

### What the System Does
- **Cryptocurrency Portfolio Management**: One wallet per email with multiple crypto assets
- **Real-time Price Tracking**: Fetches live crypto prices from CoinCap API every 5 minutes
- **Portfolio Simulation**: Calculates hypothetical portfolio values at specific dates
- **Concurrent Processing**: Uses multi-threading (max 3 concurrent requests) for price updates
- **REST API**: Provides endpoints for wallet CRUD operations

---

## 🎯 Performance Improvements

### 1. Caching Strategy (CRITICAL - Already Implemented ✅)

**Current Implementation**: `CacheConfig.java` with Caffeine cache

**Enhancements**:

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats()); // Enable metrics
        return cacheManager;
    }
    
    // Add historical price cache
    @Bean
    public Cache<String, BigDecimal> historicalPriceCache() {
        return Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(5000)
            .recordStats()
            .build();
    }
}
```

**Impact**: Reduces API calls by ~95% for frequently accessed assets.

---

### 2. Database Optimizations

#### Add Missing Indexes

Create new migration: `V2__add_performance_indexes.sql`

```sql
-- For wallet lookups by email (most common query)
CREATE INDEX IF NOT EXISTS idx_wallets_email ON wallets(email);

-- For asset queries by wallet
CREATE INDEX IF NOT EXISTS idx_assets_wallet_id ON assets(wallet_id);

-- For asset symbol searches
CREATE INDEX IF NOT EXISTS idx_assets_symbol ON assets(symbol);

-- Composite index for common join queries
CREATE INDEX IF NOT EXISTS idx_assets_wallet_symbol ON assets(wallet_id, symbol);

-- For created_at queries if you add timestamp filtering
CREATE INDEX IF NOT EXISTS idx_wallets_created_at ON wallets(created_at);
CREATE INDEX IF NOT EXISTS idx_assets_created_at ON assets(created_at);
```

#### Connection Pooling Configuration

Update `application.properties`:

```properties
# HikariCP Configuration (Spring Boot default)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000

# Statement cache
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

**Impact**: 3-5x faster queries, better handling of concurrent requests.

---

### 3. Batch Price Updates

**Current Issue**: Sequential API calls for multiple assets.

**Enhancement** in `CoinCapPricingService.java`:

```java
@Service
public class CoinCapPricingService implements PricingService {
    
    private final RestTemplate restTemplate;
    private final Executor executor;
    
    /**
     * Fetch prices for multiple symbols in parallel
     */
    public Map<String, BigDecimal> getPricesBatch(List<String> symbols) {
        List<CompletableFuture<Map.Entry<String, BigDecimal>>> futures = symbols.stream()
            .map(symbol -> CompletableFuture.supplyAsync(() -> {
                try {
                    BigDecimal price = getPrice(symbol);
                    return Map.entry(symbol, price);
                } catch (Exception e) {
                    log.error("Failed to fetch price for {}", symbol, e);
                    return null;
                }
            }, executor))
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            .join();
    }
}
```

**Impact**: Reduces latency by 50-70% for multi-asset operations.

---

### 4. Async REST Endpoints

**Enhancement** in `WalletController.java`:

```java
@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    
    @GetMapping("/{email}")
    public CompletableFuture<ResponseEntity<WalletDto>> getWallet(
        @PathVariable String email
    ) {
        return CompletableFuture.supplyAsync(() -> 
            walletManagementService.getWallet(email)
        ).thenApply(ResponseEntity::ok);
    }
    
    @PostMapping("/simulate")
    public CompletableFuture<ResponseEntity<SimulationResponse>> simulate(
        @RequestBody SimulationRequest request
    ) {
        return CompletableFuture.supplyAsync(() ->
            walletSimulationService.simulate(request)
        ).thenApply(ResponseEntity::ok);
    }
}
```

**Configuration**: Create `AsyncConfig.java`

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-wallet-");
        executor.initialize();
        return executor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> 
            log.error("Async exception in {}: {}", method.getName(), ex.getMessage());
    }
}
```

---

### 5. Circuit Breaker for CoinCap API

**Add Dependency** to `build.gradle`:

```gradle
implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
```

**Configuration** in `application.properties`:

```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.coincap.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.coincap.wait-duration-in-open-state=10000
resilience4j.circuitbreaker.instances.coincap.sliding-window-size=10
resilience4j.circuitbreaker.instances.coincap.minimum-number-of-calls=5

# Retry
resilience4j.retry.instances.coincap.max-attempts=3
resilience4j.retry.instances.coincap.wait-duration=1000
resilience4j.retry.instances.coincap.exponential-backoff-multiplier=2
```

**Implementation** in `CoinCapPricingService.java`:

```java
@Service
public class CoinCapPricingService implements PricingService {
    
    @CircuitBreaker(name = "coincap", fallbackMethod = "getPriceFallback")
    @Retry(name = "coincap")
    public BigDecimal getPrice(String symbol) {
        // CoinCap API call
    }
    
    private BigDecimal getPriceFallback(String symbol, Exception e) {
        log.warn("Circuit breaker fallback for {}: {}", symbol, e.getMessage());
        
        // Try to return cached price
        return assetCacheService.getCachedPrice(symbol)
            .orElseThrow(() -> new ServiceUnavailableException(
                "CoinCap API unavailable and no cached price for " + symbol
            ));
    }
}
```

**Impact**: Prevents cascading failures, improves system resilience.

---

### 6. Rate Limiting

**Add Dependency** to `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'com.bucket4j:bucket4j-core:8.1.0'
```

**Configuration**:

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofSeconds(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
```

**Interceptor**:

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final Bucket bucket;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        if (!bucket.tryConsume(1)) {
            response.setStatus(429); // Too Many Requests
            return false;
        }
        return true;
    }
}
```

---

### 7. Monitoring & Metrics

**Add Dependencies** to `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

**Configuration** in `application.properties`:

```properties
# Actuator
management.endpoints.web.exposure.include=health,metrics,prometheus,info
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# Custom metrics
management.metrics.tags.application=${spring.application.name}
management.metrics.tags.environment=${spring.profiles.active}
```

**Custom Metrics**:

```java
@Service
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    public void recordPriceFetch(String symbol, long duration) {
        Timer.builder("price.fetch")
            .tag("symbol", symbol)
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }
    
    public void incrementCacheHit(String symbol) {
        Counter.builder("cache.hit")
            .tag("symbol", symbol)
            .register(meterRegistry)
            .increment();
    }
}
```

---

### 8. Simulation Optimization

**Enhancement** in `WalletSimulationService.java`:

```java
@Service
public class WalletSimulationService {
    
    private final LoadingCache<SimulationCacheKey, Map<String, BigDecimal>> historicalPriceCache;
    
    public WalletSimulationService() {
        this.historicalPriceCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(5000)
            .build(this::fetchHistoricalPrices);
    }
    
    public SimulationResponse simulate(SimulationRequest request) {
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();
        
        // Get all symbols at once
        List<String> symbols = request.getAssets().stream()
            .map(SimulationAsset::getSymbol)
            .distinct()
            .toList();
        
        // Batch fetch from cache
        Map<String, BigDecimal> prices = historicalPriceCache.get(
            new SimulationCacheKey(symbols, date)
        );
        
        // Calculate totals
        // ...
    }
    
    private Map<String, BigDecimal> fetchHistoricalPrices(SimulationCacheKey key) {
        // Parallel fetch for multiple symbols
        return coinCapPricingService.getHistoricalPricesBatch(key.symbols(), key.date());
    }
}
```

---

## 🔥 Implementation Priority

### Phase 1 (Immediate - Quick Wins)
1. ✅ **Caching** - Already implemented
2. **Database indexes** - Migration file needed
3. **Connection pooling** - Configuration update

**Expected Impact**: 5-10x performance improvement
**Time Required**: 2-4 hours

### Phase 2 (Short-term - High Impact)
4. **Circuit breaker** - Resilience4j integration
5. **Batch price updates** - Service layer enhancement
6. **Async endpoints** - Controller refactoring

**Expected Impact**: 3-5x improvement + better resilience
**Time Required**: 1-2 days

### Phase 3 (Medium-term - Scalability)
7. **Monitoring** - Actuator + Prometheus
8. **Rate limiting** - Bucket4j integration
9. **Simulation optimization** - Cache strategy

**Expected Impact**: Better observability + DoS protection
**Time Required**: 2-3 days

---

## 📊 Performance Benchmarks

### Current Performance (Estimated)
- Single wallet fetch: ~200-500ms
- Add asset: ~300-800ms (with API call)
- Simulation (5 assets): ~2-5 seconds
- Price update job: ~10-30 seconds (depending on asset count)

### After Optimizations (Expected)
- Single wallet fetch: ~20-50ms (95% improvement)
- Add asset: ~50-100ms (cached prices)
- Simulation (5 assets): ~200-500ms (90% improvement)
- Price update job: ~3-8 seconds (parallel processing)

---

## 🎯 Scalability Improvements

### Current Limitations
- Single PostgreSQL instance
- Synchronous API calls
- No distributed caching
- Single application instance

### Future Enhancements

#### 1. Database Scaling
```yaml
# Read replicas for high traffic
spring.datasource.hikari.read-only=true
spring.jpa.properties.hibernate.connection.provider_disables_autocommit=true
```

#### 2. Redis for Distributed Cache
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=300000
```

#### 3. Message Queue for Price Updates
```java
@RabbitListener(queues = "price-update-queue")
public void processPriceUpdate(PriceUpdateMessage message) {
    // Async price update processing
}
```

#### 4. Load Balancing
```yaml
# docker-compose.yml
services:
  app:
    deploy:
      replicas: 3
    ports:
      - "8080-8082:8080"
  
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
```

---

## 🔍 Monitoring Checklist

### Metrics to Track
- [ ] API response times (p50, p95, p99)
- [ ] CoinCap API call frequency
- [ ] Cache hit/miss ratio
- [ ] Database connection pool usage
- [ ] Circuit breaker state
- [ ] Error rates by endpoint
- [ ] Concurrent user count

### Dashboards
```bash
# Prometheus metrics endpoint
http://localhost:8080/actuator/prometheus

# Grafana dashboard (optional)
docker run -d -p 3000:3000 grafana/grafana
```

---

## 📝 Configuration Files Summary

### New Files to Create
1. `src/main/resources/db/migration/V2__add_performance_indexes.sql`
2. `src/main/java/com/crypto/wallet/management/config/AsyncConfig.java`
3. `src/main/java/com/crypto/wallet/management/config/RateLimitConfig.java`
4. `src/main/java/com/crypto/wallet/management/service/MetricsService.java`

### Files to Update
1. `application.properties` - Add HikariCP, circuit breaker, metrics config
2. `build.gradle` - Add Resilience4j, Actuator dependencies
3. `CoinCapPricingService.java` - Add batch methods, circuit breaker
4. `WalletController.java` - Make endpoints async
5. `WalletSimulationService.java` - Add historical price cache

---

## 🧪 Testing Performance

### Load Testing Script

```bash
#!/bin/bash
# load-test.sh

# Install Apache Bench if needed
# sudo apt-get install apache2-utils

# Test wallet creation
ab -n 1000 -c 10 -p wallet.json -T application/json \
  http://localhost:8080/api/wallets

# Test wallet fetch
ab -n 1000 -c 20 \
  http://localhost:8080/api/wallets/test@example.com

# Test simulation
ab -n 500 -c 10 -p simulation.json -T application/json \
  http://localhost:8080/api/wallets/simulate
```

### JMeter Test Plan
1. Download Apache JMeter
2. Create test plan with:
   - Thread Group: 50 users
   - HTTP Requests: Create wallet, Add asset, Get wallet
   - Listeners: View results tree, Summary report

---

## 🔐 Security Considerations

### Rate Limiting Per User
```java
@Bean
public RateLimiter createRateLimiter() {
    return RateLimiter.of("api", RateLimiterConfig.custom()
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .limitForPeriod(10)
        .build());
}
```

### API Key Rotation
```properties
# Rotate keys monthly
coincap.api-key=${COINCAP_API_KEY}
coincap.backup-api-key=${COINCAP_BACKUP_API_KEY}
```

---

## 📚 Additional Resources

- [Spring Boot Performance Tuning](https://spring.io/guides/gs/performance/)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Caffeine Cache Guide](https://github.com/ben-manes/caffeine/wiki)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
- [Prometheus + Spring Boot](https://www.baeldung.com/spring-boot-actuators-prometheus)

---

**Last Updated**: November 22, 2025  
**Version**: 1.0  
**Maintainer**: Development Team

