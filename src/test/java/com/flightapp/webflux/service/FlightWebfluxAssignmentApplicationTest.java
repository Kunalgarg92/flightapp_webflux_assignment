package com.flightapp.webflux.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import com.flightapp.webflux.FlightWebfluxAssignmentApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FlightWebfluxAssignmentApplicationTest {

    @Test
    void mainMethod_runsSuccessfully_withoutStartingServer() {

        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            mocked.when(() ->
                    SpringApplication.run(FlightWebfluxAssignmentApplication.class, new String[]{})
            ).thenReturn(null);

            assertDoesNotThrow(() ->
                    FlightWebfluxAssignmentApplication.main(new String[]{})
            );
        }
    }
}
