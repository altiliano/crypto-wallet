package com.crypto.wallet.management.services;

import com.crypto.wallet.management.PriceAssets;
import com.crypto.wallet.management.service.CoinCapPricingService;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.exceptions.InvalidSymbolForAssetException;
import com.crypto.wallet.management.exceptions.WalletNotFoundException;
import com.crypto.wallet.management.mapper.AssetMapper;
import com.crypto.wallet.management.mapper.WalletMapper;
import com.crypto.wallet.management.repository.WalletRepository;
import com.crypto.wallet.management.repository.entities.Asset;
import com.crypto.wallet.management.repository.entities.Wallet;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class WalletManagementServiceImpl implements WalletManagementService {
    private final WalletRepository walletRepository;
    private final CoinCapPricingService coinCapPricingService;

    public WalletManagementServiceImpl(WalletRepository walletRepository, CoinCapPricingService coinCapPricingService) {
        this.walletRepository = walletRepository;
        this.coinCapPricingService = coinCapPricingService;
    }

    @Override
    public WalletDto create(String email) {
       Wallet savedWallet = walletRepository.save(
                Wallet.builder()
                        .email(email)
                        .build()
        );

        return  WalletMapper.INSTANCE.toDto(savedWallet);
    }

    @Override
    public WalletDto addAsset(String email, AssetDto newAsset) {
        Wallet wallet = walletRepository.findByEmail(email).orElse(null);
        if (wallet == null) {
            throw new WalletNotFoundException(email);
        }

        PriceAssets priceResponse = coinCapPricingService.getPriceBySymbol(newAsset.getSymbol());
        if (priceResponse.getData() == null || priceResponse.getData().isEmpty() || priceResponse.getData().getFirst() == null) {
            throw new InvalidSymbolForAssetException(newAsset.getSymbol());
        }

        BigDecimal value = newAsset.getQuantity().multiply(newAsset.getPrice()).setScale(2, RoundingMode.HALF_UP);
        newAsset.setValue(value);

        Asset asset = AssetMapper.INSTANCE.toEntity(newAsset);
        asset.setWallet(wallet);
        wallet.addAsset(asset);

        Wallet savedWallet = walletRepository.save(wallet);

        return WalletMapper.INSTANCE.toDto(savedWallet);
    }

    @Override
    public WalletDto getWallet(String email) {
        Wallet wallet = walletRepository.findByEmail(email)
                .orElseThrow(() -> new WalletNotFoundException(email));

        BigDecimal total = calculateTotal(wallet.getAssets());
        WalletDto walletDto = WalletMapper.INSTANCE.toDto(wallet);
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
