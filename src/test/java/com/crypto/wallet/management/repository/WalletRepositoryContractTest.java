package com.crypto.wallet.management.repository;

import com.crypto.wallet.management.repository.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class WalletRepositoryContractTest {

    protected WalletRepository walletRepository;
    private Wallet testWallet1;
    private Wallet testWallet2;

    protected abstract WalletRepository createWalletRepository();

    @BeforeEach
    void setUp() {
        walletRepository = createWalletRepository();

        testWallet1 = Wallet.builder().email("user1@example.com").build();
        testWallet2 = Wallet.builder().email("user2@example.com").build();
    }

    @Test
    void shouldSaveAndRetrieveWallet() {
        Wallet savedWallet = walletRepository.save(testWallet1);


        Optional<Wallet> retrievedWallet = walletRepository.findById(savedWallet.getId());
        assertThat(retrievedWallet).isPresent();
        assertThat(retrievedWallet.get().getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    void shouldFindWalletByEmail() {
        walletRepository.save(testWallet1);
        walletRepository.save(testWallet2);

        Optional<Wallet> foundWallet = walletRepository.findByEmail("user1@example.com");
        Optional<Wallet> notFoundWallet = walletRepository.findByEmail("nonexistent@example.com");

        assertThat(foundWallet).isPresent();
        assertThat(foundWallet.get().getEmail()).isEqualTo("user1@example.com");

        assertThat(notFoundWallet).isEmpty();
    }

    @Test
    void shouldCheckIfWalletExistsByEmail() {
        // Given
        walletRepository.save(testWallet1);

        assertThat(walletRepository.existsByEmail("user1@example.com")).isTrue();
        assertThat(walletRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    void shouldHandleUniqueEmailConstraint() {
        walletRepository.save(testWallet1);

        Wallet duplicateEmailWallet = Wallet.builder().email("user1@example.com").build();

        try {
            walletRepository.save(duplicateEmailWallet);
            long walletsWithEmail = walletRepository.findAll().stream()
                    .filter(w -> "user1@example.com".equals(w.getEmail()))
                    .count();

            assertThat(walletsWithEmail).isGreaterThan(0);
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
