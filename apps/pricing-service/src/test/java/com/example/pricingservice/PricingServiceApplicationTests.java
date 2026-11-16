package com.example.pricingservice;

import com.example.commoncontracts.enums.ServiceType;
import com.example.pricingservice.domain.entity.PricingRule;
import com.example.pricingservice.domain.repository.PricingRuleRepository;
import com.example.pricingservice.dto.FareCalculateRequest;
import com.example.pricingservice.dto.FareQuoteResponse;
import com.example.pricingservice.service.PricingCalculatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pricing-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc(addFilters = false)
class PricingServiceApplicationTests {

    @Autowired
    private PricingCalculatorService pricingCalculatorService;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        assertNotNull(pricingCalculatorService);
    }

    @Test
    void testCalculateFareWithCoordinates() {
        FareCalculateRequest request = new FareCalculateRequest(
                null,
                null,
                null,
                10.7769,
                106.7009,
                10.7800,
                106.7050,
                null,
                null,
                "Ben Thanh",
                "Notre Dame"
        );
        FareQuoteResponse response = pricingCalculatorService.calculateFareQuote(request);
        assertNotNull(response.quoteId());
        assertTrue(response.totalFare().doubleValue() >= 20000.0);
    }

    @Test
    void testPricingEndpointsQuoteAndQuotes() throws Exception {
        String jsonPayload = """
                {
                  "serviceType": "RIDE",
                  "pickup": {
                    "latitude": 10.7769,
                    "longitude": 106.7009
                  },
                  "destination": {
                    "latitude": 10.7800,
                    "longitude": 106.7050
                  }
                }
                """;

        // Test POST /api/v1/pricing/quotes
        mockMvc.perform(post("/api/v1/pricing/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").isNotEmpty())
                .andExpect(jsonPath("$.totalFare").isNumber());

        // Test POST /api/v1/pricing/quote
        mockMvc.perform(post("/api/v1/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").isNotEmpty());
    }

    @Test
    void testSurgeMultiplierCalculation() {
        PricingRule rule = new PricingRule();
        rule.setServiceType(ServiceType.DELIVERY);
        rule.setBaseFare(new BigDecimal("10000.00"));
        rule.setPerKmRate(new BigDecimal("5000.00"));
        rule.setPerMinuteRate(new BigDecimal("500.00"));
        rule.setMinimumFare(new BigDecimal("15000.00"));
        rule.setSurgeMultiplier(1.5);
        pricingRuleRepository.save(rule);

        FareCalculateRequest request = new FareCalculateRequest(
                ServiceType.DELIVERY,
                10.0,
                20.0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        FareQuoteResponse response = pricingCalculatorService.calculateFareQuote(request);
        assertEquals(1.5, response.surgeMultiplier());
        // rawFare = 10000 + (10 * 5000) + (20 * 500) = 10000 + 50000 + 10000 = 70000.
        // totalFare = 70000 * 1.5 = 105000.
        assertEquals(105000.0, response.totalFare().doubleValue(), 0.01);
    }
}
