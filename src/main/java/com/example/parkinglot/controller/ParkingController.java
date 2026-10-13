package com.example.parkinglot.controller;

import com.example.parkinglot.dto.CheckInRequest;
import com.example.parkinglot.dto.CheckOutRequest;
import com.example.parkinglot.dto.SpotAvailability;
import com.example.parkinglot.dto.TicketResponse;
import com.example.parkinglot.service.ParkingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse checkIn(@Valid @RequestBody CheckInRequest request) {
        return parkingService.checkIn(request);
    }

    @PostMapping("/check-out")
    public TicketResponse checkOut(@Valid @RequestBody CheckOutRequest request) {
        return parkingService.checkOut(request.vehicleNumber());
    }

    @GetMapping("/tickets/{ticketId}")
    public TicketResponse getTicket(@PathVariable Long ticketId) {
        return parkingService.getTicket(ticketId);
    }

    @GetMapping("/availability")
    public List<SpotAvailability> availability() {
        return parkingService.availability();
    }
}
