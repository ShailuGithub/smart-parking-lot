package com.example.parkinglot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket",
        indexes = @Index(name = "idx_ticket_vehicle_status", columnList = "vehicle_number, status"),
        uniqueConstraints = @UniqueConstraint(name = "uk_ticket_active_vehicle", columnNames = "active_vehicle"))
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", nullable = false, length = 20)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    /** Plate number while the ticket is ACTIVE, null once closed; the unique constraint on it stops a vehicle holding two active tickets. */
    @Column(name = "active_vehicle", length = 20)
    private String activeVehicle;

    protected Ticket() {
    }

    public Ticket(String vehicleNumber, VehicleType vehicleType, ParkingSpot spot, LocalDateTime entryTime) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.spot = spot;
        this.entryTime = entryTime;
        this.status = TicketStatus.ACTIVE;
        this.activeVehicle = vehicleNumber;
    }

    public void close(LocalDateTime exitTime, BigDecimal fee) {
        this.exitTime = exitTime;
        this.fee = fee;
        this.status = TicketStatus.CLOSED;
        this.activeVehicle = null;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public TicketStatus getStatus() {
        return status;
    }
}
