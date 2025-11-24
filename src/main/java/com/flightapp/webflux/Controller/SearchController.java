package com.flightapp.webflux.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.flightapp.webflux.DTO.FlightSearchRequest;
import com.flightapp.webflux.DTO.FlightSearchResponse;
import com.flightapp.webflux.Service.FlightInventoryService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1.0/flight")
public class SearchController {

    @Autowired
    private FlightInventoryService service;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.CREATED) 
    public Flux<FlightSearchResponse> search(
            @Valid @RequestBody FlightSearchRequest request) {

        return service.searchFlights(request); 
    }
}