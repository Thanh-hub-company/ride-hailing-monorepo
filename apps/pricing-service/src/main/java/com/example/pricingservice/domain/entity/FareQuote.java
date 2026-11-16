package com.example.pricingservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.commoncontracts.enums.ServiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fare_quotes")
@Getter
@Setter
@NoArgsConstructor
public class FareQuote {
    @Id
    private UUID id;
    
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    
    private double pickupLatitude;
    private double pickupLongitude;
    private double dropoffLatitude;
    private double dropoffLongitude;
    
    private double distanceKm;
    private double estimatedDurationMinutes;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal fareAmount;
    
    private double surgeMultiplier;
    
    private Instant expiresAt;
    private Instant createdAt;
    
    private String currency;
    private String ruleVersion;
}
