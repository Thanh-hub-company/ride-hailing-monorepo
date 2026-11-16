package com.example.tripservice.domain.repository;

import com.example.commoncontracts.enums.OfferStatus;
import com.example.tripservice.domain.entity.DriverOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverOfferRepository extends JpaRepository<DriverOffer, UUID> {
    List<DriverOffer> findByTripId(UUID tripId);
    Optional<DriverOffer> findByTripIdAndDriverId(UUID tripId, UUID driverId);
    List<DriverOffer> findByDriverIdAndStatus(UUID driverId, OfferStatus status);
    List<DriverOffer> findByDriverIdAndStatusOrderByExpiresAtAsc(UUID driverId, OfferStatus status);
    List<DriverOffer> findByTripIdAndStatus(UUID tripId, OfferStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from DriverOffer o where o.id = :offerId")
    Optional<DriverOffer> findByIdForUpdate(@Param("offerId") UUID offerId);
}