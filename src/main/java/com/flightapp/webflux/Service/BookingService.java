package com.flightapp.webflux.Service;

import com.flightapp.webflux.DTO.BookingRequest;
import com.flightapp.webflux.DTO.BookingResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {
	Mono<BookingResponse> getBookingByPnr(String pnr);
    Mono<BookingResponse> bookFlight(String flightId, BookingRequest request);
    Flux<BookingResponse> getBookingHistory(String email);
    Mono<Void> cancelBooking(String pnr);
}

