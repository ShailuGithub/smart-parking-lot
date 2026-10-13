package com.example.parkinglot.model;

import java.util.List;

public enum VehicleType {
    MOTORCYCLE(List.of(SpotType.SMALL, SpotType.MEDIUM, SpotType.LARGE)),
    CAR(List.of(SpotType.MEDIUM, SpotType.LARGE)),
    BUS(List.of(SpotType.LARGE));

    private final List<SpotType> compatibleSpots;

    VehicleType(List<SpotType> compatibleSpots) {
        this.compatibleSpots = compatibleSpots;
    }

    /** Spot types this vehicle can use, ordered from best fit to largest. */
    public List<SpotType> compatibleSpots() {
        return compatibleSpots;
    }
}
