package com.example.parkease.controller;

import com.example.parkease.entity.Slot;
import com.example.parkease.service.SlotService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    // Get all slots
    @GetMapping
    public ResponseEntity<List<Slot>> getAllSlots() {
        return ResponseEntity.ok(
                slotService.getAllSlots());
    }

    // Get slots for a parking lot
    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<Slot>> getSlotsByParkingLot(
            @PathVariable Long parkingLotId) {

        return ResponseEntity.ok(
                slotService.getSlotsByParkingLot(parkingLotId));
    }

    // Get available slots
    @GetMapping("/available")
    public ResponseEntity<List<Slot>> getAvailableSlots(
            @RequestParam Long parkingLotId) {

        return ResponseEntity.ok(
                slotService.getAvailableSlots(parkingLotId));
    }

    // Get a slot by ID
    @GetMapping("/{id}")
    public ResponseEntity<Slot> getSlotById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                slotService.getSlotById(id));
    }

    // Create a slot
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Slot> createSlot(
            @RequestBody Slot slot,
            Authentication authentication) {

        Slot saved = slotService.createSlot(
                slot,
                authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    // Update a slot
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<Slot> updateSlot(
            @PathVariable Long id,
            @RequestBody Slot slot,
            Authentication authentication) {

        return ResponseEntity.ok(
                slotService.updateSlot(
                        id,
                        slot,
                        authentication.getName()));
    }

    // Delete a slot
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long id,
            Authentication authentication) {

        slotService.deleteSlot(
                id,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }
}