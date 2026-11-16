package com.example.commoncontracts.event;

import com.example.commoncontracts.enums.DriverStatus;

import java.time.Instant;
import java.util.UUID;

public record DriverAvailabilityChangedEvent(
        UUID driverId,
        DriverStatus previousStatus,
        DriverStatus newStatus,
        Instant updatedAt
) {
    public DriverAvailabilityChangedEvent(UUID driverId, DriverStatus previousStatus, DriverStatus newStatus) {
        this(driverId, previousStatus, newStatus, Instant.now());
    }
}