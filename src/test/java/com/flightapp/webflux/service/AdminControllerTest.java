package com.flightapp.webflux.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.webflux.Controller.AdminController;
import com.flightapp.webflux.DTO.FlightInventoryRequest;
import com.flightapp.webflux.Service.FlightInventoryService;
import com.flightapp.webflux.exception.GlobalErrorHandler;
import com.flightapp.webflux.model.FlightInventory;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = AdminController.class)
@Import(GlobalErrorHandler.class)
class AdminControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FlightInventoryService inventoryService;

    @Test
    void addInventory_success() {
        FlightInventory entity = new FlightInventory();
        entity.setAirlineName("Indigo");
        entity.setFlightNumber("AE123");

        when(inventoryService.addInventory(any(FlightInventoryRequest.class)))
                .thenReturn(Mono.just(entity));

        String body = """
            {
              "airlineName": "Indigo",
              "flightNumber": "AE123",
              "fromPlace": "DELHI",
              "toPlace": "MUMBAI",
              "departureTime": "2025-12-01T10:00:00",
              "arrivalTime": "2025-12-01T12:00:00",
              "totalSeats": 180,
              "availableSeats": 180,
              "price": 4500.0,
              "specialFare": 3500.0,
              "fareCategory": "STUDENT"
            }
            """;

        webTestClient.post()
                .uri("/api/v1.0/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.airlineName").isEqualTo("Indigo")
                .jsonPath("$.flightNumber").isEqualTo("AE123");
    }

    @Test
    void addInventory_validationError() {
        String invalidBody = """
            {
              "airlineName": "",
              "flightNumber": "A",
              "fromPlace": "",
              "toPlace": "M",
              "departureTime": "2020-01-01T10:00:00",
              "arrivalTime": "2020-01-01T09:00:00",
              "totalSeats": 0,
              "availableSeats": -1,
              "price": -5.0,
              "specialFare": -10.0,
              "fareCategory": "INVALID"
            }
            """;

        webTestClient.post()
                .uri("/api/v1.0/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBody)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
