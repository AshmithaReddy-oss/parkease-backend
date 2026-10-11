package com.example.parkease.controller;

import com.example.parkease.entity.Payment;
import com.example.parkease.service.PaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Initiate payment for a booking
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<Payment> initiatePayment(
            @PathVariable Long bookingId,
            Authentication authentication) {

        Payment payment = paymentService.initiatePayment(
                bookingId, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payment);
    }

    // DEMO ONLY: confirm a payment and the related booking
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Payment> confirmPayment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "UPI") String method,
            Authentication authentication) {

        return ResponseEntity.ok(
                paymentService.confirmPayment(
                        id, authentication.getName(), method));
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(
                        id, authentication.getName()));
    }

    // Get current user's payment history
    @GetMapping("/my")
    public ResponseEntity<List<Payment>> getMyPayments(
            Authentication authentication) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(
                        authentication.getName()));
    }
}