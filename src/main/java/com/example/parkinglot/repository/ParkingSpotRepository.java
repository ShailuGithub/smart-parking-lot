package com.example.parkinglot.repository;

import com.example.parkinglot.dto.SpotAvailability;
import com.example.parkinglot.model.ParkingSpot;
import com.example.parkinglot.model.SpotType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    /**
     * Lowest floor / lowest number free spot of the given type, locked for update so that
     * concurrent check-ins cannot be handed the same spot. Orders by the floor id (floors are
     * created bottom-up) rather than joining the floor table, so only spot rows get locked.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSpot s "
            + "where s.type = :type and s.occupied = false order by s.floor.id, s.spotNumber")
    List<ParkingSpot> findFreeForUpdate(@Param("type") SpotType type, Pageable pageable);

    @Query("select new com.example.parkinglot.dto.SpotAvailability(f.level, s.type, count(s), "
            + "sum(case when s.occupied = false then 1L else 0L end)) "
            + "from ParkingSpot s join s.floor f group by f.level, s.type order by f.level, s.type")
    List<SpotAvailability> availability();
}
