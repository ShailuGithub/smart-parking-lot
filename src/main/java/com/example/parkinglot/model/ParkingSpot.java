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
import jakarta.persistence.Version;

@Entity
@Table(name = "parking_spot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"floor_id", "spot_number"}),
        indexes = @Index(name = "idx_spot_type_occupied", columnList = "spot_type, occupied"))
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id")
    private ParkingFloor floor;

    @Column(name = "spot_number", nullable = false)
    private int spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "spot_type", nullable = false)
    private SpotType type;

    @Column(nullable = false)
    private boolean occupied;

    /** Optimistic-lock safety net on top of the pessimistic lock used during allocation. */
    @Version
    private long version;

    protected ParkingSpot() {
    }

    public ParkingSpot(ParkingFloor floor, int spotNumber, SpotType type) {
        this.floor = floor;
        this.spotNumber = spotNumber;
        this.type = type;
    }

    public void occupy() {
        if (occupied) {
            throw new IllegalStateException("Spot " + spotNumber + " is already occupied");
        }
        this.occupied = true;
    }

    public void release() {
        this.occupied = false;
    }

    public Long getId() {
        return id;
    }

    public ParkingFloor getFloor() {
        return floor;
    }

    public int getSpotNumber() {
        return spotNumber;
    }

    public SpotType getType() {
        return type;
    }

    public boolean isOccupied() {
        return occupied;
    }
}
