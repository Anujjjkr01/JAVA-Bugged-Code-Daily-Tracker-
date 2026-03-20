package com.taskmanager.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.exception.TaskValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TravelTaskTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    void createTravelTask_setsFieldsCorrectly() {
        TravelTask task = new TravelTask("Trip", "Business trip", Priority.HIGH, FUTURE_DATE,
                "New York", TravelMode.FLIGHT);

        assertEquals("Trip", task.getTitle());
        assertEquals(Category.TRAVEL, task.getCategory());
        assertEquals("New York", task.getDestination());
        assertEquals(TravelMode.FLIGHT, task.getTravelMode());
    }

    @Test
    void createTravelTask_forcesCategoryToTravel() {
        TravelTask task = new TravelTask("Trip", "desc", Priority.MEDIUM, FUTURE_DATE,
                "Boston", TravelMode.TRAIN);
        assertEquals(Category.TRAVEL, task.getCategory());
    }

    @Test
    void createTravelTask_withoutDestination_throwsValidationException() {
        assertThrows(TaskValidationException.class,
                () -> new TravelTask("Trip", "desc", Priority.MEDIUM, FUTURE_DATE, null, TravelMode.CAR));
    }

    @Test
    void createTravelTask_withBlankDestination_throwsValidationException() {
        assertThrows(TaskValidationException.class,
                () -> new TravelTask("Trip", "desc", Priority.MEDIUM, FUTURE_DATE, "  ", TravelMode.CAR));
    }

    @Test
    void jacksonPolymorphicSerialization_roundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        TravelTask original = new TravelTask("Trip", "desc", Priority.HIGH, FUTURE_DATE,
                "Chicago", TravelMode.TRAIN);
        String json = mapper.writeValueAsString(original);

        assertTrue(json.contains("\"type\":\"TravelTask\""));

        Task deserialized = mapper.readValue(json, Task.class);
        assertInstanceOf(TravelTask.class, deserialized);
        TravelTask travelDeserialized = (TravelTask) deserialized;
        assertEquals("Chicago", travelDeserialized.getDestination());
        assertEquals(TravelMode.TRAIN, travelDeserialized.getTravelMode());
    }
}
