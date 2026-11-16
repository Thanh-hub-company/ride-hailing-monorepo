package com.example.tripservice.service;

import com.example.commoncontracts.dto.FareQuoteDto;
import com.example.commoncontracts.enums.OfferStatus;
import com.example.commoncontracts.enums.ServiceType;
import com.example.commoncontracts.enums.TripStatus;
import com.example.tripservice.client.PricingClient;
import com.example.tripservice.client.DriverDirectoryClient;
import com.example.tripservice.domain.entity.DriverOffer;
import com.example.tripservice.domain.entity.Trip;
import com.example.tripservice.domain.repository.DriverOfferRepository;
import com.example.tripservice.domain.repository.TripRepository;
import com.example.tripservice.dto.CreateTripRequest;
import com.example.tripservice.dto.TripResponse;
import com.example.tripservice.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceCreateTripTests {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID QUOTE_ID = UUID.randomUUID();
    private static final BigDecimal FARE = new BigDecimal("55000.00");

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DriverOfferRepository driverOfferRepository;

    @Mock
    private PricingClient pricingClient;

    @Mock
    private DriverDirectoryClient driverDirectoryClient;

    @Mock
    private MatchingService matchingService;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripService(tripRepository, driverOfferRepository, pricingClient,
                driverDirectoryClient, matchingService);
    }

    @Test
    void createsTripWithMatchingQuoteAndIdempotencyKey() {
        CreateTripRequest request = request("trip-create-001", FARE);
        when(tripRepository.findByCustomerIdAndIdempotencyKey(CUSTOMER_ID, "trip-create-001"))
                .thenReturn(Optional.empty());
        when(pricingClient.getQuote(QUOTE_ID)).thenReturn(validQuote());
        when(tripRepository.existsByCustomerIdAndStatusIn(CUSTOMER_ID, activeStatuses())).thenReturn(false);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverDirectoryClient.getAvailableDriverIds()).thenReturn(List.of());

        TripResponse response = tripService.createTrip(CUSTOMER_ID, request);

        assertEquals(CUSTOMER_ID, response.customerId());
        assertEquals(ServiceType.RIDE, response.serviceType());
        assertEquals(TripStatus.MATCHING, response.status());
        assertEquals(FARE, response.fareAmount());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createsPendingOffersForAvailableDrivers() {
        CreateTripRequest request = request("trip-match-001", FARE);
        UUID driverId = UUID.randomUUID();
        when(tripRepository.findByCustomerIdAndIdempotencyKey(CUSTOMER_ID, "trip-match-001"))
                .thenReturn(Optional.empty());
        when(pricingClient.getQuote(QUOTE_ID)).thenReturn(validQuote());
        when(tripRepository.existsByCustomerIdAndStatusIn(CUSTOMER_ID, activeStatuses())).thenReturn(false);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.setId(UUID.randomUUID());
            return trip;
        });
        when(driverDirectoryClient.getAvailableDriverIds()).thenReturn(List.of(driverId));

        tripService.createTrip(CUSTOMER_ID, request);

        verify(matchingService).createOfferForDriver(any(Trip.class), org.mockito.ArgumentMatchers.eq(driverId));
    }

    @Test
    void returnsExistingTripWhenSameIdempotencyKeyAndPayloadIsReplayed() {
        CreateTripRequest request = request("trip-replay-001", FARE);
        Trip existingTrip = existingTrip(request);
        when(tripRepository.findByCustomerIdAndIdempotencyKey(CUSTOMER_ID, "trip-replay-001"))
                .thenReturn(Optional.of(existingTrip));

        TripResponse response = tripService.createTrip(CUSTOMER_ID, request);

        assertEquals(existingTrip.getId(), response.tripId());
        assertEquals(existingTrip.getTripCode(), response.tripCode());
        verify(pricingClient, never()).getQuote(any());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void rejectsMissingIdempotencyKey() {
        CreateTripRequest request = request(null, FARE);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(CUSTOMER_ID, request)
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals(400, exception.getStatus().value());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void rejectsFareThatDoesNotMatchQuote() {
        CreateTripRequest request = request("trip-invalid-fare-001", new BigDecimal("60000.00"));
        when(tripRepository.findByCustomerIdAndIdempotencyKey(CUSTOMER_ID, "trip-invalid-fare-001"))
                .thenReturn(Optional.empty());
        when(pricingClient.getQuote(QUOTE_ID)).thenReturn(validQuote());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(CUSTOMER_ID, request)
        );

        assertEquals("BUSINESS_RULE_VIOLATION", exception.getCode());
        assertEquals(422, exception.getStatus().value());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void rejectsSameIdempotencyKeyWithDifferentPayload() {
        CreateTripRequest original = request("trip-reused-001", FARE);
        CreateTripRequest changed = request("trip-reused-001", new BigDecimal("60000.00"));
        Trip existingTrip = existingTrip(original);
        when(tripRepository.findByCustomerIdAndIdempotencyKey(CUSTOMER_ID, "trip-reused-001"))
                .thenReturn(Optional.of(existingTrip));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(CUSTOMER_ID, changed)
        );

        assertEquals("IDEMPOTENCY_KEY_REUSED", exception.getCode());
        assertEquals(409, exception.getStatus().value());
        verify(pricingClient, never()).getQuote(any());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void rejectsNewTripWhenCustomerHasActiveTrip() {
        CreateTripRequest request = request("trip-active-conflict-001", FARE);
        when(tripRepository.findByCustomerIdAndIdempotencyKey(CUSTOMER_ID, "trip-active-conflict-001"))
                .thenReturn(Optional.empty());
        when(pricingClient.getQuote(QUOTE_ID)).thenReturn(validQuote());
        when(tripRepository.existsByCustomerIdAndStatusIn(CUSTOMER_ID, activeStatuses())).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(CUSTOMER_ID, request)
        );

        assertEquals("BUSINESS_RULE_VIOLATION", exception.getCode());
        assertEquals(409, exception.getStatus().value());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void acceptsPendingOfferAndRejectsOtherOffersForTheTrip() {
        UUID tripId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        DriverOffer acceptedOffer = offer(tripId, driverId, OfferStatus.PENDING);
        DriverOffer otherOffer = offer(tripId, UUID.randomUUID(), OfferStatus.PENDING);
        Trip matchingTrip = existingTrip(request("trip-offer-001", FARE));
        matchingTrip.setId(tripId);
        when(driverOfferRepository.findByIdForUpdate(acceptedOffer.getId())).thenReturn(Optional.of(acceptedOffer));
        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(matchingTrip));
        when(driverOfferRepository.findByTripIdAndStatus(tripId, OfferStatus.PENDING))
                .thenReturn(List.of(acceptedOffer, otherOffer));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverOfferRepository.save(any(DriverOffer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripResponse response = tripService.acceptOffer(acceptedOffer.getId(), driverId);

        assertEquals(TripStatus.ACCEPTED, response.status());
        assertEquals(driverId, response.driverId());
        assertEquals(OfferStatus.ACCEPTED, acceptedOffer.getStatus());
        assertEquals(OfferStatus.REJECTED, otherOffer.getStatus());
        verify(tripRepository).save(matchingTrip);
    }

    @Test
    void rejectsSecondAcceptOfAlreadyProcessedOffer() {
        UUID tripId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        DriverOffer acceptedOffer = offer(tripId, driverId, OfferStatus.ACCEPTED);
        when(driverOfferRepository.findByIdForUpdate(acceptedOffer.getId())).thenReturn(Optional.of(acceptedOffer));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.acceptOffer(acceptedOffer.getId(), driverId)
        );

        assertEquals("DRIVER_ALREADY_ASSIGNED", exception.getCode());
        assertEquals(409, exception.getStatus().value());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    private CreateTripRequest request(String idempotencyKey, BigDecimal fareAmount) {
        return new CreateTripRequest(
                QUOTE_ID,
                ServiceType.RIDE,
                10.7769,
                106.7009,
                10.7800,
                106.7050,
                fareAmount,
                idempotencyKey
        );
    }

    private FareQuoteDto validQuote() {
        return new FareQuoteDto(
                QUOTE_ID,
                CUSTOMER_ID,
                ServiceType.RIDE,
                FARE,
                1.0,
                Instant.now().plusSeconds(3600)
        );
    }

    private Trip existingTrip(CreateTripRequest request) {
        Trip trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setTripCode("TRIP-REPLAY");
        trip.setCustomerId(CUSTOMER_ID);
        trip.setServiceType(request.serviceType());
        trip.setStatus(TripStatus.MATCHING);
        trip.setPickupLatitude(request.pickupLatitude());
        trip.setPickupLongitude(request.pickupLongitude());
        trip.setDropoffLatitude(request.dropoffLatitude());
        trip.setDropoffLongitude(request.dropoffLongitude());
        trip.setFareAmount(request.fareAmount());
        trip.setPaymentMethod(request.paymentMethod());
        trip.setQuoteId(request.quoteId());
        trip.setIdempotencyKey(request.idempotencyKey());
        return trip;
    }

    private DriverOffer offer(UUID tripId, UUID driverId, OfferStatus status) {
        DriverOffer offer = new DriverOffer();
        offer.setId(UUID.randomUUID());
        offer.setTripId(tripId);
        offer.setDriverId(driverId);
        offer.setStatus(status);
        offer.setExpiresAt(Instant.now().plusSeconds(30));
        return offer;
    }

    private List<TripStatus> activeStatuses() {
        return List.of(
                TripStatus.CREATED,
                TripStatus.MATCHING,
                TripStatus.ACCEPTED,
                TripStatus.PICKING_UP,
                TripStatus.ARRIVED,
                TripStatus.IN_TRIP
        );
    }
}
