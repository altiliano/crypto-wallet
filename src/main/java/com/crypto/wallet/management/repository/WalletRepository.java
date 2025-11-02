package com.crypto.wallet.management.repository;

import com.crypto.wallet.management.repository.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByEmail(String email);
    boolean existsByEmail(String email);
}
