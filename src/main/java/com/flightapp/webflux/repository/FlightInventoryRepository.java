package com.flightapp.webflux.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.webflux.model.FlightInventory;

import reactor.core.publisher.Flux;

@Repository
public interface FlightInventoryRepository extends ReactiveMongoRepository<FlightInventory, String> {

    @Query("""
        {
            'fromPlace': { $regex: ?0, $options: 'i' },
            'toPlace': { $regex: ?1, $options: 'i' },
            'departureTime': { $gte: ?2, $lte: ?3 }
        }
    """)
    Flux<FlightInventory> searchFlights(
            String fromPlace,
            String toPlace,
            LocalDateTime start,
            LocalDateTime end
    );
    Flux<FlightInventory> findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureTimeBetween(
        String from,
        String to,
        LocalDateTime start,
        LocalDateTime end
        );
}

