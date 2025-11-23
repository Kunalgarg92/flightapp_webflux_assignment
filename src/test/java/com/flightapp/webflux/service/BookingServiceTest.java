package com.flightapp.webflux.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.webflux.DTO.BookingRequest;
import com.flightapp.webflux.DTO.BookingResponse;
import com.flightapp.webflux.DTO.PassengerRequest;
import com.flightapp.webflux.Service.implementation.BookingServiceImplementation;
import com.flightapp.webflux.exception.ResourceNotFoundException;
import com.flightapp.webflux.model.BookingStatus;
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

    @BeforeEach
    void setup() {
        lenient().when(bookingRepo.findByPnr(anyString()))
                 .thenReturn(Mono.empty());
    }
    @Test
    void bookFlight_fails_when_numberOfSeats_mismatch_passengers() {
        BookingRequest req = new BookingRequest();
        req.setEmail("a@b.com");
        req.setNumberOfSeats(2);  
        req.setPassengers(List.of( 
                new PassengerRequest("STUDENT", "A", "MALE", 20, "VEG", 10)
        ));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.bookFlight("FLT1", req).block()
        );
        assertTrue(ex.getMessage().contains("numberOfSeats must equal number of passengers"));
    }
    @Test
    void bookFlight_fails_when_not_enough_seats_available() {
        BookingRequest request = new BookingRequest();
        request.setEmail("test@gmail.com");
        request.setNumberOfSeats(2);
        request.setPassengers(List.of(
                new PassengerRequest("STUDENT", "Aman", "MALE", 25, "VEG", 10),
                new PassengerRequest("STUDENT", "Amen", "MALE", 26, "VEG", 11)
        ));
        FlightInventory flight = new FlightInventory();
        flight.setId("FLT1");
        flight.setTotalSeats(100);
        flight.setAvailableSeats(1);
        when(flightRepo.findById("FLT1")).thenReturn(Mono.just(flight));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.bookFlight("FLT1", request).block()
        );
        assertTrue(ex.getMessage().contains("Not enough seats available"));
    }
    @Test
    void bookFlight_fails_when_duplicate_seats_in_request() {
        BookingRequest req = new BookingRequest();
        req.setEmail("a@b.com");
        req.setNumberOfSeats(2);
        req.setPassengers(List.of(
                new PassengerRequest("STUDENT", "A", "MALE", 20, "VEG", 10),
                new PassengerRequest("STUDENT", "B", "MALE", 21, "VEG", 10) 
        ));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.bookFlight("FLT1", req).block()
        );
        assertTrue(ex.getMessage().contains("Duplicate seat numbers in request"));
    }
    @Test
    void bookFlight_fails_when_seat_out_of_range() {
        BookingRequest req = new BookingRequest();
        req.setEmail("a@b.com");
        req.setNumberOfSeats(1);
        req.setPassengers(List.of(
                new PassengerRequest("STUDENT", "A", "MALE", 20, "VEG", 999)
        ));
        FlightInventory flight = new FlightInventory();
        flight.setId("FLT1");
        flight.setTotalSeats(100);
        flight.setAvailableSeats(50);
        when(flightRepo.findById("FLT1")).thenReturn(Mono.just(flight));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.bookFlight("FLT1", req).block()
        );
        assertTrue(ex.getMessage().contains("is out of range for this flight"));
    }
    @Test
    void bookFlight_fails_when_seat_already_booked() {
        BookingRequest req = new BookingRequest();
        req.setEmail("a@b.com");
        req.setNumberOfSeats(1);
        req.setPassengers(List.of(
                new PassengerRequest("STUDENT", "A", "MALE", 20, "VEG", 10)
        ));
        FlightInventory flight = new FlightInventory();
        flight.setId("FLT1");
        flight.setTotalSeats(100);
        flight.setAvailableSeats(10);
        when(flightRepo.findById("FLT1")).thenReturn(Mono.just(flight));
        BookingTicket existingBooking = new BookingTicket();
        existingBooking.setId("BOOK1");
        existingBooking.setFlightId("FLT1");
        when(bookingRepo.findByFlightId("FLT1"))
                .thenReturn(Flux.just(existingBooking));
        Passenger already = new Passenger();
        already.setId("P1");
        already.setSeatNumber(10);
        when(passengerRepo.findByBookingId("BOOK1"))
                .thenReturn(Flux.just(already));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.bookFlight("FLT1", req).block()
        );
        assertTrue(ex.getMessage().contains("Seat(s) already booked"));
    }
    @Test
    void bookFlight_success_applies_special_fare_and_updates_seats() {
        BookingRequest req = new BookingRequest();
        req.setEmail("test@gmail.com");
        req.setNumberOfSeats(1);
        PassengerRequest pReq =
                new PassengerRequest("STUDENT", "Kunal", "MALE", 21, "VEG", 10);
        req.setPassengers(List.of(pReq));
        FlightInventory flight = new FlightInventory();
        flight.setId("FLT1");
        flight.setFareCategory("STUDENT");
        flight.setSpecialFare(3500);
        flight.setPrice(4500);
        flight.setAvailableSeats(10);
        flight.setTotalSeats(180);
        when(flightRepo.findById("FLT1")).thenReturn(Mono.just(flight));
        when(bookingRepo.findByFlightId("FLT1")).thenReturn(Flux.empty());
        when(bookingRepo.save(any(BookingTicket.class)))
                .thenAnswer(invocation -> {
                    BookingTicket b = invocation.getArgument(0);
                    b.setId("B123");
                    return Mono.just(b);
                });
        when(passengerRepo.saveAll(any(Iterable.class)))
                .thenAnswer(invocation -> {
                    Iterable<Passenger> it = invocation.getArgument(0);
                    return Flux.fromIterable(it);
                });

        when(flightRepo.save(any(FlightInventory.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        BookingResponse resp = service.bookFlight("FLT1", req).block();
        assertNotNull(resp);
        assertEquals(1, resp.getNumberOfSeats());
        assertEquals("test@gmail.com", resp.getEmail());
        assertEquals("Booking successful", resp.getMessage());
        assertEquals(1, resp.getPassengers().size());
        assertEquals(3500.0, resp.getPassengers().get(0).fareApplied);
        assertEquals(3500.0, resp.getTotalPrice());
        assertEquals(9, flight.getAvailableSeats());
    }

    @Test
    void getBookingByPnr_success() {
        BookingTicket booking = new BookingTicket();
        booking.setId("B1");
        booking.setPnr("PNR123");
        booking.setEmail("a@b.com");
        booking.setNumberOfSeats(1);
        booking.setTotalPrice(1000);
        booking.setBookingTime(Instant.now());
        Passenger pax = new Passenger();
        pax.setName("A");
        pax.setAge(20);
        pax.setGender("MALE");
        pax.setMeal("VEG");
        pax.setSeatNumber(10);
        pax.setFareCategory("STUDENT");
        pax.setFareApplied(1000);
        pax.setFareMessage("Test");
        when(bookingRepo.findByPnr("PNR123")).thenReturn(Mono.just(booking));
        when(passengerRepo.findByBookingId("B1")).thenReturn(Flux.just(pax));
        BookingResponse resp = service.getBookingByPnr("PNR123").block();
        assertNotNull(resp);
        assertEquals("PNR123", resp.getPnr());
        assertEquals("Booking retrieved", resp.getMessage());
        assertEquals(1, resp.getPassengers().size());
    }
    @Test
    void getBookingByPnr_not_found() {
        when(bookingRepo.findByPnr("UNKNOWN")).thenReturn(Mono.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getBookingByPnr("UNKNOWN").block()
        );
    }
    @Test
    void getBookingHistory_maps_each_booking() {
        BookingTicket b1 = new BookingTicket();
        b1.setId("B1");
        b1.setPnr("P1");
        b1.setEmail("x@y.com");
        b1.setNumberOfSeats(1);
        b1.setTotalPrice(1000);
        b1.setBookingTime(Instant.now());
        BookingTicket b2 = new BookingTicket();
        b2.setId("B2");
        b2.setPnr("P2");
        b2.setEmail("x@y.com");
        b2.setNumberOfSeats(2);
        b2.setTotalPrice(2000);
        b2.setBookingTime(Instant.now());
        when(bookingRepo.findByEmailOrderByBookingTimeDesc("x@y.com"))
                .thenReturn(Flux.just(b1, b2));
        Passenger p1 = new Passenger();
        p1.setName("A");
        p1.setSeatNumber(1);
        Passenger p2 = new Passenger();
        p2.setName("B");
        p2.setSeatNumber(2);
        when(passengerRepo.findByBookingId("B1"))
                .thenReturn(Flux.just(p1));
        when(passengerRepo.findByBookingId("B2"))
                .thenReturn(Flux.just(p2));
        List<BookingResponse> history =
                service.getBookingHistory("x@y.com").collectList().block();
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals("P1", history.get(0).getPnr());
        assertEquals("P2", history.get(1).getPnr());
    }
    @Test
    void cancelBooking_success_updates_seats_and_deletes_booking() {
        BookingTicket booking = new BookingTicket();
        booking.setId("B1");
        booking.setPnr("PNR123");
        booking.setFlightId("FLT1");
        booking.setNumberOfSeats(2);
        booking.setStatus(BookingStatus.BOOKED.name());
        when(bookingRepo.findByPnr("PNR123")).thenReturn(Mono.just(booking));
        FlightInventory flight = new FlightInventory();
        flight.setId("FLT1");
        flight.setAvailableSeats(10);
        flight.setTotalSeats(100);
        flight.setDepartureTime(LocalDateTime.now().plusDays(2));
        when(flightRepo.findById("FLT1")).thenReturn(Mono.just(flight));
        when(flightRepo.save(any(FlightInventory.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(bookingRepo.delete(booking)).thenReturn(Mono.empty());
        service.cancelBooking("PNR123").block();
        assertEquals(12, flight.getAvailableSeats());
    }
    @Test
    void cancelBooking_fails_when_pnr_not_found() {
        when(bookingRepo.findByPnr("UNKNOWN")).thenReturn(Mono.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.cancelBooking("UNKNOWN").block()
        );
    }
    @Test
    void cancelBooking_fails_when_within_24_hours_of_departure() {
        BookingTicket booking = new BookingTicket();
        booking.setId("B1");
        booking.setPnr("PNR123");
        booking.setFlightId("FLT1");
        booking.setNumberOfSeats(1);
        when(bookingRepo.findByPnr("PNR123")).thenReturn(Mono.just(booking));
        FlightInventory flight = new FlightInventory();
        flight.setId("FLT1");
        flight.setAvailableSeats(10);
        flight.setTotalSeats(100);
        flight.setDepartureTime(LocalDateTime.now().plusHours(12));

        when(flightRepo.findById("FLT1")).thenReturn(Mono.just(flight));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelBooking("PNR123").block()
        );
        assertTrue(ex.getMessage().contains("Cancellation not allowed within 24 hours of departure"));
    }
}
