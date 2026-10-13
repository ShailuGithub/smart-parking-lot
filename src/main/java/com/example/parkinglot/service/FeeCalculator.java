package com.example.parkinglot.service;

import com.example.parkinglot.model.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FeeCalculator {

    BigDecimal calculate(VehicleType vehicleType, LocalDateTime entryTime, LocalDateTime exitTime);
}
