package com.example.pricingservice.service;

import com.example.pricingservice.domain.FareQuote;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PricingService {

    public FareQuote calculateFare(String customerId, Double distanceKm, Double durationMin) {
        // FR-PRI-001..018: Logic tính toán giá
        BigDecimal baseFare = new BigDecimal("20000"); // 20k VND
        BigDecimal perKm = new BigDecimal("10000"); // 10k VND/km
        BigDecimal perMin = new BigDecimal("2000"); // 2k VND/phút
        
        BigDecimal total = baseFare
                .add(perKm.multiply(BigDecimal.valueOf(distanceKm)))
                .add(perMin.multiply(BigDecimal.valueOf(durationMin)));
        
        // Làm tròn (FR-PRI-016: Không float/double)
        total = total.setScale(0, RoundingMode.HALF_UP);
        
        FareQuote quote = new FareQuote();
        quote.setQuoteId(UUID.randomUUID().toString());
        quote.setCustomerId(customerId);
        quote.setAmount(total);
        quote.setSurgeMultiplier(new BigDecimal("1.0"));
        quote.setExpiresAt(System.currentTimeMillis() + 300000); // 5 phút
        quote.setRuleVersion("v1");
        
        return quote;
    }
}
