package com.example.parkinglot.dto;

import com.example.parkinglot.model.SpotType;

public record SpotAvailability(int floor, SpotType spotType, long total, long free) {
}
