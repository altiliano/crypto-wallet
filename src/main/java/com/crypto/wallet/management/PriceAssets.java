package com.crypto.wallet.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PriceAssets {
    private Long timestamp;
    private List<String> data;

    public PriceAssets() {
    }
}
