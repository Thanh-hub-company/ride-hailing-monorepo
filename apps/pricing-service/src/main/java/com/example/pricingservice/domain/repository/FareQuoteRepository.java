package com.example.pricingservice.domain.repository;

import com.example.pricingservice.domain.entity.FareQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FareQuoteRepository extends JpaRepository<FareQuote, UUID> {
}
