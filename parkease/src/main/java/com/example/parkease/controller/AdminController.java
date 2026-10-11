package com.example.parkease.controller;

import com.example.parkease.entity.Booking;
import com.example.parkease.entity.ParkingLot;
import com.example.parkease.entity.Slot;
import com.example.parkease.entity.User;
import com.example.parkease.enums.LotStatus;
import com.example.parkease.repository.ParkingLotRepository;
import com.example.parkease.repository.SlotRepository;
import com.example.parkease.repository.UserRepository;
import com.example.parkease.service.BookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final ParkingLotRepository parkingLotRepository;
    private final SlotRepository slotRepository;

    public AdminController(
            UserRepository userRepository,
            BookingService bookingService,
            ParkingLotRepository parkingLotRepository,
            SlotRepository slotRepository) {

        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.parkingLotRepository = parkingLotRepository;
        this.slotRepository = slotRepository;
    }

    // View all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // View all bookings
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // View all parking facilities (including pending ones)
    @GetMapping("/parking-lots")
    public ResponseEntity<List<Map<String, Object>>> getAllParkingLots() {
        List<Map<String, Object>> result = parkingLotRepository.findAll()
                .stream()
                .map(lot -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", lot.getId());
                    item.put("name", lot.getName());
                    item.put("location", lot.getLocation());
                    item.put("description", lot.getDescription());
                    item.put("totalSlots", lot.getTotalSlots());
                    item.put("pricePerHour", lot.getPricePerHour());
                    item.put("status", lot.getStatus());
                    return item;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // Approve a parking facility and create its slots (S1, S2, ...)
    @PutMapping("/parking-lots/{id}/approve")
    @Transactional
    public ResponseEntity<String> approveParkingLot(@PathVariable Long id) {
        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parking lot not found"));

        lot.setStatus(LotStatus.APPROVED);
        parkingLotRepository.save(lot);

        if (slotRepository.findByParkingLotId(id).isEmpty()
                && lot.getTotalSlots() != null) {

            List<Slot> slots = new ArrayList<>();

            for (int i = 1; i <= lot.getTotalSlots(); i++) {
                Slot slot = new Slot();
                slot.setParkingLot(lot);
                slot.setSlotNumber("S" + i);
                slots.add(slot);
            }

            slotRepository.saveAll(slots);
        }

        return ResponseEntity.ok("Approved");
    }
}