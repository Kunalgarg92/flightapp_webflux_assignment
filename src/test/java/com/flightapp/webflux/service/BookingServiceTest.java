package com.flightapp.webflux.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.webflux.DTO.BookingRequest;
import com.flightapp.webflux.DTO.BookingResponse;
import com.flightapp.webflux.DTO.PassengerRequest;
import com.flightapp.webflux.Service.implementation.BookingServiceImplementation;
import com.flightapp.webflux.model.BookingTicket;
import com.flightapp.webflux.model.FlightInventory;
import com.flightapp.webflux.model.Passenger;
import com.flightapp.webflux.repository.BookingRepository;
import com.flightapp.webflux.repository.FlightInventoryRepository;
import com.flightapp.webflux.repository.PassengerRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private FlightInventoryRepository flightRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private PassengerRepository passengerRepo;

    @InjectMocks
    private BookingServiceImplementation service;

    @Test
    void testBookingFailsWhenSeatNotAvailable() {

        BookingRequest request = new BookingRequest();
        request.setEmail("test@gmail.com");
        request.setNumberOfSeats(2);
        request.setPassengers(List.of(
                new PassengerRequest("STUDENT", "Aman", "MALE", 25, "VEG", 10),
                new PassengerRequest("STUDENT", "Amen", "MALE", 26, "VEG", 11)
        ));

        FlightInventory flight = new FlightInventory();
        flight.setAvailableSeats(1);

        when(flightRepo.findById("1")).thenReturn(Mono.just(flight));

        assertThrows(IllegalArgumentException.class,
                () -> service.bookFlight("1", request).block());
    }

    @Test
    void testSpecialFare() {

        FlightInventory flight = new FlightInventory();
        flight.setFareCategory("STUDENT");
        flight.setSpecialFare(3500);
        flight.setPrice(4500);
        flight.setAvailableSeats(10);
        flight.setTotalSeats(180);

        when(flightRepo.findById("1")).thenReturn(Mono.just(flight));
        when(passengerRepo.findBookedSeatsForFlight(eq("1"), anyList())).thenReturn(Flux.empty());

        BookingTicket saved = new BookingTicket();
        saved.setId("101");
        saved.setPnr("ABC123");

        when(bookingRepo.save(any())).thenReturn(Mono.just(saved));
        when(passengerRepo.saveAll(any(List.class)))
        .thenReturn(Flux.empty());
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));

        BookingRequest req = new BookingRequest();
        req.setEmail("test@gmail.com");
        req.setNumberOfSeats(1);

        PassengerRequest p = new PassengerRequest("STUDENT", "Kunal", "MALE", 21, "VEG", 10);
        req.setPassengers(List.of(p));

        BookingResponse resp = service.bookFlight("1", req).block();
        assertEquals(3500, resp.getPassengers().get(0).fareApplied);
    }

    @Test
    void fail_when_duplicate_seats() {

        FlightInventory flight = new FlightInventory();
        flight.setAvailableSeats(10);
        flight.setTotalSeats(180);

        when(flightRepo.findById("1")).thenReturn(Mono.just(flight));

        BookingRequest req = new BookingRequest();
        req.setEmail("a@b.com");
        req.setNumberOfSeats(2);
        req.setPassengers(List.of(
                new PassengerRequest("STUDENT", "A", "MALE", 20, "VEG", 10),
                new PassengerRequest("STUDENT", "B", "MALE", 21, "VEG", 10)
        ));

        assertThrows(IllegalArgumentException.class, () -> service.bookFlight("1", req).block());
    }

    @Test
    void seat_already_booked() {

        FlightInventory flight = new FlightInventory();
        flight.setAvailableSeats(10);
        flight.setTotalSeats(180);

        when(flightRepo.findById("1")).thenReturn(Mono.just(flight));

        when(passengerRepo.findBookedSeatsForFlight(eq("1"), anyList()))
                .thenReturn(Flux.just(new Passenger()));

        BookingRequest req = new BookingRequest();
        req.setEmail("a@b.com");
        req.setNumberOfSeats(1);
        req.setPassengers(List.of(
                new PassengerRequest("STUDENT", "A", "MALE", 20, "VEG", 10)
        ));

        assertThrows(IllegalArgumentException.class, () -> service.bookFlight("1", req).block());
    }
}
