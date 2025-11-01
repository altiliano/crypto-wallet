package com.crypto.wallet.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletDto {
    private String id;
    private String email;
    private BigDecimal total;
    private List<AssetDto> assets;
}
