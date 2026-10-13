package com.example.parkinglot.dto;

import com.example.parkinglot.model.Ticket;
import com.example.parkinglot.model.TicketStatus;
import com.example.parkinglot.model.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Long ticketId,
        String vehicleNumber,
        VehicleType vehicleType,
        int floor,
        int spotNumber,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        BigDecimal fee,
        TicketStatus status) {

    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getVehicleNumber(),
                t.getVehicleType(),
                t.getSpot().getFloor().getLevel(),
                t.getSpot().getSpotNumber(),
                t.getEntryTime(),
                t.getExitTime(),
                t.getFee(),
                t.getStatus());
    }
}
