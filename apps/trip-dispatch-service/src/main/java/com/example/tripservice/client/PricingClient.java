package com.example.tripservice.client;

import com.example.commoncontracts.dto.FareQuoteDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Component
public class PricingClient {

    private final RestClient restClient;

    public PricingClient(@Value("${pricing-service.base-url:http://localhost:8084}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public FareQuoteDto getQuote(UUID quoteId) {
        try {
            return restClient.get()
                    .uri("/api/v1/quotes/{quoteId}", quoteId)
                    .retrieve()
                    .body(FareQuoteDto.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        }
    }
}
