package repository;

import com.crypto.wallet.management.repository.AssetRepository;
import com.crypto.wallet.management.repository.entities.Asset;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * In-memory implementation of AssetRepository for testing purposes.
 * This implementation stores assets in memory using a ConcurrentHashMap.
 */
public class InMemoryAssetRepository implements AssetRepository {

    private final Map<Long, Asset> assets = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Asset> findBySymbol(String symbol) {
        return assets.values().stream()
                .filter(asset -> Objects.equals(asset.getSymbol(), symbol))
                .collect(Collectors.toList());
    }

    @Override
    public <S extends Asset> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        assets.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends Asset> List<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            savedEntities.add(save(entity));
        }
        return savedEntities;
    }

    @Override
    public Optional<Asset> findById(Long id) {
        return Optional.ofNullable(assets.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return assets.containsKey(id);
    }

    @Override
    public List<Asset> findAll() {
        return new ArrayList<>(assets.values());
    }

    @Override
    public List<Asset> findAllById(Iterable<Long> ids) {
        List<Asset> result = new ArrayList<>();
        for (Long id : ids) {
            Optional<Asset> asset = findById(id);
            asset.ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        return assets.size();
    }

    @Override
    public void deleteById(Long id) {
        assets.remove(id);
    }

    @Override
    public void delete(Asset entity) {
        if (entity.getId() != null) {
            assets.remove(entity.getId());
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            assets.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Asset> entities) {
        for (Asset entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        assets.clear();
    }

    // Additional methods required by JpaRepository interface
    @Override
    public void flush() {
        // No-op for in-memory implementation
    }

    @Override
    public <S extends Asset> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends Asset> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Asset> entities) {
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
    public Asset getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Asset getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Asset getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Asset> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("findOne with Example not implemented in InMemoryAssetRepository");
    }

    @Override
    public <S extends Asset> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("findAll with Example not implemented in InMemoryAssetRepository");
    }

    @Override
    public <S extends Asset> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("findAll with Example and Sort not implemented in InMemoryAssetRepository");
    }

    @Override
    public <S extends Asset> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Example and Pageable not implemented in InMemoryAssetRepository");
    }

    @Override
    public <S extends Asset> long count(Example<S> example) {
        throw new UnsupportedOperationException("count with Example not implemented in InMemoryAssetRepository");
    }

    @Override
    public <S extends Asset> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("exists with Example not implemented in InMemoryAssetRepository");
    }

    @Override
    public <S extends Asset, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("findBy with Example not implemented in InMemoryAssetRepository");
    }

    @Override
    public List<Asset> findAll(Sort sort) {
        throw new UnsupportedOperationException("findAll with Sort not implemented in InMemoryAssetRepository");
    }

    @Override
    public Page<Asset> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Pageable not implemented in InMemoryAssetRepository");
    }

    /**
     * Clear all stored assets. Useful for test cleanup.
     */
    public void clear() {
        assets.clear();
        idGenerator.set(1);
    }
}
