# Smart Parking Lot System

Low-level design and a working Spring Boot backend for a multi-floor parking lot: automatic spot
allocation by vehicle size, check-in / check-out, fee calculation and real-time availability.

Stack: Java 17, Spring Boot 4.1, Spring Data JPA, H2 (in-memory), JUnit 5.

## Run

```bash
./mvnw spring-boot:run      # starts on http://localhost:8080
./mvnw test                 # unit + concurrency tests
```

On first start a 3-floor lot is seeded: per floor 4 small, 6 medium and 2 large spots.
The H2 console is at `/h2-console` (JDBC URL `jdbc:h2:mem:parkingdb`, user `sa`).

## API

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/parking/check-in` | Allocate a spot and open a ticket |
| POST | `/api/parking/check-out` | Close the vehicle's active ticket, free the spot, compute the fee |
| GET | `/api/parking/tickets/{id}` | Look up a ticket |
| GET | `/api/parking/availability` | Total / free spots per floor and spot type |

```bash
curl -X POST localhost:8080/api/parking/check-in \
  -H 'Content-Type: application/json' \
  -d '{"vehicleNumber":"MH12AB1234","vehicleType":"CAR"}'

curl -X POST localhost:8080/api/parking/check-out \
  -H 'Content-Type: application/json' \
  -d '{"vehicleNumber":"MH12AB1234"}'
```

Errors use RFC 7807 problem details: `409` when the lot is full or the vehicle is already parked,
`404` when no active ticket exists, `400` for invalid input.

## Data model

```
parking_floor (id PK, floor_level UNIQUE)
      1
      |
      N
parking_spot  (id PK, floor_id FK, spot_number, spot_type, occupied, version)
              UNIQUE (floor_id, spot_number)   INDEX (spot_type, occupied)
      1
      |
      N
ticket        (id PK, vehicle_number, vehicle_type, spot_id FK,
               entry_time, exit_time, fee, status, active_vehicle)
              INDEX (vehicle_number, status)   UNIQUE (active_vehicle)
```

A vehicle is not stored as its own table: only its plate number and type matter for a parking
session, so they live on the ticket. A ticket is `ACTIVE` until check-out, then `CLOSED`.
`active_vehicle` holds the plate while the ticket is active and is null once closed; its unique
constraint guarantees a vehicle can never hold two active tickets, even under concurrent check-ins.

## Spot allocation

Spot types are `SMALL`, `MEDIUM`, `LARGE`. Each vehicle type lists the spot types it may use,
best fit first:

| Vehicle | Spot types tried, in order |
|---------|----------------------------|
| Motorcycle | SMALL, MEDIUM, LARGE |
| Car | MEDIUM, LARGE |
| Bus | LARGE |

For each spot type the lowest floor and lowest spot number that is free is chosen, so small
vehicles do not use up large spots unless nothing smaller is left. If nothing fits the request
is rejected with `409`. Lookup is served by the `(spot_type, occupied)` index.

## Fee calculation

`FeeCalculator` is an interface; `HourlyFeeCalculator` is the implementation:

- stays up to the grace period (15 min) are free;
- otherwise fee = hourly rate for the vehicle type x number of started hours (minimum 1).

Rates and grace period are configured in `application.properties` (`parking.fee.*`), so a different
pricing rule can be added by providing another `FeeCalculator`.

## Concurrency

- Allocation reads the candidate spot with a **pessimistic write lock** (`SELECT ... FOR UPDATE`),
  so two simultaneous check-ins are serialised on the same spot and the second one moves on to the
  next free spot.
- `ParkingSpot` also carries a `@Version` column as an optimistic safety net, which also protects
  against two simultaneous check-outs of the same vehicle.
- Each check-in / check-out runs in its own transaction; a request that loses a race
  (`ConcurrencyFailureException`, or a unique-constraint clash on `active_vehicle`) is retried up to
  3 times in a fresh transaction. If it still loses, the client gets `409`, not a server error.
- `ParkingServiceTest#concurrentCheckInsNeverShareASpot` fires 20 buses at 6 large spots at once and
  asserts exactly 6 succeed with distinct spots and the other 14 get `409`.

## Project layout

```
controller/   REST endpoints
service/      ParkingService (allocation, check-in/out), FeeCalculator + HourlyFeeCalculator
repository/   Spring Data JPA repositories (locking query, availability aggregate)
model/        JPA entities and enums
dto/          request / response records
exception/    domain exceptions and global handler
config/       fee properties, clock, initial lot seeder
```
