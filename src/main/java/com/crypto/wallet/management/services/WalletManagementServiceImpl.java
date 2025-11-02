package com.crypto.wallet.management.services;

import com.crypto.wallet.management.PriceAssets;
import com.crypto.wallet.management.PricingApiClient;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.mapper.AssetMapper;
import com.crypto.wallet.management.mapper.WalletMapper;
import com.crypto.wallet.management.repository.WalletRepository;
import com.crypto.wallet.management.repository.entities.Asset;
import com.crypto.wallet.management.repository.entities.Wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class WalletManagementServiceImpl implements WalletManagementService {
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final AssetMapper assetMapper;
    private final PricingApiClient pricingApiClient;

    public WalletManagementServiceImpl(WalletRepository walletRepository, WalletMapper walletMapper, AssetMapper assetMapper, PricingApiClient pricingApiClient) {
        this.walletRepository = walletRepository;
        this.walletMapper = walletMapper;
        this.assetMapper = assetMapper;
        this.pricingApiClient = pricingApiClient;
    }

    @Override
    public WalletDto create(String email) {
       Wallet savedWallet = walletRepository.save(
                Wallet.builder()
                        .email(email)
                        .build()
        );

        return  walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto addAsset(String email, AssetDto newAsset) {
        Wallet wallet = walletRepository.findByEmail(email).orElse(null);
        if (wallet == null) {
            return null;
        }

        PriceAssets price = pricingApiClient.getPriceBySymbol(newAsset.getSymbol());
        if (price.getData() == null || price.getData().isEmpty() || price.getData().getFirst() == null) {
            return walletMapper.toDto(wallet);
        }

        Asset asset = assetMapper.toEntity(newAsset);
        BigDecimal assetPrice = new BigDecimal(price.getData().getFirst());

        asset.setPrice(assetPrice);
        wallet.addAsset(asset);

        Wallet savedWallet = walletRepository.save(wallet);

        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto getWallet(String email) {
        Wallet wallet = walletRepository.findByEmail(email).orElse(null);
        if (wallet == null) {
            return null;
        }
        BigDecimal total = calculateTotal(wallet.getAssets());
        WalletDto walletDto = walletMapper.toDto(wallet);
        walletDto.setTotal(total);
        return walletDto;
    }

    private BigDecimal calculateTotal(List<Asset> assets) {
        if (assets == null || assets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return assets.stream()
                .map(Asset::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
