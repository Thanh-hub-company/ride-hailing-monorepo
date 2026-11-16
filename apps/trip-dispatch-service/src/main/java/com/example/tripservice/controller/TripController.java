package com.example.tripservice.controller;

import com.example.commonsecurity.annotation.CurrentUser;
import com.example.commonsecurity.model.UserPrincipal;
import com.example.tripservice.dto.AcceptOfferRequest;
import com.example.tripservice.dto.CreateTripRequest;
import com.example.tripservice.dto.TripResponse;
import com.example.tripservice.dto.UpdateTripStatusRequest;
import com.example.tripservice.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;
import com.example.tripservice.domain.entity.DriverOffer;
import com.example.commoncontracts.enums.OfferStatus;
import com.example.tripservice.domain.repository.DriverOfferRepository;
import com.example.tripservice.exception.BusinessException;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService tripService;
    private final DriverOfferRepository driverOfferRepository;

    public TripController(TripService tripService, DriverOfferRepository driverOfferRepository) {
        this.tripService = tripService;
        this.driverOfferRepository = driverOfferRepository;
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@CurrentUser UserPrincipal currentUser,
                                                   @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                   @RequestBody CreateTripRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Body tạo chuyến không được để trống.");
        }
        String key = idempotencyKey != null ? idempotencyKey : request.idempotencyKey();
        CreateTripRequest normalized = new CreateTripRequest(request.quoteId(), request.serviceType(),
                request.pickupLatitude(), request.pickupLongitude(), request.dropoffLatitude(),
                request.dropoffLongitude(), request.fareAmount(), key, request.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.createTrip(currentUser.getUserId(), normalized));
    }

    @PostMapping("/offers/accept")
    public ResponseEntity<TripResponse> acceptOffer(@CurrentUser UserPrincipal currentUser,
                                                    @RequestBody AcceptOfferRequest request) {
        return ResponseEntity.ok(tripService.acceptOffer(request.offerId(), currentUser.getUserId()));
    }

    @GetMapping({"/driver/offers/pending", "/offers/pending"})
    public ResponseEntity<List<DriverOffer>> pendingOffers(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(driverOfferRepository.findByDriverIdAndStatusOrderByExpiresAtAsc(
                currentUser.getUserId(), OfferStatus.PENDING));
    }

    @PostMapping({"/driver/offers/{offerId}/accept", "/offers/{offerId}/accept"})
    public ResponseEntity<TripResponse> acceptOfferByPath(@PathVariable UUID offerId,
                                                           @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(tripService.acceptOffer(offerId, currentUser.getUserId()));
    }

    @PutMapping("/{tripId}/status")
    public ResponseEntity<TripResponse> updateStatus(@PathVariable UUID tripId,
                                                     @CurrentUser UserPrincipal currentUser,
                                                     @RequestBody UpdateTripStatusRequest request) {
        if (request == null || request.targetStatus() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Trạng thái đích (targetStatus hoặc status) không được để trống.");
        }
        return ResponseEntity.ok(tripService.updateTripStatus(tripId, currentUser.getUserId(), request.targetStatus()));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable UUID tripId) {
        return ResponseEntity.ok(tripService.getTripById(tripId));
    }
}
