package com.crypto.wallet.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {
    @NotNull(message = "Assets list cannot be null")
    @Valid
    private List<SimulationAsset> assets;

    private LocalDate date;

    public LocalDate getEffectiveDate() {
        return date != null ? date : LocalDate.now();
    }
}
