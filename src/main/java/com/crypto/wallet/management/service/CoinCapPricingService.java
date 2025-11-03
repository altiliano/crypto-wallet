package com.crypto.wallet.management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CoinCapPricingService implements PricingService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public CoinCapPricingService(RestTemplate restTemplate,
                                @Value("${coincap.api.key:}") String apiKey,
                                @Value("${coincap.api.base-url:https://api.coincap.io/v2}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        try {
            String url = baseUrl + "/assets/" + symbol.toLowerCase();
            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.setBearerAuth(apiKey);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data.containsKey("priceUsd")) {
                    String priceStr = (String) data.get("priceUsd");
                    return Optional.of(new BigDecimal(priceStr));
                }
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
            long endTimestamp = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

            String url = String.format("%s/assets/%s/history?interval=d1&start=%d&end=%d",
                    baseUrl, symbol.toLowerCase(), startTimestamp, endTimestamp);

            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.setBearerAuth(apiKey);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                if (!data.isEmpty()) {
                    Map<String, Object> priceData = data.get(0);
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
}
