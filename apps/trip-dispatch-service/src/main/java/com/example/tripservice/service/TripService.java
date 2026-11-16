package com.example.tripservice.service;

import com.example.commoncontracts.enums.OfferStatus;
import com.example.commoncontracts.enums.TripStatus;
import com.example.tripservice.domain.entity.DriverOffer;
import com.example.tripservice.domain.entity.Trip;
import com.example.tripservice.domain.repository.DriverOfferRepository;
import com.example.tripservice.domain.repository.TripRepository;
import com.example.tripservice.client.PricingClient;
import com.example.tripservice.client.DriverDirectoryClient;
import com.example.tripservice.dto.CreateTripRequest;
import com.example.tripservice.dto.TripResponse;
import com.example.commoncontracts.dto.FareQuoteDto;
import com.example.tripservice.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.Map;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final DriverOfferRepository driverOfferRepository;
    private final PricingClient pricingClient;
    private final DriverDirectoryClient driverDirectoryClient;
    private final MatchingService matchingService;

    public TripService(TripRepository tripRepository, DriverOfferRepository driverOfferRepository,
                       PricingClient pricingClient, DriverDirectoryClient driverDirectoryClient,
                       MatchingService matchingService) {
        this.tripRepository = tripRepository;
        this.driverOfferRepository = driverOfferRepository;
        this.pricingClient = pricingClient;
        this.driverDirectoryClient = driverDirectoryClient;
        this.matchingService = matchingService;
    }
    @Transactional
    public TripResponse createTrip(UUID customerId, CreateTripRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Body tạo chuyến không được để trống.");
        }
        if (request.quoteId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Thiếu quoteId.", Map.of("field", "quoteId"));
        }
        if (request.serviceType() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Thiếu serviceType.", Map.of("field", "serviceType"));
        }
        String idempotencyKey = request.idempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Thiếu Idempotency-Key.");
        }
        if (request.paymentMethod() == null || request.paymentMethod().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Thiếu paymentMethod.",
                    Map.of("field", "paymentMethod", "allowedExample", "ELECTRONIC_WALLET"));
        }
        Trip previousTrip = tripRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey).orElse(null);
        if (previousTrip != null) {
            if (!samePayload(previousTrip, request)) {
                throw new BusinessException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                        "Idempotency-Key đã được sử dụng cho dữ liệu khác.");
            }
            return mapToResponse(previousTrip);
        }
        FareQuoteDto quote = pricingClient.getQuote(request.quoteId());
        if (quote == null) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION",
                    "Không tìm thấy báo giá với quoteId đã cung cấp.",
                    Map.of("field", "quoteId", "quoteId", String.valueOf(request.quoteId()),
                            "reason", "QUOTE_NOT_FOUND"));
        }
        Instant now = Instant.now();
        if (quote.expiresAt() == null || !quote.expiresAt().isAfter(now)) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "QUOTE_EXPIRED",
                    "Báo giá đã hết hạn hoặc không có thời điểm hết hạn UTC hợp lệ.",
                    Map.of("field", "expiresAt", "quoteId", String.valueOf(request.quoteId()),
                            "expiresAt", String.valueOf(quote.expiresAt()), "now", now.toString()));
        }
        if (quote.customerId() != null && !quote.customerId().equals(customerId)) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION",
                    "Báo giá không thuộc về khách hàng đang đăng nhập.",
                    Map.of("field", "customerId", "quoteCustomerId", String.valueOf(quote.customerId()),
                            "requestCustomerId", String.valueOf(customerId), "reason", "QUOTE_OWNER_MISMATCH"));
        }
        if (request.serviceType() != quote.serviceType()) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION",
                    "Loại dịch vụ của chuyến không khớp với báo giá.",
                    Map.of("field", "serviceType", "requested", String.valueOf(request.serviceType()),
                            "quoted", String.valueOf(quote.serviceType())));
        }
        var effectiveFareAmount = request.fareAmount() == null ? quote.amount() : request.fareAmount();
        if (effectiveFareAmount.compareTo(quote.amount()) != 0) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION",
                    "Số tiền chuyến đi không khớp với báo giá.",
                    Map.of("field", "fareAmount", "requested", String.valueOf(effectiveFareAmount),
                            "quoted", String.valueOf(quote.amount())));
        }
        validateCoordinate(request.pickupLatitude(), request.pickupLongitude(), "pickup");
        validateCoordinate(request.dropoffLatitude(), request.dropoffLongitude(), "dropoff");
        List<TripStatus> activeStatuses = List.of(
                TripStatus.CREATED, TripStatus.MATCHING, TripStatus.ACCEPTED,
                TripStatus.PICKING_UP, TripStatus.ARRIVED, TripStatus.IN_TRIP
        );
        if (tripRepository.existsByCustomerIdAndStatusIn(customerId, activeStatuses)) {
            throw new BusinessException(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION",
                    "Khách hàng đang có một chuyến đi chưa hoàn tất.",
                    Map.of("customerId", customerId.toString(), "activeStatuses", activeStatuses));
        }


        Trip trip = new Trip();
        trip.setTripCode("TRIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        trip.setCustomerId(customerId);
        trip.setServiceType(request.serviceType());
        trip.setStatus(TripStatus.MATCHING);
        trip.setPickupLatitude(request.pickupLatitude());
        trip.setPickupLongitude(request.pickupLongitude());
        trip.setDropoffLatitude(request.dropoffLatitude());
        trip.setDropoffLongitude(request.dropoffLongitude());
        trip.setFareAmount(effectiveFareAmount);
        trip.setPaymentMethod(request.paymentMethod());
        trip.setQuoteId(request.quoteId());
        trip.setIdempotencyKey(idempotencyKey);

        Trip savedTrip = tripRepository.save(trip);
        driverDirectoryClient.getAvailableDriverIds().stream()
                .map(driverId -> matchingService.createOfferForDriver(savedTrip, driverId))
                .toList();
        return mapToResponse(savedTrip);
    }

    private void validateCoordinate(double latitude, double longitude, String field) {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180
                || (latitude == 0 && longitude == 0)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Tọa độ " + field + " không hợp lệ.",
                    Map.of("field", field, "latitude", latitude, "longitude", longitude));
        }
    }

    private boolean samePayload(Trip trip, CreateTripRequest request) {
        return Objects.equals(trip.getQuoteId(), request.quoteId())
                && trip.getServiceType() == request.serviceType()
                && Objects.equals(trip.getFareAmount(), request.fareAmount())
                && Objects.equals(trip.getPaymentMethod(), request.paymentMethod())
                && trip.getPickupLatitude() == request.pickupLatitude()
                && trip.getPickupLongitude() == request.pickupLongitude()
                && trip.getDropoffLatitude() == request.dropoffLatitude()
                && trip.getDropoffLongitude() == request.dropoffLongitude();
    }

    @Transactional
    public TripResponse acceptOffer(UUID offerId, UUID driverId) {
        DriverOffer offer = driverOfferRepository.findByIdForUpdate(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin offer"));

        if (!offer.getDriverId().equals(driverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer này không thuộc về tài xế");
        }

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessException(HttpStatus.CONFLICT, "DRIVER_ALREADY_ASSIGNED",
                    "Offer đã được tài xế xử lý hoặc chuyến đã có tài xế.");
        }

        if (offer.getExpiresAt().isBefore(Instant.now())) {
            offer.setStatus(OfferStatus.EXPIRED);
            driverOfferRepository.save(offer);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Offer đã hết hiệu lực");
        }

        Trip trip = tripRepository.findByIdForUpdate(offer.getTripId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyến đi"));

        if (trip.getStatus() != TripStatus.MATCHING) {
            offer.setStatus(OfferStatus.REJECTED);
            driverOfferRepository.save(offer);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chuyến đi đã được tài xế khác tiếp nhận");
        }

        if (trip.getDriverId() != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "DRIVER_ALREADY_ASSIGNED",
                    "Chuyến đi đã được tài xế khác nhận.");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        driverOfferRepository.save(offer);
        driverOfferRepository.findByTripIdAndStatus(trip.getId(), OfferStatus.PENDING)
                .stream()
                .filter(other -> !other.getId().equals(offer.getId()))
                .forEach(other -> {
                    other.setStatus(OfferStatus.REJECTED);
                    driverOfferRepository.save(other);
                });

        trip.setDriverId(driverId);
        trip.setStatus(TripStatus.ACCEPTED);
        Trip updatedTrip = tripRepository.save(trip);

        return mapToResponse(updatedTrip);
    }

    @Transactional
    public TripResponse updateTripStatus(UUID tripId, UUID actorId, TripStatus targetStatus) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyến đi"));

        validateStateTransition(trip.getStatus(), targetStatus);

        trip.setStatus(targetStatus);
        if (targetStatus == TripStatus.COMPLETED) {
            trip.setCompletedAt(Instant.now());
        }

        return mapToResponse(tripRepository.save(trip));
    }

    public TripResponse getTripById(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyến đi"));
        return mapToResponse(trip);
    }

    private void validateStateTransition(TripStatus current, TripStatus target) {
        boolean valid = switch (current) {
            case CREATED -> target == TripStatus.MATCHING || target == TripStatus.CANCELLED;
            case MATCHING -> target == TripStatus.ACCEPTED || target == TripStatus.NO_DRIVER || target == TripStatus.CANCELLED;
            case ACCEPTED -> target == TripStatus.PICKING_UP || target == TripStatus.CANCELLED;
            case PICKING_UP -> target == TripStatus.ARRIVED || target == TripStatus.CANCELLED;
            case ARRIVED -> target == TripStatus.IN_TRIP || target == TripStatus.CANCELLED;
            case IN_TRIP -> target == TripStatus.COMPLETED || target == TripStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể chuyển trạng thái chuyến đi từ " + current + " sang " + target);
        }
    }

    private TripResponse mapToResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getTripCode(),
                trip.getCustomerId(),
                trip.getDriverId(),
                trip.getServiceType(),
                trip.getStatus(),
                trip.getPickupLatitude(),
                trip.getPickupLongitude(),
                trip.getDropoffLatitude(),
                trip.getDropoffLongitude(),
                trip.getFareAmount(),
                trip.getPaymentMethod(),
                trip.getCreatedAt(),
                trip.getCompletedAt()
        );
    }
}