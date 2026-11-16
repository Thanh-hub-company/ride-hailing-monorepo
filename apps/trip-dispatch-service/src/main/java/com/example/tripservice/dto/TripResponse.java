package com.example.tripservice.dto;

import com.example.commoncontracts.enums.ServiceType;
import com.example.commoncontracts.enums.TripStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TripResponse(
        UUID tripId,
        String tripCode,
        UUID customerId,
        UUID driverId,
        ServiceType serviceType,
        TripStatus status,
        double pickupLatitude,
        double pickupLongitude,
        double dropoffLatitude,
        double dropoffLongitude,
        BigDecimal fareAmount,
        String paymentMethod,
        Instant createdAt,
        Instant completedAt
) {}