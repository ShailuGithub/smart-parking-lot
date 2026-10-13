package com.example.parkinglot.service;

import com.example.parkinglot.config.FeeProperties;
import com.example.parkinglot.model.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Free within the grace period; otherwise the hourly rate for the vehicle type multiplied by
 * the number of started hours (minimum one hour).
 */
@Component
public class HourlyFeeCalculator implements FeeCalculator {

    private final FeeProperties properties;

    public HourlyFeeCalculator(FeeProperties properties) {
        this.properties = properties;
    }

    @Override
    public BigDecimal calculate(VehicleType vehicleType, LocalDateTime entryTime, LocalDateTime exitTime) {
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        if (minutes <= properties.graceMinutes()) {
            return BigDecimal.ZERO.setScale(2);
        }
        long hours = Math.max(1, (minutes + 59) / 60);
        BigDecimal rate = properties.rate().get(vehicleType);
        if (rate == null) {
            throw new IllegalStateException("No hourly rate configured for " + vehicleType);
        }
        return rate.multiply(BigDecimal.valueOf(hours)).setScale(2);
    }
}
