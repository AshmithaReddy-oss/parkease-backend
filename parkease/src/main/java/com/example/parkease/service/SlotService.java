package com.example.parkease.service;

import com.example.parkease.entity.ParkingLot;
import com.example.parkease.entity.Slot;
import com.example.parkease.entity.User;
import com.example.parkease.enums.LotStatus;
import com.example.parkease.enums.Role;
import com.example.parkease.enums.SlotStatus;
import com.example.parkease.exception.ResourceNotFoundException;
import com.example.parkease.repository.ParkingLotRepository;
import com.example.parkease.repository.SlotRepository;
import com.example.parkease.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;

    public SlotService(
            SlotRepository slotRepository,
            ParkingLotRepository parkingLotRepository,
            UserRepository userRepository) {

        this.slotRepository = slotRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
    }

    // Get all slots
    @Transactional(readOnly = true)
    public List<Slot> getAllSlots() {
        return slotRepository.findAll();
    }

    // Get a slot by ID
    @Transactional(readOnly = true)
    public Slot getSlotById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parking slot not found: " + id));
    }

    // Get slots for a specific parking lot
    @Transactional(readOnly = true)
    public List<Slot> getSlotsByParkingLot(Long parkingLotId) {

        if (!parkingLotRepository.existsById(parkingLotId)) {
            throw new ResourceNotFoundException(
                    "Parking lot not found: " + parkingLotId);
        }

        return slotRepository.findByParkingLotId(parkingLotId);
    }

    // Get available slots in a parking lot
    @Transactional(readOnly = true)
    public List<Slot> getAvailableSlots(Long parkingLotId) {

        ParkingLot lot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parking lot not found: " + parkingLotId));

        if (lot.getStatus() != LotStatus.APPROVED) {
            throw new ResourceNotFoundException(
                    "Approved parking lot not found.");
        }

        return slotRepository
                .findByParkingLotIdAndAvailableTrue(parkingLotId)
                .stream()
                .filter(slot ->
                        slot.getStatus() == SlotStatus.AVAILABLE)
                .toList();
    }

    // Create a slot for an owner's parking lot
    @Transactional
    public Slot createSlot(Slot slot, String ownerEmail) {

        if (slot.getParkingLot() == null
                || slot.getParkingLot().getId() == null) {
            throw new IllegalArgumentException(
                    "Parking lot ID is required.");
        }

        ParkingLot lot = parkingLotRepository
                .findById(slot.getParkingLot().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parking lot not found."));

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."));

        if (owner.getRole() != Role.OWNER
                || !lot.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException(
                    "You cannot add slots to this parking lot.");
        }

        if (slot.getSlotNumber() == null
                || slot.getSlotNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Slot number is required.");
        }

        slot.setParkingLot(lot);
        slot.setSlotNumber(slot.getSlotNumber().trim());
        slot.setAvailable(true);
        slot.setStatus(SlotStatus.AVAILABLE);

        return slotRepository.save(slot);
    }

    // Update a slot
    @Transactional
    public Slot updateSlot(
            Long id,
            Slot updatedSlot,
            String ownerEmail) {

        Slot existing = getSlotById(id);

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."));

        if (owner.getRole() != Role.ADMIN
                && (owner.getRole() != Role.OWNER
                || !existing.getParkingLot().getOwner()
                        .getId().equals(owner.getId()))) {

            throw new AccessDeniedException(
                    "You cannot update this parking slot.");
        }

        if (updatedSlot.getSlotNumber() != null
                && !updatedSlot.getSlotNumber().isBlank()) {
            existing.setSlotNumber(
                    updatedSlot.getSlotNumber().trim());
        }

        // Do not allow the client to move a slot to a
        // different parking lot through this method.

        return slotRepository.save(existing);
    }

    // Delete a slot
    @Transactional
    public void deleteSlot(Long id, String ownerEmail) {

        Slot slot = getSlotById(id);

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found."));

        if (owner.getRole() != Role.ADMIN
                && (owner.getRole() != Role.OWNER
                || !slot.getParkingLot().getOwner()
                        .getId().equals(owner.getId()))) {

            throw new AccessDeniedException(
                    "You cannot delete this parking slot.");
        }

        slotRepository.delete(slot);
    }
}