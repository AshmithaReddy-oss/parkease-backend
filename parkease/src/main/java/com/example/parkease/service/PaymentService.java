package com.example.parkease.service;

import com.example.parkease.entity.Booking;
import com.example.parkease.entity.Payment;
import com.example.parkease.entity.User;
import com.example.parkease.enums.BookingStatus;
import com.example.parkease.enums.PaymentStatus;
import com.example.parkease.exception.ResourceNotFoundException;
import com.example.parkease.repository.BookingRepository;
import com.example.parkease.repository.PaymentRepository;
import com.example.parkease.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Create a pending payment record for a booking.
    // If one already exists for this booking, return it so the user can retry.
    @Transactional
    public Payment initiatePayment(
            Long bookingId,
            String email) {

        User user = findUser(email);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found: " + bookingId));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot pay for another user's booking.");
        }

        Payment existing = paymentRepository
                .findByBookingUserId(user.getId())
                .stream()
                .filter(p -> p.getBooking().getId().equals(bookingId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            return existing;
        }

        if (booking.getTotalAmount() == null
                || booking.getTotalAmount().signum() <= 0) {
            throw new IllegalStateException(
                    "Booking amount must be greater than zero.");
        }

        Payment payment = new Payment();

        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    // DEMO ONLY: marks the payment as paid and confirms the booking.
    // Replace with a real payment gateway confirmation before going live.
    @Transactional
    public Payment confirmPayment(
            Long paymentId,
            String email,
            String method) {

        Payment payment = findOwnedPayment(paymentId, email);

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a pending payment can be confirmed.");
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentMethod(method);
        payment.setTransactionReference(
                "DEMO-" + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase());
        payment.setPaymentDate(LocalDateTime.now());

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    // Retrieve a payment only if it belongs to the current user
    @Transactional(readOnly = true)
    public Payment getPaymentById(
            Long paymentId,
            String email) {

        return findOwnedPayment(paymentId, email);
    }

    // Retrieve the current user's payment history
    @Transactional(readOnly = true)
    public List<Payment> getMyPayments(String email) {

        User user = findUser(email);

        return paymentRepository
                .findByBookingUserId(user.getId());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."));
    }

    private Payment findOwnedPayment(Long paymentId, String email) {
        User user = findUser(email);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found: " + paymentId));

        if (!payment.getBooking().getUser().getId()
                .equals(user.getId())) {

            throw new AccessDeniedException(
                    "You cannot access another user's payment.");
        }

        return payment;
    }
}