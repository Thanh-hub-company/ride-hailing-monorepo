package com.example.commoncontracts.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TripCompletedEvent(
        UUID tripId,
        UUID customerId,
        UUID driverId,
        BigDecimal finalFare,
        String currency,
        Instant completedAt
) {
    public TripCompletedEvent(UUID tripId, UUID customerId, UUID driverId, BigDecimal finalFare, String currency) {
        this(tripId, customerId, driverId, finalFare, currency, Instant.now());
    }
}