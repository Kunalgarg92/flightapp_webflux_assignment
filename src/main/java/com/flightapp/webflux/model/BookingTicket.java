package com.flightapp.webflux.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "booking_ticket")
public class BookingTicket {

    @Id
    private String id;  

    @Field("pnr")
    private String pnr;

    @Field("email")
    private String email;

    @Field("flight_id")
    private String flightId;

    @Field("booking_time")
    private Instant bookingTime;

    @Field("number_of_seats")
    private int numberOfSeats;

    @Field("total_price")
    private double totalPrice;

    @Field("status")
    private String status;

    @Field("cancelled")
    private boolean cancelled = false;

    @Field("passengers")
    private List<Passenger> passengers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    

    public String getFlightId() {
		return flightId;
	}

	public void setFlightId(String flightId) {
		this.flightId = flightId;
	}

	public Instant getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Instant bookingTime) {
        this.bookingTime = bookingTime;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }
}
