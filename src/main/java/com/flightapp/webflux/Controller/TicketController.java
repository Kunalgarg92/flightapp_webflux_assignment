package com.flightapp.webflux.Controller;
import com.flightapp.webflux.DTO.BookingResponse;
import com.flightapp.webflux.Service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1.0/flight")
public class TicketController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/ticket/{pnr}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<BookingResponse> getTicketByPnr(@PathVariable("pnr") String pnr) {

        return bookingService.getBookingByPnr(pnr); 
    }
}
