package com.flightapp.webflux.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.webflux.Controller.SearchController;
import com.flightapp.webflux.DTO.FlightSearchRequest;
import com.flightapp.webflux.DTO.FlightSearchResponse;
import com.flightapp.webflux.Service.FlightInventoryService;
import com.flightapp.webflux.exception.GlobalErrorHandler;

import reactor.core.publisher.Flux;

@WebFluxTest(controllers = SearchController.class)
@Import(GlobalErrorHandler.class)
class SearchControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FlightInventoryService inventoryService;

    @Test
    void search_success() {
        FlightSearchResponse res = new FlightSearchResponse();
        res.setAirlineName("Indigo");
        res.setFlightNumber("AE101");

        when(inventoryService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(Flux.just(res));

        String body = """
            {
              "fromPlace": "DELHI",
              "toPlace": "MUMBAI",
              "travelDate": "2025-11-25",
              "travelTime": "09:00",
              "tripType": "ONE_WAY"
            }
            """;

        webTestClient.post()
                .uri("/api/v1.0/flight/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].airlineName").isEqualTo("Indigo")
                .jsonPath("$[0].flightNumber").isEqualTo("AE101");
    }

    @Test
    void search_validationError() {
        // Missing required fields like fromPlace, toPlace, travelDate
        String invalidBody = """
            {
              "fromPlace": "",
              "toPlace": "",
              "travelDate": null,
              "tripType": "ONE_WAY"
            }
            """;

        webTestClient.post()
                .uri("/api/v1.0/flight/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBody)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
