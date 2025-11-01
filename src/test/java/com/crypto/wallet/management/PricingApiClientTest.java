package com.crypto.wallet.management;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PricingApiClientTest {

    private MockWebServer mockWebServer;
    private PricingApiClient pricingApiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        pricingApiClient = new PricingApiClient() {
            private final RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer  banaa")
                    .build();

            @Override
            public PriceAssets getPriceBySymbol(String symbols) {
                return client.get()
                        .uri("price/bysymbol/{symbols}", symbols)
                        .retrieve()
                        .body(PriceAssets.class);
            }
        };
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getPriceForSingleSymbol() throws InterruptedException {
        String symbol = "BTC";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                            "timestamp": 1762001097228,
                            "data": ["110118.610000000000582077"]
                        }
                        """));


        PriceAssets result = pricingApiClient.getPriceBySymbol(symbol);


        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isEqualTo(1762001097228L);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst()).isEqualTo("110118.610000000000582077");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/price/bysymbol/" + symbol);
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer  banaa");
    }

    @Test
    void getPriceForMultipleSymbols() throws InterruptedException {

        String symbols = "ADA, BTC";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                            "timestamp": 1762001097228,
                            "data": [
                                "0.613999999999999990",
                                "110118.610000000000582077"
                            ]
                        }
                        """));


        PriceAssets result = pricingApiClient.getPriceBySymbol(symbols);


        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isEqualTo(1762001097228L);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0)).isEqualTo("0.613999999999999990");
        assertThat(result.getData().get(1)).isEqualTo("110118.610000000000582077");

        RecordedRequest request = mockWebServer.takeRequest();
        String expectedPath = "/price/bysymbol/" + "ADA%2C%20BTC";
        assertThat(request.getPath()).isEqualTo(expectedPath);
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    void getPriceForInvalidSymbol() throws InterruptedException {

        String symbol = "INVALID";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                            "timestamp": 1762001097228,
                            "data": [null]
                        }
                        """));


        PriceAssets result = pricingApiClient.getPriceBySymbol(symbol);


        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isEqualTo(1762001097228L);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst()).isNull();

        // Verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/price/bysymbol/" + symbol);
    }
}

