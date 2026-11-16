package com.example.paymentwalletservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_entries")
@Getter
@Setter
@NoArgsConstructor
public class WalletEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID walletId;

    private UUID tripId;

    @Column(nullable = false)
    private String type; // TRIP_EARNING, PLATFORM_COMMISSION, ADJUSTMENT, REFUND

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String referenceCode;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}