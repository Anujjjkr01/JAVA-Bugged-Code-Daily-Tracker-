package com.taskmanager.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.exception.TaskValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    void createTask_withAllFields_setsFieldsCorrectly() {
        Task task = new Task("Meeting", "Team sync", Category.MEETING, Priority.HIGH, FUTURE_DATE);

        assertNotNull(task.getId());
        assertEquals("Meeting", task.getTitle());
        assertEquals("Team sync", task.getDescription());
        assertEquals(Category.MEETING, task.getCategory());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(FUTURE_DATE, task.getDueDate());
        assertFalse(task.isCompleted());
        assertNull(task.getCompletionTimestamp());
    }

    @Test
    void createTask_withoutPriority_defaultsToMedium() {
        Task task = new Task("Work item", "desc", Category.WORK, null, FUTURE_DATE);
        assertEquals(Priority.MEDIUM, task.getPriority());
    }

    @Test
    void createTask_withoutCategory_defaultsToWork() {
        Task task = new Task("Work item", "desc", null, Priority.LOW, FUTURE_DATE);
        assertEquals(Category.WORK, task.getCategory());
    }

    @Test
    void createTask_withEmptyTitle_throwsValidationException() {
        assertThrows(TaskValidationException.class,
                () -> new Task("", "desc", Category.WORK, Priority.MEDIUM, FUTURE_DATE));
    }

    @Test
    void createTask_withBlankTitle_throwsValidationException() {
        assertThrows(TaskValidationException.class,
                () -> new Task("   ", "desc", Category.WORK, Priority.MEDIUM, FUTURE_DATE));
    }

    @Test
    void createTask_withNullTitle_throwsValidationException() {
        assertThrows(TaskValidationException.class,
                () -> new Task(null, "desc", Category.WORK, Priority.MEDIUM, FUTURE_DATE));
    }

    @Test
    void createTask_withPastDueDate_throwsValidationException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThrows(TaskValidationException.class,
                () -> new Task("Title", "desc", Category.WORK, Priority.MEDIUM, pastDate));
    }

    @Test
    void createTask_assignsUniqueId() {
        Task task1 = new Task("Task 1", "desc", Category.WORK, Priority.MEDIUM, FUTURE_DATE);
        Task task2 = new Task("Task 2", "desc", Category.WORK, Priority.MEDIUM, FUTURE_DATE);
        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    void jacksonPolymorphicSerialization_roundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Task original = new Task("Test", "desc", Category.WORK, Priority.HIGH, FUTURE_DATE);
        String json = mapper.writeValueAsString(original);

        assertTrue(json.contains("\"type\":\"Task\""));

        Task deserialized = mapper.readValue(json, Task.class);
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getTitle(), deserialized.getTitle());
        assertEquals(original.getCategory(), deserialized.getCategory());
        assertEquals(original.getPriority(), deserialized.getPriority());
        assertEquals(original.getDueDate(), deserialized.getDueDate());
    }
}
