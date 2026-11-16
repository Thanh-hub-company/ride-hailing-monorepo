package com.example.tripservice.controller;

import com.example.commoncontracts.enums.OfferStatus;
import com.example.commonsecurity.annotation.CurrentUser;
import com.example.commonsecurity.model.UserPrincipal;
import com.example.tripservice.domain.entity.DriverOffer;
import com.example.tripservice.domain.repository.DriverOfferRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/driver/offers")
public class DriverOfferController {

    private final DriverOfferRepository driverOfferRepository;

    public DriverOfferController(DriverOfferRepository driverOfferRepository) {
        this.driverOfferRepository = driverOfferRepository;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<DriverOffer>> pendingOffers(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(driverOfferRepository.findByDriverIdAndStatusOrderByExpiresAtAsc(
                currentUser.getUserId(), OfferStatus.PENDING));
    }
}
