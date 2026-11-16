package com.example.paymentwalletservice.controller;

import com.example.commonsecurity.annotation.CurrentUser;
import com.example.commonsecurity.model.UserPrincipal;
import com.example.paymentwalletservice.dto.WalletBalanceResponse;
import com.example.paymentwalletservice.dto.WalletEntryResponse;
import com.example.paymentwalletservice.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/wallets", "/api/v1/driver/wallet"})
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "driverId", required = false) UUID driverIdParam
    ) {
        UUID driverId = (currentUser != null && currentUser.getUserId() != null)
                ? currentUser.getUserId()
                : driverIdParam;
        if (driverId == null) {
            driverId = UUID.fromString("22222222-2222-2222-2222-222222222201");
        }
        return ResponseEntity.ok(walletService.getWalletBalance(driverId));
    }

    @GetMapping({"/history", "/entries"})
    public ResponseEntity<List<WalletEntryResponse>> getEntries(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "tripId", required = false) UUID tripId,
            @RequestParam(value = "driverId", required = false) UUID driverIdParam
    ) {
        UUID driverId = (currentUser != null && currentUser.getUserId() != null)
                ? currentUser.getUserId()
                : driverIdParam;
        if (driverId == null && tripId == null) {
            driverId = UUID.fromString("22222222-2222-2222-2222-222222222201");
        }
        return ResponseEntity.ok(walletService.getEntries(driverId, tripId));
    }

    @PostMapping("/earnings/process")
    public ResponseEntity<Void> processTripEarning(@RequestParam UUID driverId,
                                                   @RequestParam UUID tripId,
                                                   @RequestParam BigDecimal totalFare) {
        walletService.processTripEarning(driverId, tripId, totalFare);
        return ResponseEntity.ok().build();
    }
}