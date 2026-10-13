package com.example.parkinglot.service;

import com.example.parkinglot.config.FeeProperties;
import com.example.parkinglot.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HourlyFeeCalculatorTest {

    private final HourlyFeeCalculator calculator = new HourlyFeeCalculator(new FeeProperties(15, Map.of(
            VehicleType.MOTORCYCLE, new BigDecimal("10"),
            VehicleType.CAR, new BigDecimal("20"),
            VehicleType.BUS, new BigDecimal("50"))));

    private final LocalDateTime entry = LocalDateTime.of(2026, 10, 13, 9, 0);

    @Test
    void freeWithinGracePeriod() {
        assertThat(calculator.calculate(VehicleType.CAR, entry, entry.plusMinutes(15))).isEqualByComparingTo("0");
    }

    @Test
    void chargesOneHourJustAfterGrace() {
        assertThat(calculator.calculate(VehicleType.CAR, entry, entry.plusMinutes(16))).isEqualByComparingTo("20");
    }

    @Test
    void roundsUpToNextStartedHour() {
        assertThat(calculator.calculate(VehicleType.CAR, entry, entry.plusMinutes(61))).isEqualByComparingTo("40");
        assertThat(calculator.calculate(VehicleType.CAR, entry, entry.plusMinutes(120))).isEqualByComparingTo("40");
    }

    @Test
    void rateDependsOnVehicleType() {
        LocalDateTime exit = entry.plusHours(3);
        assertThat(calculator.calculate(VehicleType.MOTORCYCLE, entry, exit)).isEqualByComparingTo("30");
        assertThat(calculator.calculate(VehicleType.BUS, entry, exit)).isEqualByComparingTo("150");
    }
}
