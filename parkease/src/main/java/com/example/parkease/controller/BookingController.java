package com.example.parkease.controller;

import com.example.parkease.dto.BookingRequest;
import com.example.parkease.entity.Booking;
import com.example.parkease.service.BookingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create a booking
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestBody BookingRequest request,
            Authentication authentication) {

        Booking saved = bookingService.createBooking(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    // Get the logged-in user's bookings
    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings(
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.getMyBookings(
                        authentication.getName()
                )
        );
    }

    // Get one booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.getBookingById(
                        id,
                        authentication.getName()
                )
        );
    }

    // Cancel a booking
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(
                        id,
                        authentication.getName()
                )
        );
    }

    // Get all bookings: administrators only
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {

        return ResponseEntity.ok(
                bookingService.getAllBookings()
        );
    }
}