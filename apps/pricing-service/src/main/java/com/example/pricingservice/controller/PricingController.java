package com.example.pricingservice.controller;

import com.example.pricingservice.dto.FareCalculateRequest;
import com.example.pricingservice.dto.FareQuoteResponse;
import com.example.pricingservice.service.PricingCalculatorService;
import com.example.commoncontracts.dto.FareQuoteDto;
import com.example.commonsecurity.annotation.CurrentUser;
import com.example.commonsecurity.model.UserPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/pricing", "/api/v1/quotes"})
public class PricingController {

    private final PricingCalculatorService pricingCalculatorService;

    public PricingController(PricingCalculatorService pricingCalculatorService) {
        this.pricingCalculatorService = pricingCalculatorService;
    }

    @PostMapping({"/quote", "/quotes", ""})
    public ResponseEntity<FareQuoteResponse> getFareQuote(@CurrentUser UserPrincipal currentUser,
                                                          @RequestBody FareCalculateRequest request) {
        UUID customerId = currentUser == null ? null : currentUser.getUserId();
        return ResponseEntity.ok(pricingCalculatorService.calculateFareQuote(request, customerId));
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<FareQuoteDto> getQuote(@PathVariable UUID quoteId) {
        FareQuoteDto quote = pricingCalculatorService.getQuote(quoteId);
        return quote == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(quote);
    }
}