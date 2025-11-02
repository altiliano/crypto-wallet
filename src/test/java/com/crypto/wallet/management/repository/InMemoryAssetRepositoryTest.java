package com.crypto.wallet.management.repository;

import org.junit.jupiter.api.AfterEach;
import repository.InMemoryAssetRepository;

class InMemoryAssetRepositoryTest extends AssetRepositoryContractTest {
    private InMemoryAssetRepository inMemoryRepository;

    @Override
    protected AssetRepository createAssetRepository() {
        inMemoryRepository = new InMemoryAssetRepository();
        return inMemoryRepository;
    }

    @AfterEach
    void tearDown() {
        // Clear the in-memory repository after each test
        if (inMemoryRepository != null) {
            inMemoryRepository.clear();
        }
    }
}
