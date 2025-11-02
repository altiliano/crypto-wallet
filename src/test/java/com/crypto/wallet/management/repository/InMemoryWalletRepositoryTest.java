package com.crypto.wallet.management.repository;

import org.junit.jupiter.api.AfterEach;
import repository.InMemoryWalletRepository;

class InMemoryWalletRepositoryTest extends WalletRepositoryContractTest {

    private InMemoryWalletRepository inMemoryRepository;

    @Override
    protected WalletRepository createWalletRepository() {
        inMemoryRepository = new InMemoryWalletRepository();
        return inMemoryRepository;
    }

    @AfterEach
    void tearDown() {
        if (inMemoryRepository != null) {
            inMemoryRepository.clear();
        }
    }
}
