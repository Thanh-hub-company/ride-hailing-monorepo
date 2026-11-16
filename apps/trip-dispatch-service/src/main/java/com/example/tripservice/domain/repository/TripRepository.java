package com.example.tripservice.domain.repository;

import com.example.commoncontracts.enums.TripStatus;
import com.example.tripservice.domain.entity.Trip;
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
public interface TripRepository extends JpaRepository<Trip, UUID> {
    Optional<Trip> findByTripCode(String tripCode);
    List<Trip> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<Trip> findByDriverIdOrderByCreatedAtDesc(UUID driverId);
    boolean existsByCustomerIdAndStatusIn(UUID customerId, List<TripStatus> activeStatuses);
    boolean existsByDriverIdAndStatusIn(UUID driverId, List<TripStatus> activeStatuses);
    Optional<Trip> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.id = :tripId")
    Optional<Trip> findByIdForUpdate(@Param("tripId") UUID tripId);
}