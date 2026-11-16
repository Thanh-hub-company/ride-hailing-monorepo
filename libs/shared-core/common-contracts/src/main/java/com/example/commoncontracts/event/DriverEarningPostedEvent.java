package com.example.commoncontracts.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DriverEarningPostedEvent(
        UUID driverId,
        UUID tripId,
        BigDecimal netAmount,
        BigDecimal commissionAmount,
        String reference,
        Instant createdAt
) {
    public DriverEarningPostedEvent(UUID driverId, UUID tripId, BigDecimal netAmount, BigDecimal commissionAmount, String reference) {
        this(driverId, tripId, netAmount, commissionAmount, reference, Instant.now());
    }
}