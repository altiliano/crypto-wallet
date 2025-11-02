package com.crypto.wallet.management.mapper;

import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.repository.entities.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AssetMapper {
    AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

    @Mapping(target = "wallet", ignore = true)
    Asset toEntity(AssetDto dto);

    AssetDto toDto(Asset entity);
}
