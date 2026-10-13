package com.example.parkinglot.service;

import com.example.parkinglot.dto.CheckInRequest;
import com.example.parkinglot.dto.SpotAvailability;
import com.example.parkinglot.dto.TicketResponse;
import com.example.parkinglot.exception.NoSpotAvailableException;
import com.example.parkinglot.exception.TicketNotFoundException;
import com.example.parkinglot.exception.VehicleAlreadyParkedException;
import com.example.parkinglot.model.SpotType;
import com.example.parkinglot.model.TicketStatus;
import com.example.parkinglot.model.VehicleType;
import com.example.parkinglot.repository.ParkingSpotRepository;
import com.example.parkinglot.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ParkingServiceTest {

    @Autowired
    private ParkingService service;
    @Autowired
    private TicketRepository tickets;
    @Autowired
    private ParkingSpotRepository spots;

    @BeforeEach
    void resetLot() {
        tickets.deleteAll();
        spots.findAll().forEach(s -> {
            s.release();
            spots.save(s);
        });
    }

    @Test
    void motorcycleGetsSmallestSpotOnLowestFloor() {
        TicketResponse ticket = service.checkIn(new CheckInRequest("mh12ab1234", VehicleType.MOTORCYCLE));

        assertThat(ticket.floor()).isEqualTo(1);
        assertThat(ticket.spotNumber()).isEqualTo(1);
        assertThat(ticket.vehicleNumber()).isEqualTo("MH12AB1234");
        assertThat(ticket.status()).isEqualTo(TicketStatus.ACTIVE);
    }

    @Test
    void busOnlyGetsLargeSpot() {
        TicketResponse ticket = service.checkIn(new CheckInRequest("BUS-1", VehicleType.BUS));

        assertThat(ticket.spotNumber()).isGreaterThan(10);
    }

    @Test
    void availabilityDropsOnCheckInAndRecoversOnCheckOut() {
        assertThat(freeSpots(SpotType.MEDIUM)).isEqualTo(18);

        service.checkIn(new CheckInRequest("CAR-1", VehicleType.CAR));
        assertThat(freeSpots(SpotType.MEDIUM)).isEqualTo(17);

        TicketResponse closed = service.checkOut("CAR-1");
        assertThat(closed.status()).isEqualTo(TicketStatus.CLOSED);
        assertThat(closed.exitTime()).isNotNull();
        assertThat(closed.fee()).isEqualByComparingTo("0");
        assertThat(freeSpots(SpotType.MEDIUM)).isEqualTo(18);
    }

    @Test
    void sameVehicleCannotCheckInTwice() {
        service.checkIn(new CheckInRequest("DUP-1", VehicleType.CAR));

        assertThatThrownBy(() -> service.checkIn(new CheckInRequest("dup-1", VehicleType.CAR)))
                .isInstanceOf(VehicleAlreadyParkedException.class);
    }

    @Test
    void checkOutWithoutTicketFails() {
        assertThatThrownBy(() -> service.checkOut("NOPE-1")).isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void concurrentCheckInsNeverShareASpot() throws Exception {
        int largeSpots = 6;
        int attempts = 20;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<TicketResponse>> futures = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            String plate = "BUS-" + i;
            Callable<TicketResponse> task = () -> {
                start.await();
                return service.checkIn(new CheckInRequest(plate, VehicleType.BUS));
            };
            futures.add(pool.submit(task));
        }
        start.countDown();

        List<TicketResponse> parked = new ArrayList<>();
        int rejected = 0;
        for (Future<TicketResponse> future : futures) {
            try {
                parked.add(future.get());
            } catch (java.util.concurrent.ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(NoSpotAvailableException.class);
                rejected++;
            }
        }
        pool.shutdown();

        Set<String> distinctSpots = parked.stream()
                .map(t -> t.floor() + "-" + t.spotNumber())
                .collect(Collectors.toSet());
        assertThat(parked).hasSize(largeSpots);
        assertThat(distinctSpots).hasSize(largeSpots);
        assertThat(rejected).isEqualTo(attempts - largeSpots);
    }

    private long freeSpots(SpotType type) {
        return service.availability().stream()
                .filter(a -> a.spotType() == type)
                .mapToLong(SpotAvailability::free)
                .sum();
    }
}
