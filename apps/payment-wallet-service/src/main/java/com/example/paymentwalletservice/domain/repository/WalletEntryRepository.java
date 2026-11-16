package com.example.paymentwalletservice.domain.repository;

import com.example.paymentwalletservice.domain.entity.WalletEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface WalletEntryRepository extends JpaRepository<WalletEntry, UUID> {
    List<WalletEntry> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
    List<WalletEntry> findByTripIdOrderByCreatedAtDesc(UUID tripId);
    boolean existsByTripIdAndType(UUID tripId, String type);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM WalletEntry e WHERE e.walletId = :walletId")
    BigDecimal calculateBalanceByWalletId(UUID walletId);
}
