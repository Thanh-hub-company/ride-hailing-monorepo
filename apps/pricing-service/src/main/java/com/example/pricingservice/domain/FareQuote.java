package com.example.pricingservice.domain;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareQuote {
    private String quoteId;
    private String customerId;
    private BigDecimal amount;
    private BigDecimal surgeMultiplier;
    private Long expiresAt;
    private String ruleVersion;
}
