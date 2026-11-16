package com.example.tripservice.service;

import com.example.commoncontracts.enums.OfferStatus;
import com.example.tripservice.domain.entity.DriverOffer;
import com.example.tripservice.domain.entity.Trip;
import com.example.tripservice.domain.repository.DriverOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MatchingService {

    private final DriverOfferRepository driverOfferRepository;

    public MatchingService(DriverOfferRepository driverOfferRepository) {
        this.driverOfferRepository = driverOfferRepository;
    }

    @Transactional
    public DriverOffer createOfferForDriver(Trip trip, UUID candidateDriverId) {
        DriverOffer offer = new DriverOffer();
        offer.setTripId(trip.getId());
        offer.setDriverId(candidateDriverId);
        offer.setStatus(OfferStatus.PENDING);
        offer.setOfferedAt(Instant.now());
        offer.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));

        return driverOfferRepository.save(offer);
    }
}
