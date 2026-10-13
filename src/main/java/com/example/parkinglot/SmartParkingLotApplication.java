package com.example.parkinglot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SmartParkingLotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartParkingLotApplication.class, args);
    }
}
