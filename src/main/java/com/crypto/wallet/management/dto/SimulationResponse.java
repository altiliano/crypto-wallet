package com.crypto.wallet.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {
    private BigDecimal total;
    private String bestAsset;
    private BigDecimal bestPerformance;
    private String worstAsset;
    private BigDecimal worstPerformance;
}
