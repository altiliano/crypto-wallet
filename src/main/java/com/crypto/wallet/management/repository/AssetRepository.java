package com.crypto.wallet.management.repository;

import com.crypto.wallet.management.repository.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findBySymbol(String symbol);
}
