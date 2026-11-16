package com.example.commoncontracts.dto;

import com.example.commoncontracts.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FareQuoteDto(
        UUID quoteId,
        UUID customerId,
        ServiceType serviceType,
        BigDecimal amount,
        double surgeMultiplier,
        Instant expiresAt
) {
    @JsonProperty("quote_id")
    public UUID quote_id() {
        return quoteId;
    }
}