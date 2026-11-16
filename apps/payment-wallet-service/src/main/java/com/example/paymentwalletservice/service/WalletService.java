package com.example.paymentwalletservice.service;

import com.example.paymentwalletservice.domain.entity.Payment;
import com.example.paymentwalletservice.domain.entity.Wallet;
import com.example.paymentwalletservice.domain.entity.WalletEntry;
import com.example.paymentwalletservice.domain.repository.PaymentRepository;
import com.example.paymentwalletservice.domain.repository.WalletEntryRepository;
import com.example.paymentwalletservice.domain.repository.WalletRepository;
import com.example.paymentwalletservice.dto.WalletBalanceResponse;
import com.example.paymentwalletservice.dto.WalletEntryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.20"); // Hoa hồng nền tảng 20%
    private static final UUID DEFAULT_DRIVER_ID = UUID.fromString("22222222-2222-2222-2222-222222222201");

    private final WalletRepository walletRepository;
    private final WalletEntryRepository walletEntryRepository;
    private final PaymentRepository paymentRepository;

    public WalletService(WalletRepository walletRepository,
                         WalletEntryRepository walletEntryRepository,
                         PaymentRepository paymentRepository) {
        this.walletRepository = walletRepository;
        this.walletEntryRepository = walletEntryRepository;
        this.paymentRepository = paymentRepository;
    }

    public Wallet getOrCreateWallet(UUID driverId) {
        UUID effectiveDriverId = driverId != null ? driverId : DEFAULT_DRIVER_ID;
        return walletRepository.findByDriverId(effectiveDriverId)
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setDriverId(effectiveDriverId);
                    return walletRepository.save(wallet);
                });
    }

    public WalletBalanceResponse getWalletBalance(UUID driverId) {
        Wallet wallet = getOrCreateWallet(driverId);
        BigDecimal balance = walletEntryRepository.calculateBalanceByWalletId(wallet.getId());
        return new WalletBalanceResponse(wallet.getId(), wallet.getDriverId(), balance, wallet.getCurrency());
    }

    /**
     * Bút toán phân bổ thu nhập & hoa hồng sau khi chốt chuyến đi (Idempotent)
     */
    @Transactional
    public void processTripEarning(UUID driverId, UUID tripId, BigDecimal totalFare) {
        if (tripId == null) return;
        if (walletEntryRepository.existsByTripIdAndType(tripId, "TRIP_EARNING")) {
            return; // Đã xử lý trước đó, bỏ qua để đảm bảo tính Idempotency
        }

        UUID effectiveDriverId = driverId != null ? driverId : DEFAULT_DRIVER_ID;
        BigDecimal fare = (totalFare != null && totalFare.compareTo(BigDecimal.ZERO) > 0)
                ? totalFare
                : new BigDecimal("25000.00");

        Wallet wallet = getOrCreateWallet(effectiveDriverId);

        // 1. Ghi nhận cộng tổng cước vào ví
        WalletEntry earningEntry = new WalletEntry();
        earningEntry.setWalletId(wallet.getId());
        earningEntry.setTripId(tripId);
        earningEntry.setType("TRIP_EARNING");
        earningEntry.setAmount(fare);
        earningEntry.setReferenceCode("EARN-" + tripId.toString().substring(0, 8).toUpperCase());
        walletEntryRepository.save(earningEntry);

        // 2. Bút toán trừ hoa hồng 20%
        BigDecimal commission = fare.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        WalletEntry commissionEntry = new WalletEntry();
        commissionEntry.setWalletId(wallet.getId());
        commissionEntry.setTripId(tripId);
        commissionEntry.setType("PLATFORM_COMMISSION");
        commissionEntry.setAmount(commission.negate()); // Số tiền âm biểu thị khoản trừ
        commissionEntry.setReferenceCode("COMM-" + tripId.toString().substring(0, 8).toUpperCase());
        walletEntryRepository.save(commissionEntry);
    }

    public List<WalletEntryResponse> getWalletHistory(UUID driverId) {
        if (driverId == null) {
            driverId = DEFAULT_DRIVER_ID;
        }
        Wallet wallet = getOrCreateWallet(driverId);
        return walletEntryRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public List<WalletEntryResponse> getEntries(UUID driverId, UUID tripId) {
        if (tripId != null) {
            List<WalletEntry> entries = walletEntryRepository.findByTripIdOrderByCreatedAtDesc(tripId);
            if (entries.isEmpty()) {
                // Tự động phân bổ thu nhập nếu chuyến này đã có thanh toán hoặc vừa hoàn thành
                BigDecimal fare = paymentRepository.findByTripId(tripId)
                        .map(Payment::getAmount)
                        .orElse(new BigDecimal("25000.00"));
                processTripEarning(driverId != null ? driverId : DEFAULT_DRIVER_ID, tripId, fare);
                entries = walletEntryRepository.findByTripIdOrderByCreatedAtDesc(tripId);
            }
            return entries.stream().map(this::mapToResponse).toList();
        }
        if (driverId != null) {
            return getWalletHistory(driverId);
        }
        return walletEntryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private WalletEntryResponse mapToResponse(WalletEntry e) {
        return new WalletEntryResponse(
                e.getId(),
                e.getWalletId(),
                e.getTripId(),
                e.getType(),
                e.getAmount(),
                e.getReferenceCode(),
                e.getCreatedAt()
        );
    }
}