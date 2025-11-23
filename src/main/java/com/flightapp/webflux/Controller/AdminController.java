package com.flightapp.webflux.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.webflux.DTO.FlightInventoryRequest;
import com.flightapp.webflux.Service.FlightInventoryService;
import com.flightapp.webflux.model.FlightInventory;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1.0/flight/airline")
public class AdminController {

    @Autowired
    FlightInventoryService inventoryService;

    @PostMapping("/inventory/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FlightInventory> addInventory(
            @Valid @RequestBody FlightInventoryRequest request) {

        return inventoryService.addInventory(request);
    }
}
