package com.example.pricingservice.domain.repository;

import com.example.commoncontracts.enums.ServiceType;
import com.example.pricingservice.domain.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {
    Optional<PricingRule> findByServiceType(ServiceType serviceType);
}