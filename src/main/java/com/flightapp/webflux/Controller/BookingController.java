package com.flightapp.webflux.Controller;
import com.flightapp.webflux.DTO.BookingRequest;
import com.flightapp.webflux.DTO.BookingResponse;
import com.flightapp.webflux.Service.BookingService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1.0/flight")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/booking/{flightId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<BookingResponse> book(
            @PathVariable String flightId,
            @Valid @RequestBody BookingRequest request) {

        return bookingService.bookFlight(flightId, request);
    }
    @GetMapping("/booking/history/{email}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<BookingResponse> history(@PathVariable String email) {

        return bookingService.getBookingHistory(email);
    }
    @DeleteMapping("/booking/cancel/{pnr}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> cancel(@PathVariable String pnr) {
        return bookingService.cancelBooking(pnr)
                .then(Mono.just("Ticket with PNR " + pnr + " has been cancelled successfully."));
    }
}