package com.taskmanager.controller;

import com.taskmanager.model.*;
import com.taskmanager.service.TaskManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskManager taskManager;

    public TaskController(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @GetMapping
    public List<Task> getAllTasks(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String keyword) {
        if (category != null || priority != null || completed != null || keyword != null) {
            return taskManager.filterTasks(category, priority, completed, keyword);
        }
        return taskManager.getAllTasks();
    }

    @GetMapping("/sorted")
    public List<Task> getTasksSortedByPriority() {
        return taskManager.getTasksSortedByPriority();
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable String id) {
        return taskManager.getTaskById(id);
    }

    @PostMapping
    public Task createTask(@RequestBody CreateTaskRequest req) {
        Task created;
        if ("TRAVEL".equalsIgnoreCase(req.taskType())) {
            TravelTask t = taskManager.createTravelTask(
                    req.title(), req.description(), req.priority(),
                    req.dueDate(), req.destination(), req.travelMode());
            applyExtras(t, req);
            taskManager.save();
            return t;
        }
        if ("FOOD".equalsIgnoreCase(req.taskType())) {
            FoodTask t = taskManager.createFoodTask(
                    req.title(), req.description(), req.priority(),
                    req.dueDate(), req.mealType());
            applyExtras(t, req);
            taskManager.save();
            return t;
        }
        if ("MEETING".equalsIgnoreCase(req.taskType())) {
            MeetingTask t = taskManager.createMeetingTask(
                    req.title(), req.description(), req.priority(),
                    req.dueDate(), req.location(), req.attendees(), req.meetingLink());
            applyExtras(t, req);
            taskManager.save();
            return t;
        }
        created = taskManager.createTask(
                req.title(), req.description(), req.category(),
                req.priority(), req.dueDate(), req.notes(), req.timeZone());
        applyExtras(created, req);
        taskManager.save();
        return created;
    }

    private void applyExtras(Task t, CreateTaskRequest req) {
        // BUG #4: Always overwrites notes (even with null), losing notes set by specialized constructors
        t.setNotes(req.notes());
        if (req.timeZone() != null) t.setTimeZone(req.timeZone());
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable String id, @RequestBody UpdateTaskRequest req) {
        // BUG #8: Swapped title and description — title gets description value and vice versa
        Task updated = taskManager.updateTask(
                id, req.description(), req.title(),
                req.category(), req.priority(), req.dueDate());
        if (req.notes() != null) updated.setNotes(req.notes());
        if (req.timeZone() != null) updated.setTimeZone(req.timeZone());
        taskManager.save();
        return updated;
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Map<String, String>> completeTask(@PathVariable String id) {
        String msg = taskManager.markComplete(id);
        taskManager.save();
        if (msg != null) {
            return ResponseEntity.ok(Map.of("message", msg));
        }
        return ResponseEntity.ok(Map.of("message", "Task marked as complete"));
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<Map<String, String>> revokeTask(@PathVariable String id) {
        String msg = taskManager.revokeComplete(id);
        taskManager.save();
        if (msg != null) {
            return ResponseEntity.ok(Map.of("message", msg));
        }
        return ResponseEntity.ok(Map.of("message", "Task moved back to pending"));
    }

    @PatchMapping("/bulk-complete")
    public ResponseEntity<Map<String, String>> bulkComplete(@RequestBody List<String> ids) {
        int count = 0;
        for (String id : ids) {
            String msg = taskManager.markComplete(id);
            if (msg == null) count++;
        }
        taskManager.save();
        return ResponseEntity.ok(Map.of("message", count + " task(s) marked as complete"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskManager.deleteTask(id);
        taskManager.save();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear/completed")
    public ResponseEntity<Map<String, String>> clearCompleted() {
        List<String> ids = taskManager.getAllTasks().stream()
                .filter(Task::isCompleted).map(Task::getId).toList();
        ids.forEach(taskManager::deleteTask);
        taskManager.save();
        return ResponseEntity.ok(Map.of("message", ids.size() + " completed task(s) cleared"));
    }

    @DeleteMapping("/clear/pending")
    public ResponseEntity<Map<String, String>> clearPending() {
        List<String> ids = taskManager.getAllTasks().stream()
                .filter(t -> !t.isCompleted()).map(Task::getId).toList();
        ids.forEach(taskManager::deleteTask);
        taskManager.save();
        return ResponseEntity.ok(Map.of("message", ids.size() + " pending task(s) cleared"));
    }

    @DeleteMapping("/clear/all")
    public ResponseEntity<Map<String, String>> clearAll() {
        int count = taskManager.getAllTasks().size();
        taskManager.getAllTasks().stream().map(Task::getId).toList()
                .forEach(taskManager::deleteTask);
        taskManager.save();
        return ResponseEntity.ok(Map.of("message", count + " task(s) cleared"));
    }

    @GetMapping("/summary")
    public TaskManager.ProgressSummary getProgress() {
        return taskManager.getProgressSummary();
    }

    @GetMapping("/daily")
    public Object getDailySummary(@RequestParam(required = false) LocalDate date) {
        return taskManager.getDailySummary(date != null ? date : LocalDate.now());
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, String>> seedSampleTasks() {
        if (!taskManager.getAllTasks().isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Tasks already exist, skipping seed"));
        }
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusDays(7);

        // WORK tasks
        Task t1 = taskManager.createTask("Review quarterly report", "Go through Q1 numbers and prepare summary", Category.WORK, Priority.HIGH, today);
        t1.setNotes("Check revenue figures and compare with last quarter"); t1.setTimeZone("America/New_York");
        Task t2 = taskManager.createTask("Update project documentation", "Refresh API docs and README", Category.WORK, Priority.MEDIUM, tomorrow);
        t2.setNotes("Focus on the new endpoints added this sprint"); t2.setTimeZone("America/New_York");
        Task t3 = taskManager.createTask("Code review for PR #142", "Review backend changes", Category.WORK, Priority.HIGH, today);
        t3.setTimeZone("America/New_York");
        Task t4 = taskManager.createTask("Submit expense report", "Monthly expenses for March", Category.WORK, Priority.LOW, nextWeek);

        // PERSONAL tasks
        Task t5 = taskManager.createTask("Gym workout", "Leg day + 20 min cardio", Category.PERSONAL, Priority.MEDIUM, today);
        t5.setNotes("Don't skip stretching"); t5.setTimeZone("America/New_York");
        Task t6 = taskManager.createTask("Call dentist for appointment", "Schedule annual checkup", Category.PERSONAL, Priority.LOW, tomorrow);
        Task t7 = taskManager.createTask("Grocery shopping", "Weekly groceries", Category.PERSONAL, Priority.MEDIUM, today);
        t7.setNotes("Milk, eggs, bread, chicken, vegetables, fruits");
        Task t8 = taskManager.createTask("Read 30 pages of book", "Currently reading Clean Code", Category.PERSONAL, Priority.LOW, today);

        // TRAVEL tasks
        taskManager.createTravelTask("Flight to Chicago for conference", "Annual tech conference", Priority.HIGH, tomorrow, "Chicago", TravelMode.FLIGHT);
        taskManager.createTravelTask("Drive to client office", "Quarterly review meeting", Priority.MEDIUM, nextWeek, "Downtown Office Park", TravelMode.CAR);
        TravelTask tt = taskManager.createTravelTask("Train to NYC for workshop", "Design thinking workshop", Priority.MEDIUM, today.plusDays(3), "New York City", TravelMode.TRAIN);
        tt.setNotes("Book quiet car, bring laptop charger"); tt.setTimeZone("America/New_York");

        // FOOD tasks
        taskManager.createFoodTask("Team lunch at Italian place", "Celebrate sprint completion", Priority.MEDIUM, today, MealType.LUNCH);
        taskManager.createFoodTask("Prep breakfast for the week", "Overnight oats and smoothie packs", Priority.LOW, tomorrow, MealType.BREAKFAST);
        FoodTask ft = taskManager.createFoodTask("Dinner reservation", "Anniversary dinner", Priority.HIGH, today.plusDays(5), MealType.DINNER);
        ft.setNotes("Reservation at 7pm, confirm +1"); ft.setTimeZone("America/New_York");

        // MEETING tasks
        taskManager.createMeetingTask("Daily standup", "15-min sync with the team", Priority.HIGH, today, "Conference Room A", "Dev team", "https://meet.example.com/standup");
        taskManager.createMeetingTask("1-on-1 with manager", "Weekly check-in", Priority.MEDIUM, tomorrow, "Manager's office", "Manager", null);
        MeetingTask mt = taskManager.createMeetingTask("Sprint planning", "Plan next 2-week sprint", Priority.HIGH, today.plusDays(2), "Board Room", "Full team", "https://meet.example.com/sprint");
        mt.setNotes("Prepare backlog items and estimates"); mt.setTimeZone("America/New_York");
        taskManager.createMeetingTask("Client demo", "Show new features to stakeholders", Priority.HIGH, nextWeek, "Virtual", "Client team, PM, Dev leads", "https://meet.example.com/demo");

        taskManager.save();
        return ResponseEntity.ok(Map.of("message", "Seeded " + taskManager.getAllTasks().size() + " sample tasks"));
    }

    // ---- Request DTOs ----

    public record CreateTaskRequest(
            String title, String description, Category category,
            Priority priority, LocalDate dueDate,
            String taskType,
            String destination, TravelMode travelMode,
            MealType mealType,
            String location, String attendees, String meetingLink,
            String notes, String timeZone) {}

    public record UpdateTaskRequest(
            String title, String description, Category category,
            Priority priority, LocalDate dueDate,
            String notes, String timeZone) {}
}
