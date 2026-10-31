package com.example.parkinglot.service;

import com.example.parkinglot.dto.CheckInRequest;
import com.example.parkinglot.dto.SpotAvailability;
import com.example.parkinglot.dto.TicketResponse;
import com.example.parkinglot.exception.NoSpotAvailableException;
import com.example.parkinglot.exception.TicketNotFoundException;
import com.example.parkinglot.exception.VehicleAlreadyParkedException;
import com.example.parkinglot.model.ParkingSpot;
import com.example.parkinglot.model.SpotType;
import com.example.parkinglot.model.Ticket;
import com.example.parkinglot.model.TicketStatus;
import com.example.parkinglot.model.VehicleType;
import com.example.parkinglot.repository.ParkingSpotRepository;
import com.example.parkinglot.repository.TicketRepository;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Service
public class ParkingService {

    private static final int MAX_ATTEMPTS = 3;

    private final ParkingSpotRepository spots;
    private final TicketRepository tickets;
    private final FeeCalculator feeCalculator;
    private final Clock clock;
    private final TransactionTemplate tx;

    public ParkingService(ParkingSpotRepository spots, TicketRepository tickets,
                          FeeCalculator feeCalculator, Clock clock,
                          PlatformTransactionManager transactionManager) {
        this.spots = spots;
        this.tickets = tickets;
        this.feeCalculator = feeCalculator;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public TicketResponse checkIn(CheckInRequest request) {
        String vehicleNumber = normalize(request.vehicleNumber());
        return withRetry(() -> tx.execute(status -> doCheckIn(vehicleNumber, request.vehicleType())));
    }

    public TicketResponse checkOut(String vehicleNumber) {
        String normalized = normalize(vehicleNumber);
        return withRetry(() -> tx.execute(status -> doCheckOut(normalized)));
    }

    public TicketResponse getTicket(Long ticketId) {
        return tx.execute(status -> tickets.findById(ticketId)
                .map(TicketResponse::from)
                .orElseThrow(() -> new TicketNotFoundException("Ticket " + ticketId + " not found")));
    }

    public List<SpotAvailability> availability() {
        return tx.execute(status -> spots.availability());
    }

    private TicketResponse doCheckIn(String vehicleNumber, VehicleType vehicleType) {
        if (tickets.existsByVehicleNumberAndStatus(vehicleNumber, TicketStatus.ACTIVE)) {
            throw new VehicleAlreadyParkedException(vehicleNumber);
        }
        ParkingSpot spot = allocate(vehicleType);
        spot.occupy();
        Ticket ticket = tickets.save(new Ticket(vehicleNumber, vehicleType, spot, now()));
        return TicketResponse.from(ticket);
    }

    /** Best fit first: the smallest compatible spot type, then the lowest floor and spot number. */
    private ParkingSpot allocate(VehicleType vehicleType) {
        for (SpotType spotType : vehicleType.compatibleSpots()) {
            List<ParkingSpot> free = spots.findFreeForUpdate(spotType, PageRequest.of(0, 1));
            if (!free.isEmpty()) {
                return free.get(0);
            }
        }
        throw new NoSpotAvailableException(vehicleType);
    }

    private TicketResponse doCheckOut(String vehicleNumber) {
        Ticket ticket = tickets.findByVehicleNumberAndStatus(vehicleNumber, TicketStatus.ACTIVE)
                .orElseThrow(() -> new TicketNotFoundException(
                        "No active ticket for vehicle " + vehicleNumber));
        LocalDateTime exitTime = now();
        BigDecimal fee = feeCalculator.calculate(ticket.getVehicleType(), ticket.getEntryTime(), exitTime);
        ticket.getSpot().release();
        ticket.close(exitTime, fee);
        return TicketResponse.from(ticket);
    }

    /**
     * Concurrent requests that lose a race (optimistic version clash, lock timeout, or a second
     * active ticket for the same vehicle) are retried in a fresh transaction, where they see the
     * up-to-date state.
     */
    private <T> T withRetry(Supplier<T> action) {
        for (int attempt = 1; ; attempt++) {
            try {
                return action.get();
            } catch (ConcurrencyFailureException | DataIntegrityViolationException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static String normalize(String vehicleNumber) {
        return vehicleNumber.trim().toUpperCase();
    }
}
