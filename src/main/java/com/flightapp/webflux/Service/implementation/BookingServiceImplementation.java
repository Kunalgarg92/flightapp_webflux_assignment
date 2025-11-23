package com.flightapp.webflux.Service.implementation;

import com.flightapp.webflux.DTO.BookingRequest;
import com.flightapp.webflux.DTO.BookingResponse;
import com.flightapp.webflux.DTO.PassengerRequest;
import com.flightapp.webflux.Service.BookingService;
import com.flightapp.webflux.exception.ResourceNotFoundException;
import com.flightapp.webflux.model.BookingTicket;
import com.flightapp.webflux.model.FlightInventory;
import com.flightapp.webflux.model.Passenger;
import com.flightapp.webflux.model.BookingStatus;
import com.flightapp.webflux.repository.BookingRepository;
import com.flightapp.webflux.repository.FlightInventoryRepository;
import com.flightapp.webflux.repository.PassengerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingServiceImplementation implements BookingService {

    @Autowired
    private FlightInventoryRepository flightRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private PassengerRepository passengerRepo;

    private static final Random RNG = new Random();

    @Override
    public Mono<BookingResponse> bookFlight(String flightId, BookingRequest request) {

        int seatsRequested = request.getNumberOfSeats();

        if (seatsRequested != request.getPassengers().size()) {
            return Mono.error(new IllegalArgumentException(
                    "numberOfSeats must equal number of passengers"));
        }

        List<Integer> seatNumbers = request.getPassengers()
                .stream()
                .map(PassengerRequest::getSeatNumber)
                .collect(Collectors.toList());

        Set<Integer> uniqueSeats = new HashSet<>(seatNumbers);
        if (uniqueSeats.size() != seatNumbers.size()) {
            return Mono.error(new IllegalArgumentException("Duplicate seat numbers in request"));
        }

        return flightRepo.findById(flightId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Flight not found")))
                .flatMap(flight -> {

                    for (Integer s : seatNumbers) {
                        if (s < 1 || s > flight.getTotalSeats()) {
                            return Mono.error(new IllegalArgumentException(
                                    "Seat number " + s + " is out of range for this flight"));
                        }
                    }

                    if (flight.getAvailableSeats() < seatsRequested) {
                        return Mono.error(new IllegalArgumentException("Not enough seats available"));
                    }

                    return bookingRepo.findByFlightId(flightId)
                            .flatMap(booking ->
                                    passengerRepo.findByBookingId(booking.getId()))
                            .filter(p -> seatNumbers.contains(p.getSeatNumber()))
                            .collectList()
                            .flatMap(alreadyBooked -> {
                                if (!alreadyBooked.isEmpty()) {
                                    String taken = alreadyBooked.stream()
                                            .map(p -> String.valueOf(p.getSeatNumber()))
                                            .collect(Collectors.joining(","));
                                    return Mono.error(new IllegalArgumentException(
                                            "Seat(s) already booked: " + taken));
                                }

                                String inventoryCat = (flight.getFareCategory() == null)
                                        ? ""
                                        : flight.getFareCategory().trim().toUpperCase();

                                return generatePnr()
                                        .flatMap(pnr -> {

                                            BookingTicket booking = new BookingTicket();
                                            booking.setPnr(pnr);
                                            booking.setEmail(request.getEmail());
                                            booking.setFlightId(flight.getId());
                                            booking.setBookingTime(Instant.now());
                                            booking.setNumberOfSeats(seatsRequested);
                                            booking.setStatus(BookingStatus.BOOKED.name());
                                            booking.setCancelled(false);

                                            List<Passenger> passengerEntities = new ArrayList<>();
                                            double totalPrice = 0.0;

                                            for (PassengerRequest pr : request.getPassengers()) {

                                                Passenger p = new Passenger();
                                                p.setName(pr.getName());
                                                p.setAge(pr.getAge());
                                                p.setGender(pr.getGender());
                                                p.setMeal(pr.getMeal());
                                                p.setSeatNumber(pr.getSeatNumber());

                                                String passengerCat =
                                                        (pr.getFareCategory() == null)
                                                                ? ""
                                                                : pr.getFareCategory().trim().toUpperCase();

                                                p.setFareCategory(passengerCat);

                                                if (!inventoryCat.isEmpty()
                                                        && passengerCat.equals(inventoryCat)) {
                                                    p.setFareApplied(flight.getSpecialFare());
                                                    p.setFareMessage("Special fare applied for category: " + passengerCat);
                                                    totalPrice += flight.getSpecialFare();
                                                } else {
                                                    p.setFareApplied(flight.getPrice());
                                                    p.setFareMessage("No special fare available. Extra benefits applied.");
                                                    totalPrice += flight.getPrice();
                                                }

                                                passengerEntities.add(p);
                                            }

                                            booking.setTotalPrice(totalPrice);

                                            return bookingRepo.save(booking)
                                                    .flatMap(savedBooking -> {
                                                        passengerEntities.forEach(
                                                                p -> p.setBookingId(savedBooking.getId())
                                                        );

                                                        return passengerRepo.saveAll(passengerEntities)
                                                                .collectList()
                                                                .flatMap(savedPassengers -> {

                                                                    flight.setAvailableSeats(
                                                                            flight.getAvailableSeats() - seatsRequested);

                                                                    return flightRepo.save(flight)
                                                                            .thenReturn(buildBookingResponse(
                                                                                    savedBooking,
                                                                                    savedPassengers,
                                                                                    "Booking successful"));
                                                                });
                                                    });
                                        });
                            });
                });
    }

    @Override
    public Mono<BookingResponse> getBookingByPnr(String pnr) {

        return bookingRepo.findByPnr(pnr)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Booking with PNR '" + pnr + "' not found")))
                .flatMap(booking ->
                        passengerRepo.findByBookingId(booking.getId())
                                .collectList()
                                .map(passengers ->
                                        buildBookingResponse(booking, passengers, "Booking retrieved")
                                )
                );
    }

    @Override
    public Flux<BookingResponse> getBookingHistory(String email) {

        return bookingRepo.findByEmailOrderByBookingTimeDesc(email)
                .flatMap(booking ->
                        passengerRepo.findByBookingId(booking.getId())
                                .collectList()
                                .map(passengers ->
                                        buildBookingResponse(booking, passengers, "Booking history item")
                                )
                );
    }

    @Override
    public Mono<Void> cancelBooking(String pnr) {

        return bookingRepo.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "PNR '" + pnr + "' not found")))
                .flatMap(booking ->
                        flightRepo.findById(booking.getFlightId())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Flight not found for booking")))
                                .flatMap(flight -> {

                                    LocalDateTime now = LocalDateTime.now();
                                    LocalDateTime departure = flight.getDepartureTime();

                                    if (departure.minusHours(24).isBefore(now)) {
                                        return Mono.error(new IllegalArgumentException(
                                                "Cancellation not allowed within 24 hours of departure"));
                                    }

                                    flight.setAvailableSeats(
                                            flight.getAvailableSeats() + booking.getNumberOfSeats());

                                    return flightRepo.save(flight)
                                            .then(bookingRepo.delete(booking));
                                })
                );
    }

    private Mono<String> generatePnr() {
        return tryGeneratePnr(0);
    }

    private Mono<String> tryGeneratePnr(int attempt) {

        if (attempt >= 5) {
            return Mono.error(new IllegalStateException("Could not generate unique PNR"));
        }

        String p = randomAlphaNumeric(6);

        return bookingRepo.findByPnr(p)
                .hasElement()
                .flatMap(exists -> exists
                        ? tryGeneratePnr(attempt + 1)
                        : Mono.just(p));
    }

    private String randomAlphaNumeric(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RNG.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private BookingResponse buildBookingResponse(
            BookingTicket booking,
            List<Passenger> passengers,
            String message
    ) {
        BookingResponse resp = new BookingResponse();

        resp.setPnr(booking.getPnr());
        resp.setEmail(booking.getEmail());
        resp.setNumberOfSeats(booking.getNumberOfSeats());
        resp.setTotalPrice(booking.getTotalPrice());
        resp.setBookingTime(booking.getBookingTime());
        resp.setMessage(message);

        List<BookingResponse.PassengerInfo> plist = passengers.stream()
                .map(px -> {
                    BookingResponse.PassengerInfo info = new BookingResponse.PassengerInfo();
                    info.name = px.getName();
                    info.age = px.getAge();
                    info.gender = px.getGender();
                    info.meal = px.getMeal();
                    info.seatNumber = px.getSeatNumber();
                    info.fareCategory = px.getFareCategory();
                    info.fareApplied = px.getFareApplied();
                    info.fareMessage = px.getFareMessage();
                    return info;
                })
                .collect(Collectors.toList());

        resp.setPassengers(plist);

        return resp;
    }
}
