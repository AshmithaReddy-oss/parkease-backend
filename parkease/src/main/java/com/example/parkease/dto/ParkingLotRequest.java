package com.example.parkease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ParkingLotRequest {

    @NotBlank(message = "Parking lot name is required")
    @Size(max = 120)
    private String name;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    private String location;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Total slots are required")
    @Min(value = 1, message = "At least one slot is required")
    private Integer totalSlots;

    @NotNull(message = "Hourly price is required")
    @DecimalMin(value = "0.01",
            message = "Hourly price must be greater than zero")
    private BigDecimal pricePerHour;

    public ParkingLotRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Integer totalSlots) {
        this.totalSlots = totalSlots;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
}