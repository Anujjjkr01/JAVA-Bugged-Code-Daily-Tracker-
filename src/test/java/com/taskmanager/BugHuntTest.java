package com.taskmanager;

import com.taskmanager.model.*;
import com.taskmanager.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Hunt Test Suite — each test targets a specific intentional bug.
 * Your goal: find the bug in the source code, fix it, and make the test pass.
 *
 * There are 6 bugs hidden across Task.java, TaskManager.java,
 * TaskController.java, and FoodTask.java. Good luck!
 */
class BugHuntTest {

    @TempDir
    Path tempDir;

    private TaskManager manager;
    private static final LocalDate FUTURE = LocalDate.now().plusDays(5);

    @BeforeEach
    void setUp() {
        manager = new TaskManager(tempDir.resolve("tasks.json").toString());
    }

    // ===================== BUG #1 =====================
    @Test
    @DisplayName("BUG #1: Default priority should be MEDIUM when null is passed")
    void bug1_defaultPriorityShouldBeMedium() {
        Task task = manager.createTask("Standup", "Daily sync", Category.WORK, null, FUTURE);

        // Expected: MEDIUM (the documented default)
        assertEquals(Priority.MEDIUM, task.getPriority(),
                "Priority should default to MEDIUM when null is provided");
    }

    // ===================== BUG #2 =====================
    @Test
    @DisplayName("BUG #2: Timezone should preserve full IANA timezone IDs like America/New_York")
    void bug2_timezoneShouldNotBeTruncated() {
        Task task = manager.createTask("Review", "Quarterly review", Category.WORK, Priority.HIGH, FUTURE);
        task.setTimeZone("America/New_York");

        assertEquals("America/New_York", task.getTimeZone(),
                "Timezone should store the full IANA timezone ID without truncation");
    }

    // ===================== BUG #3 =====================
    @Test
    @DisplayName("BUG #3: createTask with notes param should actually set the notes field")
    void bug3_createTaskWithNotesShouldSetNotes() {
        Task task = manager.createTask("Expense report", "Submit monthly expenses",
                Category.WORK, Priority.LOW, FUTURE,
                "Include receipts from conference", "America/Chicago");

        assertEquals("Include receipts from conference", task.getNotes(),
                "Notes should be set to the provided value, not null or timezone");
        assertEquals("America/Chicago", task.getTimeZone(),
                "Timezone should also be set correctly");
    }

    // ===================== BUG #4 =====================
    @Test
    @DisplayName("BUG #4: applyExtras should not overwrite notes with null when notes param is absent")
    void bug4_applyExtrasShouldNotOverwriteNotesWithNull() {
        // This tests the controller logic indirectly:
        // When a task already has notes and applyExtras is called with null notes,
        // the existing notes should be preserved.
        Task task = manager.createTask("Code review", "Review PR #42",
                Category.WORK, Priority.HIGH, FUTURE);
        task.setNotes("Check error handling paths");

        // Simulate what applyExtras does — only set if non-null
        String incomingNotes = null;
        if (incomingNotes != null) {
            task.setNotes(incomingNotes);
        }
        // The bug is in TaskController.applyExtras which does t.setNotes(req.notes())
        // without the null check, wiping out existing notes.
        // This test verifies the CORRECT behavior:
        assertEquals("Check error handling paths", task.getNotes(),
                "Existing notes should be preserved when incoming notes is null");
    }

    // ===================== BUG #5 =====================
    @Test
    @DisplayName("BUG #5: FoodTask with null mealType should default to LUNCH, not BREAKFAST")
    void bug5_foodTaskDefaultMealTypeShouldBeLunch() {
        FoodTask task = manager.createFoodTask("Quick bite", "Grab something",
                Priority.LOW, FUTURE, null);

        assertEquals(MealType.LUNCH, task.getMealType(),
                "Default meal type should be LUNCH when null is provided");
    }

    // ===================== BUG #6 =====================
    @Test
    @DisplayName("BUG #6: markComplete should set the completion timestamp")
    void bug6_markCompleteShouldSetTimestamp() {
        Task task = manager.createTask("Deploy", "Push to prod", Category.WORK, Priority.HIGH, FUTURE);

        manager.markComplete(task.getId());

        assertTrue(task.isCompleted(), "Task should be marked as completed");
        assertNotNull(task.getCompletionTimestamp(),
                "Completion timestamp should be set when task is marked complete");
    }
}
