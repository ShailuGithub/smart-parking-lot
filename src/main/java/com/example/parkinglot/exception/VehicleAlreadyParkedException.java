package com.example.parkinglot.exception;

public class VehicleAlreadyParkedException extends RuntimeException {
    public VehicleAlreadyParkedException(String vehicleNumber) {
        super("Vehicle " + vehicleNumber + " is already parked");
    }
}
