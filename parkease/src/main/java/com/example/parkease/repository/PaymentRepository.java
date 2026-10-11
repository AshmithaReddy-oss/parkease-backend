package com.example.parkease.repository;

import com.example.parkease.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    boolean existsByBookingId(Long bookingId);

    List<Payment> findByBookingUserId(Long userId);
}