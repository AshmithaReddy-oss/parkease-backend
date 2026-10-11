package com.example.parkease.repository;

import com.example.parkease.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByParkingLotId(Long parkingLotId);

    List<Slot> findByParkingLotIdAndAvailableTrue(
            Long parkingLotId);
}