package com.example.parkinglot.config;

import com.example.parkinglot.model.ParkingFloor;
import com.example.parkinglot.model.ParkingSpot;
import com.example.parkinglot.model.SpotType;
import com.example.parkinglot.repository.ParkingFloorRepository;
import com.example.parkinglot.repository.ParkingSpotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Creates a 3-floor lot on first start: per floor 4 small, 6 medium and 2 large spots. */
@Component
public class ParkingLotSeeder implements CommandLineRunner {

    private static final int FLOORS = 3;
    private static final int SMALL = 4;
    private static final int MEDIUM = 6;
    private static final int LARGE = 2;

    private final ParkingFloorRepository floors;
    private final ParkingSpotRepository spots;

    public ParkingLotSeeder(ParkingFloorRepository floors, ParkingSpotRepository spots) {
        this.floors = floors;
        this.spots = spots;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (floors.count() > 0) {
            return;
        }
        for (int level = 1; level <= FLOORS; level++) {
            ParkingFloor floor = floors.save(new ParkingFloor(level));
            List<ParkingSpot> batch = new ArrayList<>();
            int number = 1;
            for (int i = 0; i < SMALL; i++) {
                batch.add(new ParkingSpot(floor, number++, SpotType.SMALL));
            }
            for (int i = 0; i < MEDIUM; i++) {
                batch.add(new ParkingSpot(floor, number++, SpotType.MEDIUM));
            }
            for (int i = 0; i < LARGE; i++) {
                batch.add(new ParkingSpot(floor, number++, SpotType.LARGE));
            }
            spots.saveAll(batch);
        }
    }
}
