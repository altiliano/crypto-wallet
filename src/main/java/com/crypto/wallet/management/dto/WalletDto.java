package com.crypto.wallet.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletDto {
    private String id;
    private BigDecimal total;
    private List<AssetDto> assets;

    public WalletDto(String id, BigDecimal total, List<AssetDto> assets) {
        this.id = id;
        this.total = total;
        this.assets = assets;
    }
}

