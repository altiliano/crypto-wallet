package com.crypto.wallet.management.repository;

import com.crypto.wallet.management.repository.entities.Wallet;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;


/**
 * In-memory implementation of WalletRepository for testing purposes.
 * This implementation stores wallets in memory using a ConcurrentHashMap.
 */
public class InMemoryWalletRepository implements WalletRepository {

    private final Map<Long, Wallet> wallets = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<Wallet> findByEmail(String email) {
        return wallets.values().stream()
                .filter(wallet -> Objects.equals(wallet.getEmail(), email))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return wallets.values().stream()
                .anyMatch(wallet -> Objects.equals(wallet.getEmail(), email));
    }

    @Override
    public <S extends Wallet> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        wallets.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends Wallet> List<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            savedEntities.add(save(entity));
        }
        return savedEntities;
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return Optional.ofNullable(wallets.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return wallets.containsKey(id);
    }

    @Override
    public List<Wallet> findAll() {
        return new ArrayList<>(wallets.values());
    }

    @Override
    public List<Wallet> findAllById(Iterable<Long> ids) {
        List<Wallet> result = new ArrayList<>();
        for (Long id : ids) {
            Optional<Wallet> wallet = findById(id);
            wallet.ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        return wallets.size();
    }

    @Override
    public void deleteById(Long id) {
        wallets.remove(id);
    }

    @Override
    public void delete(Wallet entity) {
        if (entity.getId() != null) {
            wallets.remove(entity.getId());
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            wallets.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Wallet> entities) {
        for (Wallet entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        wallets.clear();
    }

    // Additional methods required by JpaRepository interface
    @Override
    public void flush() {
        // No-op for in-memory implementation
    }

    @Override
    public <S extends Wallet> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends Wallet> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Wallet> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public Wallet getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Wallet getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Wallet getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Wallet> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("findOne with Example not implemented in InMemoryWalletRepository");
    }

    @Override
    public <S extends Wallet> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("findAll with Example not implemented in InMemoryWalletRepository");
    }

    @Override
    public <S extends Wallet> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("findAll with Example and Sort not implemented in InMemoryWalletRepository");
    }

    @Override
    public <S extends Wallet> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Example and Pageable not implemented in InMemoryWalletRepository");
    }

    @Override
    public <S extends Wallet> long count(Example<S> example) {
        throw new UnsupportedOperationException("count with Example not implemented in InMemoryWalletRepository");
    }

    @Override
    public <S extends Wallet> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("exists with Example not implemented in InMemoryWalletRepository");
    }

    @Override
    public <S extends Wallet, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("findBy with Example not implemented in InMemoryWalletRepository");
    }

    @Override
    public List<Wallet> findAll(Sort sort) {
        throw new UnsupportedOperationException("findAll with Sort not implemented in InMemoryWalletRepository");
    }

    @Override
    public Page<Wallet> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Pageable not implemented in InMemoryWalletRepository");
    }

    /**
     * Clear all stored wallets. Useful for test cleanup.
     */
    public void clear() {
        wallets.clear();
        idGenerator.set(1);
    }
}
