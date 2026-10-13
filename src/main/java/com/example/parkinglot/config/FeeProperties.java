package com.example.parkinglot.config;

import com.example.parkinglot.model.VehicleType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

@ConfigurationProperties(prefix = "parking.fee")
public record FeeProperties(int graceMinutes, Map<VehicleType, BigDecimal> rate) {
}
