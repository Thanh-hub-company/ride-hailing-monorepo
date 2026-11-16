package com.example.tripservice.domain.entity;

import com.example.commoncontracts.enums.ServiceType;
import com.example.commoncontracts.enums.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "trips", uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        name = "uk_trips_customer_idempotency", columnNames = {"customer_id", "idempotency_key"}))
@Getter
@Setter
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String tripCode;

    @Column(nullable = false)
    private UUID customerId;

    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.CREATED;

    @Column(nullable = false)
    private double pickupLatitude;

    @Column(nullable = false)
    private double pickupLongitude;

    @Column(nullable = false)
    private double dropoffLatitude;

    @Column(nullable = false)
    private double dropoffLongitude;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fareAmount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private UUID quoteId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @jakarta.persistence.Version
    private long version;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant completedAt;
}