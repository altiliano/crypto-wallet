package com.crypto.wallet.management.mapper;

import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.repository.entities.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(uses = AssetMapper.class)
public interface WalletMapper {
    WalletMapper INSTANCE = Mappers.getMapper(WalletMapper.class);

    @Mapping(target = "total", expression = "java(calculateTotal(entity))")
    WalletDto toDto(Wallet entity);

    @Mapping(target = "assets", ignore = true)
    Wallet toEntity(WalletDto dto);

    default BigDecimal calculateTotal(Wallet wallet) {
        if (wallet.getAssets() == null || wallet.getAssets().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return wallet.getAssets().stream()
                .map(asset -> AssetMapper.INSTANCE.toDto(asset).getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
