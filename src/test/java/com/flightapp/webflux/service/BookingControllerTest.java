package com.flightapp.webflux.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.webflux.Controller.BookingController;
import com.flightapp.webflux.DTO.BookingResponse;
import com.flightapp.webflux.Service.BookingService;
import com.flightapp.webflux.exception.GlobalErrorHandler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = BookingController.class)
@Import(GlobalErrorHandler.class)
class BookingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void postBooking_validation_error() {

        String invalidBody = """
            {
              "email":"a@b.com",
              "numberOfSeats":2,
              "passengers":[]
            }
            """;

        webTestClient.post()
                .uri("/api/v1.0/flight/booking/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void postBooking_success_returns200() {

        BookingResponse resp = new BookingResponse();
        resp.setPnr("ABC123");

        Mockito.when(
                bookingService.bookFlight(eq("1"), any())
        ).thenReturn(Mono.just(resp));

        String validBody = """
            {
              "email":"a@b.com",
              "numberOfSeats":1,
              "passengers":[
                {
                  "name":"A",
                  "gender":"MALE",
                  "age":20,
                  "meal":"VEG",
                  "seatNumber":10,
                  "fareCategory":"STUDENT"
                }
              ]
            }
            """;

        webTestClient.post()
                .uri("/api/v1.0/flight/booking/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo("ABC123");
    }

    @Test
    void testGetHistory() {

        Mockito.when(
                bookingService.getBookingHistory("abc@gmail.com")
        ).thenReturn(Flux.just(new BookingResponse()));

        webTestClient.get()
                .uri("/api/v1.0/flight/booking/history/abc@gmail.com")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void cancelBooking_success() {

        Mockito.when(
                bookingService.cancelBooking("PNR123")
        ).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1.0/flight/booking/cancel/PNR123")
                .exchange()
                .expectStatus().isOk();
    }
}
