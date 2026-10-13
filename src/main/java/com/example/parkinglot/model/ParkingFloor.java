package com.example.parkinglot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_floor")
public class ParkingFloor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "floor_level", nullable = false, unique = true)
    private int level;

    protected ParkingFloor() {
    }

    public ParkingFloor(int level) {
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }
}
