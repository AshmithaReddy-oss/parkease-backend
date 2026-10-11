package com.example.parkease.service;

import com.example.parkease.dto.ParkingLotRequest;
import com.example.parkease.entity.ParkingLot;
import com.example.parkease.entity.User;
import com.example.parkease.enums.LotStatus;
import com.example.parkease.enums.Role;
import com.example.parkease.exception.ResourceNotFoundException;
import com.example.parkease.repository.ParkingLotRepository;
import com.example.parkease.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;

    public ParkingLotService(
            ParkingLotRepository parkingLotRepository,
            UserRepository userRepository) {

        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ParkingLot createParkingLot(
            ParkingLotRequest request,
            String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Owner account not found."
                        )
                );

        if (owner.getRole() != Role.OWNER) {
            throw new IllegalArgumentException(
                    "Only parking lot owners can register lots."
            );
        }

        ParkingLot lot = new ParkingLot();

        lot.setOwner(owner);
        lot.setName(request.getName());
        lot.setLocation(request.getLocation());
        lot.setDescription(request.getDescription());
        lot.setTotalSlots(request.getTotalSlots());
        lot.setPricePerHour(
                request.getPricePerHour().doubleValue()
        );

        lot.setStatus(LotStatus.PENDING);

        return parkingLotRepository.save(lot);
    }

    public List<ParkingLot> getApprovedParkingLots() {

        return parkingLotRepository.findByStatus(
                LotStatus.APPROVED
        );
    }

    public ParkingLot getParkingLotById(Long id) {

        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parking lot not found: " + id
                        )
                );

        if (lot.getStatus() != LotStatus.APPROVED) {
            throw new ResourceNotFoundException(
                    "Approved parking lot not found."
            );
        }

        return lot;
    }

    public List<ParkingLot> searchByLocation(
            String location) {

        if (location == null || location.isBlank()) {
            return getApprovedParkingLots();
        }

        return parkingLotRepository
                .findByLocationContainingIgnoreCaseAndStatus(
                        location.trim(),
                        LotStatus.APPROVED
                );
    }

    public List<ParkingLot> getOwnerParkingLots(
            String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Owner account not found."
                        )
                );

        return parkingLotRepository.findByOwnerId(
                owner.getId()
        );
    }

    @Transactional
    public ParkingLot approveParkingLot(Long id) {

        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parking lot not found: " + id
                        )
                );

        lot.setStatus(LotStatus.APPROVED);

        return parkingLotRepository.save(lot);
    }

    @Transactional
    public ParkingLot rejectParkingLot(Long id) {

        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parking lot not found: " + id
                        )
                );

        lot.setStatus(LotStatus.REJECTED);

        return parkingLotRepository.save(lot);
    }
    // Get all parking lots (admin use)
public List<ParkingLot> getAllParkingLots() {
    return parkingLotRepository.findAll();
}

// Update a parking lot
@Transactional
public ParkingLot updateParkingLot(
        Long id,
        com.example.parkease.dto.ParkingLotRequest request,
        String ownerEmail) {

    ParkingLot lot = parkingLotRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Parking lot not found: " + id));

    User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Owner account not found."));

    if (!lot.getOwner().getId().equals(owner.getId())
            && owner.getRole() != Role.ADMIN) {
        throw new org.springframework.security.access.AccessDeniedException(
                "You cannot update this parking lot.");
    }

    lot.setName(request.getName());
    lot.setLocation(request.getLocation());
    lot.setDescription(request.getDescription());
    lot.setTotalSlots(request.getTotalSlots());
    lot.setPricePerHour(
            request.getPricePerHour().doubleValue());

    return parkingLotRepository.save(lot);
}

// Delete a parking lot
@Transactional
public void deleteParkingLot(Long id, String ownerEmail) {

    ParkingLot lot = parkingLotRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Parking lot not found: " + id));

    User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Owner account not found."));

    if (!lot.getOwner().getId().equals(owner.getId())
            && owner.getRole() != Role.ADMIN) {
        throw new org.springframework.security.access.AccessDeniedException(
                "You cannot delete this parking lot.");
    }

    parkingLotRepository.delete(lot);
}
}