package com.example.parkease.repository;

import com.example.parkease.entity.ParkingLot;
import com.example.parkease.enums.LotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingLotRepository
        extends JpaRepository<ParkingLot, Long> {

    List<ParkingLot> findByStatus(LotStatus status);

    List<ParkingLot> findByOwnerId(Long ownerId);

    List<ParkingLot> findByLocationContainingIgnoreCaseAndStatus(
            String location,
            LotStatus status
    );
}