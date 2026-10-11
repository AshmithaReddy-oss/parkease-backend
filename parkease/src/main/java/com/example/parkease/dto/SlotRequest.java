package com.example.parkease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SlotRequest {

    @NotNull(message = "Parking lot ID is required")
    private Long parkingLotId;

    @NotBlank(message = "Slot number is required")
    @Size(max = 30)
    private String slotNumber;

    public SlotRequest() {
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }
}