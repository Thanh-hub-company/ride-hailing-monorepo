package com.example.pricingservice.service;

import com.example.commoncontracts.enums.ServiceType;
import com.example.pricingservice.domain.entity.PricingRule;
import com.example.pricingservice.domain.repository.PricingRuleRepository;
import com.example.pricingservice.domain.repository.FareQuoteRepository;
import com.example.pricingservice.domain.entity.FareQuote;
import com.example.pricingservice.dto.FareCalculateRequest;
import com.example.pricingservice.dto.FareQuoteResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import com.example.commoncontracts.dto.FareQuoteDto;

@Service
public class PricingCalculatorService {

    private final PricingRuleRepository pricingRuleRepository;
    private final FareQuoteRepository fareQuoteRepository;

    public PricingCalculatorService(PricingRuleRepository pricingRuleRepository,
                                    FareQuoteRepository fareQuoteRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.fareQuoteRepository = fareQuoteRepository;
    }

    /**
     * Tính toán cước phí chuyến đi dựa theo công thức:
     * Total = Max(MinimumFare, BaseFare + (Distance * PerKm) + (Duration * PerMinute)) * SurgeMultiplier
     */
    public FareQuoteResponse calculateFareQuote(FareCalculateRequest request, UUID customerId) {
        ServiceType type = request.serviceType() != null ? request.serviceType() : ServiceType.RIDE;

        PricingRule rule = pricingRuleRepository.findByServiceType(type)
                .orElseGet(() -> getDefaultPricingRule(type));

        double distance = request.distanceKm() != null && request.distanceKm() > 0
                ? request.distanceKm()
                : calculateDistanceKm(
                        request.getEffectivePickupLat(), request.getEffectivePickupLng(),
                        request.getEffectiveDropoffLat(), request.getEffectiveDropoffLng()
                );
        if (distance <= 0) {
            distance = 3.0; // Khoảng cách giả định mặc định nếu không có tọa độ cụ thể
        }

        double duration = request.estimatedDurationMinutes() != null && request.estimatedDurationMinutes() > 0
                ? request.estimatedDurationMinutes()
                : Math.max(5.0, distance * 2.5); // Ước lượng 2.5 phút/km

        BigDecimal distanceCost = BigDecimal.valueOf(distance)
                .multiply(rule.getPerKmRate());

        BigDecimal durationCost = BigDecimal.valueOf(duration)
                .multiply(rule.getPerMinuteRate());

        BigDecimal rawFare = rule.getBaseFare()
                .add(distanceCost)
                .add(durationCost);

        if (rawFare.compareTo(rule.getMinimumFare()) < 0) {
            rawFare = rule.getMinimumFare();
        }

        // Lấy hệ số surge từ DB (mặc định 1.0 nếu null)
        double surgeMultiplier = rule.getSurgeMultiplier() != null ? rule.getSurgeMultiplier() : 1.0;
        BigDecimal totalFare = rawFare.multiply(BigDecimal.valueOf(surgeMultiplier))
                .setScale(-2, RoundingMode.HALF_UP); // Làm tròn đến hàng trăm đồng

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(24, ChronoUnit.HOURS);
        UUID quoteId = UUID.randomUUID();
        FareQuote quote = new FareQuote();
        quote.setId(quoteId);
        quote.setCustomerId(customerId);
        quote.setServiceType(type);
        quote.setPickupLatitude(request.getEffectivePickupLat());
        quote.setPickupLongitude(request.getEffectivePickupLng());
        quote.setDropoffLatitude(request.getEffectiveDropoffLat());
        quote.setDropoffLongitude(request.getEffectiveDropoffLng());
        quote.setDistanceKm(distance);
        quote.setEstimatedDurationMinutes(duration);
        quote.setFareAmount(totalFare);
        quote.setSurgeMultiplier(surgeMultiplier);
        quote.setExpiresAt(expiresAt);
        quote.setCreatedAt(createdAt);
        quote.setCurrency("VND");
        quote.setRuleVersion("DEFAULT-1");
        fareQuoteRepository.save(quote);

        return new FareQuoteResponse(
                quoteId,
                type,
                totalFare,
                surgeMultiplier,
                distance,
                duration,
                expiresAt,
                rule.getBaseFare(),
                totalFare,
                "VND",
                "DEFAULT-1"
        );
    }

    public FareQuoteDto getQuote(UUID quoteId) {
        return fareQuoteRepository.findById(quoteId)
                .map(quote -> new FareQuoteDto(
                        quote.getId(), quote.getCustomerId(), quote.getServiceType(), quote.getFareAmount(),
                        quote.getSurgeMultiplier(), quote.getExpiresAt()))
                .orElse(null);
    }

    public FareQuoteResponse calculateFareQuote(FareCalculateRequest request) {
        return calculateFareQuote(request, null);
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 && lon1 == 0 && lat2 == 0 && lon2 == 0) return 0.0;
        final int R = 6371; // Bán kính trái đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0;
    }

    private double calculateSurgeMultiplier(double lat, double lng) {
        // Có thể mở rộng tích hợp Redis Geo / ML Model tính toán theo mật độ nhu cầu
        return 1.0;
    }

    private PricingRule getDefaultPricingRule(ServiceType serviceType) {
        PricingRule rule = new PricingRule();
        rule.setServiceType(serviceType);
        rule.setBaseFare(new BigDecimal("15000.00"));
        rule.setPerKmRate(new BigDecimal("10000.00"));
        rule.setPerMinuteRate(new BigDecimal("800.00"));
        rule.setMinimumFare(new BigDecimal("20000.00"));
        return rule;
    }
}