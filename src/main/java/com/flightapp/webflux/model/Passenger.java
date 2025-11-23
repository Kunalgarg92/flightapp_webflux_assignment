package com.flightapp.webflux.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "passenger")
public class Passenger {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("gender")
    private String gender;

    @Field("age")
    private int age;

    @Field("meal")
    private String meal;

    @Field("seat_number")
    private int seatNumber;

    @Field("booking_id")
    private String bookingId;  // 🔥 MongoDB ref should be String, not Long

    @Field("fare_category")
    private String fareCategory;

    @Field("fare_applied")
    private double fareApplied;

    @Field("fare_message")
    private String fareMessage;

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) { this.id = id; }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() {
        return age;
    }
    public void setAge(int age) { this.age = age; }

    public String getMeal() {
        return meal;
    }
    public void setMeal(String meal) { this.meal = meal; }

    public int getSeatNumber() {
        return seatNumber;
    }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public String getBookingId() {
        return bookingId;
    }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getFareCategory() {
        return fareCategory;
    }
    public void setFareCategory(String fareCategory) { this.fareCategory = fareCategory; }

    public double getFareApplied() {
        return fareApplied;
    }
    public void setFareApplied(double fareApplied) { this.fareApplied = fareApplied; }

    public String getFareMessage() {
        return fareMessage;
    }
    public void setFareMessage(String fareMessage) { this.fareMessage = fareMessage; }
}
