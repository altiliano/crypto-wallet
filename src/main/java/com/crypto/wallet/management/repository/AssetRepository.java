package com.crypto.wallet.management.repository;

import com.crypto.wallet.management.repository.entities.Asset;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findBySymbol(String symbol);

   // @Cacheable(value = "distinctSymbols", unless = "#result == null or #result.isEmpty()")
    @Query("SELECT DISTINCT a.symbol FROM Asset a")
    List<String> findDistinctSymbols();

    @Modifying
    @Query("UPDATE Asset a SET a.price = :price, a.value = a.quantity * :price WHERE a.symbol = :symbol")
    int updatePriceBySymbol(@Param("symbol") String symbol, @Param("price") BigDecimal price);
}
