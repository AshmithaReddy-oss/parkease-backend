package com.example.parkease.controller;

import com.example.parkease.dto.ParkingLotRequest;
import com.example.parkease.entity.ParkingLot;
import com.example.parkease.service.ParkingLotService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(
            ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    // List approved parking lots for users
    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAllParkingLots() {
        return ResponseEntity.ok(
                parkingLotService.getAllParkingLots());
    }

    // Search approved lots by location
    @GetMapping("/search")
    public ResponseEntity<List<ParkingLot>> searchParkingLots(
            @RequestParam(required = false) String location) {

        return ResponseEntity.ok(
                parkingLotService.searchByLocation(location));
    }

    // Get a parking lot by ID
    @GetMapping("/{id}")
    public ResponseEntity<ParkingLot> getParkingLotById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                parkingLotService.getParkingLotById(id));
    }

    // Register a parking lot
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ParkingLot> createParkingLot(
            @Valid @RequestBody ParkingLotRequest request,
            Authentication authentication) {

        ParkingLot lot = parkingLotService.createParkingLot(
                request,
                authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(lot);
    }

    // List lots belonging to the authenticated owner
    @GetMapping("/owner/my")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ParkingLot>> getOwnerParkingLots(
            Authentication authentication) {

        return ResponseEntity.ok(
                parkingLotService.getOwnerParkingLots(
                        authentication.getName()));
    }

    // Update a parking lot
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ParkingLot> updateParkingLot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingLotRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                parkingLotService.updateParkingLot(
                        id, request, authentication.getName()));
    }

    // Delete a parking lot
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<Void> deleteParkingLot(
            @PathVariable Long id,
            Authentication authentication) {

        parkingLotService.deleteParkingLot(
                id, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}