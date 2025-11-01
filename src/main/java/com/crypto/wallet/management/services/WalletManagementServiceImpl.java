package com.crypto.wallet.management.services;

import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.dto.AssetDto;

import java.math.BigDecimal;
import java.util.*;

public class WalletManagementServiceImpl implements WalletManagementService {
    private final Map<String, WalletDto> wallets = new HashMap<>();

    @Override
    public WalletDto create(String email) {
        WalletDto wallet = new WalletDto(UUID.randomUUID().toString(), email, BigDecimal.ZERO, new ArrayList<>());
        wallets.put(email, wallet);
        return wallet;
    }

    @Override
    public WalletDto addAsset(String email, AssetDto newAsset) {
        WalletDto wallet = wallets.get(email);

        AssetDto asset = AssetDto.builder()
                .symbol(newAsset.getSymbol())
                .quantity(newAsset.getQuantity())
                .price(newAsset.getPrice())
                .value(BigDecimal.ZERO)
                .build();

        wallet.getAssets().add(asset);

        return wallet;
    }
}
