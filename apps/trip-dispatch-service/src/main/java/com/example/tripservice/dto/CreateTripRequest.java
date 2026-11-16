package com.example.tripservice.dto;

import com.example.commoncontracts.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTripRequest(
        @JsonAlias({"quote_id", "quoteId"})
        UUID quoteId,
        @JsonAlias({"service_type", "serviceType"})
        ServiceType serviceType,
        @JsonAlias({"pickup_latitude", "pickupLatitude"})
        double pickupLatitude,
        @JsonAlias({"pickup_longitude", "pickupLongitude"})
        double pickupLongitude,
        @JsonAlias({"dropoff_latitude", "dropoffLatitude"})
        double dropoffLatitude,
        @JsonAlias({"dropoff_longitude", "dropoffLongitude"})
        double dropoffLongitude,
        @JsonAlias({"fare_amount", "fareAmount", "amount", "totalFare"})
        BigDecimal fareAmount,
        @JsonAlias({"idempotency_key", "idempotencyKey"})
        String idempotencyKey,
        @JsonAlias({"payment_method", "paymentMethod"})
        String paymentMethod
) {
    public CreateTripRequest(UUID quoteId, ServiceType serviceType,
                             double pickupLatitude, double pickupLongitude,
                             double dropoffLatitude, double dropoffLongitude,
                             BigDecimal fareAmount, String idempotencyKey) {
        this(quoteId, serviceType, pickupLatitude, pickupLongitude, dropoffLatitude,
                dropoffLongitude, fareAmount, idempotencyKey, "E_WALLET");
    }
}