package com.crypto.wallet.management.service;

import com.crypto.wallet.management.PriceAssets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CoinCapPricingService implements PricingService {

    private final RestClient restClient;

    public CoinCapPricingService(@Value("${coincap.api-key:}") String apiKey,
                                @Value("${coincap.api.base-url:https://pro.coincap.io}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/price/bysymbol/{symbol}", symbol.toLowerCase())
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<String> data =(List<String>) response.get("data");
                return Optional.of(new BigDecimal(data.getFirst()));

            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<BigDecimal> getHistoricalPrice(String symbol, LocalDate date) {
        try {
            long startTimestamp = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            long endTimestamp = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() ;
            String id = getAssetIdBySymbol(symbol).orElse(null);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/assets/{slug}/history?interval=d1&start={start}&end={end}",
                         id, startTimestamp, endTimestamp)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (!data.isEmpty()) {
                    Map<String, Object> priceData = data.getFirst();
                    if (priceData.containsKey("priceUsd")) {
                        String priceStr = (String) priceData.get("priceUsd");
                        return Optional.of(new BigDecimal(priceStr));
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    private Optional<String> getAssetIdBySymbol(String symbol) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/assets?search={symbol}", symbol.toLowerCase())
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

                for (Map<String, Object> asset : data) {
                    String assetSymbol = (String) asset.get("symbol");
                    if (assetSymbol != null && assetSymbol.equalsIgnoreCase(symbol)) {
                        return Optional.ofNullable((String) asset.get("id"));
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public PriceAssets getPrice(String symbols) {
        try {
            return restClient.get()
                    .uri("/price/bysymbol/{symbols}", symbols)
                    .retrieve()
                    .body(PriceAssets.class);
        } catch (Exception e) {
            return PriceAssets.builder()
                    .timestamp(System.currentTimeMillis())
                    .data(List.of())
                    .build();
        }
    }

    public List<PriceAssets> getPrices(List<String> symbols) {
        try {
            String joinedSymbols = String.join(",", symbols);
            PriceAssets response = restClient.get()
                    .uri("/price/bysymbol/{symbols}", joinedSymbols)
                    .retrieve()
                    .body(PriceAssets.class);
            return List.of(response);
        } catch (Exception e) {
            return List.of();
        }
    }
}
