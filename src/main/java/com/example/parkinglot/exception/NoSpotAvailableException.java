package com.example.parkinglot.exception;

import com.example.parkinglot.model.VehicleType;

public class NoSpotAvailableException extends RuntimeException {
    public NoSpotAvailableException(VehicleType type) {
        super("No parking spot available for vehicle type " + type);
    }
}
