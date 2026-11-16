package com.example.paymentwalletservice;

import com.example.paymentwalletservice.dto.ProcessPaymentRequest;
import com.example.paymentwalletservice.service.PaymentService;
import com.example.paymentwalletservice.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:wallet-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class PaymentServiceTests {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WalletService walletService;

    @Test
    void testProcessPaymentSuccess() {
        UUID customerId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        ProcessPaymentRequest request = new ProcessPaymentRequest(tripId, new BigDecimal("25000.00"), "CASH");

        var response = paymentService.processPayment(customerId, request);

        assertNotNull(response);
        assertEquals(tripId, response.tripId());
        assertEquals(customerId, response.customerId());
        assertEquals("SUCCEEDED", response.status());
        assertEquals("CASH", response.paymentMethod());
        assertNotNull(response.providerRef());
    }

    @Test
    void testProcessPaymentIdempotent() {
        UUID customerId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        ProcessPaymentRequest request = new ProcessPaymentRequest(tripId, new BigDecimal("50000.00"), "E_WALLET");

        var response1 = paymentService.processPayment(customerId, request);
        var response2 = paymentService.processPayment(customerId, request);

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.paymentId(), response2.paymentId());
    }

    @Test
    void testProcessPaymentMissingTripId() {
        UUID customerId = UUID.randomUUID();
        ProcessPaymentRequest request = new ProcessPaymentRequest(null, new BigDecimal("50000.00"), "E_WALLET");

        assertThrows(ResponseStatusException.class, () -> paymentService.processPayment(customerId, request));
    }

    @Test
    void testProcessPaymentWithNullAmount() {
        UUID customerId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        ProcessPaymentRequest request = new ProcessPaymentRequest(tripId, null, null);

        var response = paymentService.processPayment(customerId, request);

        assertNotNull(response);
        assertEquals(tripId, response.tripId());
        assertEquals(new BigDecimal("25000.00"), response.amount());
        assertEquals("CASH", response.paymentMethod());
        assertEquals("SUCCEEDED", response.status());
    }

    @Test
    void testWalletEntriesAfterPayment() {
        UUID customerId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        ProcessPaymentRequest request = new ProcessPaymentRequest(tripId, new BigDecimal("30000.00"), "CASH");

        paymentService.processPayment(customerId, request);

        var entries = walletService.getEntries(null, tripId);
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(2, entries.size()); // TRIP_EARNING và PLATFORM_COMMISSION
    }
}
