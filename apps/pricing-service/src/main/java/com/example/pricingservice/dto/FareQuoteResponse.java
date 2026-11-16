package com.example.pricingservice.dto;

import com.example.commoncontracts.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FareQuoteResponse(
        UUID quoteId,
        ServiceType serviceType,
        BigDecimal amount,
        double surgeMultiplier,
        double distanceKm,
        double estimatedDurationMinutes,
        Instant expiresAt,
        BigDecimal baseFare,
        BigDecimal total,
        String currency,
        String ruleVersion
) {
    @JsonProperty("quote_id")
    public UUID quote_id() {
        return quoteId;
    }

    @JsonProperty("totalFare")
    public BigDecimal totalFare() {
        return amount;
    }

    @JsonProperty("fareAmount")
    public BigDecimal fareAmount() {
        return amount;
    }

    @JsonProperty("fare_amount")
    public BigDecimal fare_amount() {
        return amount;
    }
}