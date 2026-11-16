package com.example.paymentwalletservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID tripId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String status,
        String providerRef,
        Instant createdAt
) {}