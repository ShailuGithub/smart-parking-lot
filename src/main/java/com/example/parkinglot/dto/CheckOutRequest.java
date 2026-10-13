package com.example.parkinglot.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckOutRequest(@NotBlank String vehicleNumber) {
}
