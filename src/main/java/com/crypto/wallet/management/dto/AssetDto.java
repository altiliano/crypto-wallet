package com.crypto.wallet.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDto {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal value;

}

