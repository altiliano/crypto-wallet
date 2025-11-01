package com.crypto.wallet.management.services;

import com.crypto.wallet.management.PriceAssets;
import com.crypto.wallet.management.PricingApiClient;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.dto.AssetDto;

import java.math.BigDecimal;
import java.util.*;

public class WalletManagementServiceImpl implements WalletManagementService {
    private final Map<String, WalletDto> wallets = new HashMap<>();
    private final PricingApiClient pricingApiClient;

    public WalletManagementServiceImpl(PricingApiClient pricingApiClient) {
        this.pricingApiClient = pricingApiClient;
    }

    @Override
    public WalletDto create(String email) {
        WalletDto wallet = new WalletDto(UUID.randomUUID().toString(), email, BigDecimal.ZERO, new ArrayList<>());
        wallets.put(email, wallet);
        return wallet;
    }

    @Override
    public WalletDto addAsset(String email, AssetDto newAsset) {
        WalletDto wallet = wallets.get(email);

       PriceAssets price =  pricingApiClient.getPriceBySymbol(newAsset.getSymbol());

        if (price.getData() == null ||  price.getData().getFirst() == null) {
            return wallet;
        }

        AssetDto asset = AssetDto.builder()
                .symbol(newAsset.getSymbol())
                .quantity(newAsset.getQuantity())
                .price(new BigDecimal(price.getData().getFirst()))
                .value(newAsset.getValue())
                .build();

        wallet.getAssets().add(asset);
        BigDecimal total = wallet.getTotal().add(newAsset.getValue()).setScale(2, java.math.RoundingMode.HALF_UP);
        wallet.setTotal(total);

        return wallet;
    }

    @Override
    public WalletDto getWallet(String email) {
        return wallets.get(email);
    }
}
