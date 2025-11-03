package com.crypto.wallet.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {
    private List<SimulationAsset> assets;
    private LocalDate date;
}
