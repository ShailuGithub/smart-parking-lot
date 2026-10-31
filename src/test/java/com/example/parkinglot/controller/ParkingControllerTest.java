package com.example.parkinglot.controller;

import com.example.parkinglot.repository.ParkingSpotRepository;
import com.example.parkinglot.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ParkingControllerTest {

    @Autowired
    private MockMvc mvc;
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

    private void checkIn(String body, int expectedStatus) throws Exception {
        mvc.perform(post("/api/parking/check-in").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void checkInReturns201WithTicket() throws Exception {
        mvc.perform(post("/api/parking/check-in").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicleNumber\":\"MH12AB1234\",\"vehicleType\":\"CAR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleNumber").value("MH12AB1234"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void alreadyParkedVehicleGets409() throws Exception {
        String body = "{\"vehicleNumber\":\"DUP-1\",\"vehicleType\":\"CAR\"}";
        checkIn(body, 201);
        checkIn(body, 409);
    }

    @Test
    void fullLotGets409() throws Exception {
        for (int i = 0; i < 6; i++) {
            checkIn("{\"vehicleNumber\":\"BUS-" + i + "\",\"vehicleType\":\"BUS\"}", 201);
        }
        checkIn("{\"vehicleNumber\":\"BUS-X\",\"vehicleType\":\"BUS\"}", 409);
    }

    @Test
    void checkOutWithoutTicketGets404() throws Exception {
        mvc.perform(post("/api/parking/check-out").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicleNumber\":\"NOPE-1\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownTicketGets404() throws Exception {
        mvc.perform(get("/api/parking/tickets/999999")).andExpect(status().isNotFound());
    }

    @Test
    void blankVehicleNumberGets400() throws Exception {
        checkIn("{\"vehicleNumber\":\"\",\"vehicleType\":\"CAR\"}", 400);
    }

    @Test
    void unknownVehicleTypeGets400() throws Exception {
        checkIn("{\"vehicleNumber\":\"X-1\",\"vehicleType\":\"SPACESHIP\"}", 400);
    }

    @Test
    void malformedJsonGets400() throws Exception {
        checkIn("{not json", 400);
    }
}
