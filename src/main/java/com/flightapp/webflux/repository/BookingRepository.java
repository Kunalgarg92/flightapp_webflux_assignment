package com.flightapp.webflux.repository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.flightapp.webflux.model.BookingTicket;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BookingRepository extends ReactiveMongoRepository<BookingTicket, String> {

    Mono<BookingTicket> findByPnr(String pnr);

    Flux<BookingTicket> findByEmailOrderByBookingTimeDesc(String email);
    
    Flux<BookingTicket> findByFlightId(String flightId);

}
