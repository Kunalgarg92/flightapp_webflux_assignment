package com.flightapp.webflux.Service.implementation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.flightapp.webflux.DTO.FlightInventoryRequest;
import com.flightapp.webflux.DTO.FlightSearchRequest;
import com.flightapp.webflux.DTO.FlightSearchResponse;
import com.flightapp.webflux.Service.FlightInventoryService;
import com.flightapp.webflux.model.FlightInventory;
import com.flightapp.webflux.repository.FlightInventoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FlightInventoryServiceImplementation implements FlightInventoryService {

    @Autowired
    private FlightInventoryRepository repository;

    @Override
    public Mono<FlightInventory> addInventory(FlightInventoryRequest request) {

        FlightInventory f = new FlightInventory();

        f.setAirlineName(request.getAirlineName());
        f.setFlightNumber(request.getFlightNumber());
        f.setFromPlace(request.getFromPlace());
        f.setToPlace(request.getToPlace());
        f.setDepartureTime(request.getDepartureTime());
        f.setArrivalTime(request.getArrivalTime());
        f.setTotalSeats(request.getTotalSeats());
        f.setAvailableSeats(request.getTotalSeats());
        f.setPrice(request.getPrice());
        f.setSpecialFare(request.getSpecialFare());
        f.setFareCategory(request.getFareCategory());

        return repository.save(f);
    }

    @Override
    public Flux<FlightSearchResponse> searchFlights(FlightSearchRequest request) {

        LocalDateTime startTime;
        LocalDateTime endTime;

        if (request.getTravelTime() == null || request.getTravelTime().isBlank()) {
            startTime = request.getTravelDate().atStartOfDay();
            endTime = request.getTravelDate().atTime(23, 59, 59);
        } else {
            LocalTime userTime = LocalTime.parse(request.getTravelTime());
            startTime = LocalDateTime.of(request.getTravelDate(), userTime.minusMinutes(30));
            endTime = LocalDateTime.of(request.getTravelDate(), userTime.plusMinutes(30));
        }

        return repository
                .findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureTimeBetween(
                        request.getFromPlace().trim(),
                        request.getToPlace().trim(),
                        startTime,
                        endTime
                )
                .flatMap(onward -> {

                    FlightSearchResponse res = new FlightSearchResponse();
                    res.setAirlineName(onward.getAirlineName());
                    res.setFlightNumber(onward.getFlightNumber());
                    res.setDepartureTime(onward.getDepartureTime());
                    res.setArrivalTime(onward.getArrivalTime());
                    res.setOneWayPrice(onward.getPrice());
                    res.setRoundTripPrice(onward.getPrice() * 2);

                    if (!"ROUND_TRIP".equalsIgnoreCase(request.getTripType())) {
                        res.setMessage("One-way trip available.");
                        return Mono.just(res);
                    }

                    LocalDate returnDate = request.getReturnDate();
                    LocalDateTime rStart = returnDate.atStartOfDay();
                    LocalDateTime rEnd = returnDate.atTime(23, 59, 59);

                    return repository
                            .findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureTimeBetween(
                                    request.getToPlace().trim(),
                                    request.getFromPlace().trim(),
                                    rStart,
                                    rEnd
                            )
                            .collectList()
                            .flatMap(returnFlights -> {

                                if (!returnFlights.isEmpty()) {
                                    FlightInventory r = returnFlights.get(0);

                                    res.setReturnFlightNumber(r.getFlightNumber());
                                    res.setReturnDepartureTime(r.getDepartureTime());
                                    res.setReturnArrivalTime(r.getArrivalTime());
                                    res.setMessage("Round trip available.");
                                } else {
                                    res.setMessage("Only onward flight available. Return flight not found.");
                                }

                                return Mono.just(res);
                            });
                });
    }
}

