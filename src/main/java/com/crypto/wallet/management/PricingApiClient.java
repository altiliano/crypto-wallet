package com.crypto.wallet.management;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PricingApiClient {

    private final RestClient client;

    public PricingApiClient() {
        this.client = RestClient.builder().baseUrl("https://rest.coincap.io/v3/")
                .defaultHeader("Authorization", "Bearer  banaa")
                .build();
    }

    public PriceAssets getPriceBySymbol(String symbols) {
        return client.get()
                .uri("/price/bysymbol/{symbols}", symbols)
                .retrieve()
                .body(PriceAssets.class);
    }
}
