package com.example.paymentwalletservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletBalanceResponse(
        UUID walletId,
        UUID driverId,
        BigDecimal balance,
        String currency
) {}