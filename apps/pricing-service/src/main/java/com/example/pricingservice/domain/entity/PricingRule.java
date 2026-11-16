package com.example.pricingservice.domain.entity;

import com.example.commoncontracts.enums.ServiceType;
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
import java.util.UUID;

@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
@NoArgsConstructor
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ServiceType serviceType; // RIDE, DELIVERY

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFare; // Cước mở cửa (VND)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal perKmRate; // Đơn giá / KM (VND)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal perMinuteRate; // Đơn giá / Phút (VND)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumFare; // Cước tối thiểu (VND)

    @Column(name = "surge_multiplier")
    private Double surgeMultiplier = 1.0;

    public Double getSurgeMultiplier() {
        return surgeMultiplier != null ? surgeMultiplier : 1.0;
    }

    public void setSurgeMultiplier(Double surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }
}