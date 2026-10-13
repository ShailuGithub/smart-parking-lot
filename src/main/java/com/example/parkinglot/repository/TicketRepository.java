package com.example.parkinglot.repository;

import com.example.parkinglot.model.Ticket;
import com.example.parkinglot.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByVehicleNumberAndStatus(String vehicleNumber, TicketStatus status);

    boolean existsByVehicleNumberAndStatus(String vehicleNumber, TicketStatus status);
}
