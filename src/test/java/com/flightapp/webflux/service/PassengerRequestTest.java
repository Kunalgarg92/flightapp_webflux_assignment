package com.flightapp.webflux.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.flightapp.webflux.DTO.PassengerRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

public class PassengerRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testGetterSetter() {
        PassengerRequest pr = new PassengerRequest();

        pr.setName("Kunal");
        pr.setGender("MALE");
        pr.setAge(22);
        pr.setMeal("VEG");
        pr.setSeatNumber(15);
        pr.setFareCategory("STUDENT");

        assertEquals("Kunal", pr.getName());
        assertEquals("MALE", pr.getGender());
        assertEquals(22, pr.getAge());
        assertEquals("VEG", pr.getMeal());
        assertEquals(15, pr.getSeatNumber());
        assertEquals("STUDENT", pr.getFareCategory());
    }

    @Test
    void validationFails_whenInvalidInputs() {
        PassengerRequest pr = new PassengerRequest();
        pr.setName("");                   
        pr.setGender("INVALID");           
        pr.setAge(-1);                     
        pr.setMeal("FOOD");                
        pr.setSeatNumber(0);               
        pr.setFareCategory("");           

        Set<?> violations = validator.validate(pr);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validationPasses_withValidInputs() {
        PassengerRequest pr = new PassengerRequest();

        pr.setName("Kunal");
        pr.setGender("MALE");
        pr.setAge(20);
        pr.setMeal("VEG");
        pr.setSeatNumber(5);
        pr.setFareCategory("STUDENT");

        Set<?> violations = validator.validate(pr);
        assertTrue(violations.isEmpty());
    }
}
