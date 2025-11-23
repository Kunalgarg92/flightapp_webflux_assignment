package com.flightapp.webflux.Service;

import com.flightapp.webflux.DTO.FlightInventoryRequest;
import com.flightapp.webflux.model.FlightInventory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.flightapp.webflux.DTO.FlightSearchResponse;
import com.flightapp.webflux.DTO.FlightSearchRequest;

public interface FlightInventoryService {
	Mono<FlightInventory> addInventory(FlightInventoryRequest request);
    Flux<FlightSearchResponse> searchFlights(FlightSearchRequest request);
}
