package com.example.parkinglot.dto;

import com.example.parkinglot.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckInRequest(
        @NotBlank @Size(max = 20) String vehicleNumber,
        @NotNull VehicleType vehicleType) {
}
