package com.example.paymentwalletservice.controller;

import com.example.commonsecurity.annotation.CurrentUser;
import com.example.commonsecurity.model.UserPrincipal;
import com.example.paymentwalletservice.dto.PaymentResponse;
import com.example.paymentwalletservice.dto.ProcessPaymentRequest;
import com.example.paymentwalletservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@CurrentUser UserPrincipal currentUser,
                                                          @RequestBody(required = false) ProcessPaymentRequest request) {
        if (request == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Request body không được để trống.");
        }
        UUID customerId = currentUser != null ? currentUser.getUserId() : request.customerId();
        if (customerId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Yêu cầu xác thực Bearer token hoặc cung cấp customerId.");
        }
        return ResponseEntity.ok(paymentService.processPayment(customerId, request));
    }

    @GetMapping("/trips/{tripId}")
    public ResponseEntity<PaymentResponse> getPaymentByTripId(@PathVariable UUID tripId) {
        return ResponseEntity.ok(paymentService.getPaymentByTripId(tripId));
    }
}