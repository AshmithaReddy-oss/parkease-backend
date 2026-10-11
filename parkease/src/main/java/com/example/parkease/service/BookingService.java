package com.example.parkease.service;

import com.example.parkease.dto.BookingRequest;
import com.example.parkease.entity.Booking;
import com.example.parkease.entity.Slot;
import com.example.parkease.entity.User;
import com.example.parkease.enums.BookingStatus;
import com.example.parkease.enums.LotStatus;
import com.example.parkease.enums.Role;
import com.example.parkease.enums.SlotStatus;
import com.example.parkease.exception.ResourceNotFoundException;
import com.example.parkease.repository.BookingRepository;
import com.example.parkease.repository.SlotRepository;
import com.example.parkease.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            SlotRepository slotRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    // Create a new booking
    @Transactional
    public Booking createBooking(
            BookingRequest request,
            String userEmail) {

        if (request == null || request.getSlotId() == null) {
            throw new IllegalArgumentException(
                    "A valid booking request and slot ID are required."
            );
        }

        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required."
            );
        }

        if (!start.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Booking must start in the future."
            );
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."
                        )
                );

        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parking slot not found."
                        )
                );

        if (slot.getParkingLot().getStatus() != LotStatus.APPROVED) {
            throw new IllegalStateException(
                    "This parking lot is not approved."
            );
        }

        if (slot.getStatus() != SlotStatus.AVAILABLE
                || !slot.isAvailable()) {

            throw new IllegalStateException(
                    "This parking slot is unavailable."
            );
        }

        boolean overlap = bookingRepository.existsOverlappingBooking(
                slot.getId(),
                start,
                end,
                List.of(
                        BookingStatus.PENDING_PAYMENT,
                        BookingStatus.CONFIRMED
                )
        );

        if (overlap) {
            throw new IllegalStateException(
                    "This slot is already booked for the selected time."
            );
        }

        long minutes = Duration.between(start, end).toMinutes();

        if (minutes <= 0) {
            throw new IllegalArgumentException(
                    "Booking duration must be at least one minute."
            );
        }

        // Charge for each started hour.
        long billableHours = (minutes + 59) / 60;

        BigDecimal hourlyRate = BigDecimal.valueOf(
                slot.getParkingLot().getPricePerHour()
        );

        BigDecimal amount = hourlyRate
                .multiply(BigDecimal.valueOf(billableHours))
                .setScale(2, RoundingMode.HALF_UP);

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setSlot(slot);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setTotalAmount(amount);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        return bookingRepository.save(booking);
    }

    // Get bookings belonging to the logged-in user
    public List<Booking> getMyBookings(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."
                        )
                );

        return bookingRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    // Get one booking, allowing its owner or an administrator
    public Booking getBookingById(Long bookingId, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."
                        )
                );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found: " + bookingId
                        )
                );

        boolean isOwner = booking.getUser().getId()
                .equals(user.getId());

        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You cannot access another user's booking."
            );
        }

        return booking;
    }

    // Get all bookings for administrators
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Cancel a booking belonging to the logged-in user
    @Transactional
    public Booking cancelBooking(
            Long bookingId,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."
                        )
                );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found: " + bookingId
                        )
                );

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot cancel another user's booking."
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT
                && booking.getStatus() != BookingStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "This booking cannot be cancelled."
            );
        }

        if (!booking.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "A booking that has already started cannot be cancelled."
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        return bookingRepository.save(booking);
    }
}