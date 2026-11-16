package com.example.paymentwalletservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletEntryResponse(
        UUID entryId,
        UUID walletId,
        UUID tripId,
        String type,
        BigDecimal amount,
        String referenceCode,
        Instant createdAt
) {}
