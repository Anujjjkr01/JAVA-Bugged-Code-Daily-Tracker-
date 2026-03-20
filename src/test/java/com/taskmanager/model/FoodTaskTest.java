package com.taskmanager.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FoodTaskTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    void createFoodTask_setsFieldsCorrectly() {
        FoodTask task = new FoodTask("Lunch meeting", "Team lunch", Priority.LOW, FUTURE_DATE,
                MealType.LUNCH);

        assertEquals("Lunch meeting", task.getTitle());
        assertEquals(Category.FOOD, task.getCategory());
        assertEquals(MealType.LUNCH, task.getMealType());
    }

    @Test
    void createFoodTask_forcesCategoryToFood() {
        FoodTask task = new FoodTask("Breakfast", "desc", Priority.MEDIUM, FUTURE_DATE,
                MealType.BREAKFAST);
        assertEquals(Category.FOOD, task.getCategory());
    }

    @Test
    void createFoodTask_withoutMealType_defaultsToLunch() {
        FoodTask task = new FoodTask("Meal", "desc", Priority.MEDIUM, FUTURE_DATE, null);
        assertEquals(MealType.LUNCH, task.getMealType());
    }

    @Test
    void jacksonPolymorphicSerialization_roundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        FoodTask original = new FoodTask("Dinner", "desc", Priority.LOW, FUTURE_DATE,
                MealType.DINNER);
        String json = mapper.writeValueAsString(original);

        assertTrue(json.contains("\"type\":\"FoodTask\""));

        Task deserialized = mapper.readValue(json, Task.class);
        assertInstanceOf(FoodTask.class, deserialized);
        FoodTask foodDeserialized = (FoodTask) deserialized;
        assertEquals(MealType.DINNER, foodDeserialized.getMealType());
    }
}
