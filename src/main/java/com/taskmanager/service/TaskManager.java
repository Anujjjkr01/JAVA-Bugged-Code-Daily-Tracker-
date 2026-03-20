package com.taskmanager.service;

import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.exception.TaskValidationException;
import com.taskmanager.model.*;
import com.taskmanager.service.SummaryGenerator.DailySummaryResult;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Facade class that coordinates all task management operations.
 * Delegates to TaskParser, PrettyPrinter, Scheduler, Filter, and SummaryGenerator.
 */
public class TaskManager {

    /**
     * A record representing the progress summary with completed and pending counts.
     */
    public record ProgressSummary(int completedCount, int pendingCount) {
    }

    private final Path filePath;
    private final TaskParser taskParser;
    private final PrettyPrinter prettyPrinter;
    private final Scheduler scheduler;
    private final Filter filter;
    private final SummaryGenerator summaryGenerator;
    private final List<Task> tasks;

    /**
     * Constructs a TaskManager that loads tasks from the given file path.
     *
     * @param filePath the path to the JSON file for task persistence
     */
    public TaskManager(String filePath) {
        this.filePath = Path.of(filePath);
        this.taskParser = new TaskParser();
        this.prettyPrinter = new PrettyPrinter();
        this.scheduler = new Scheduler();
        this.filter = new Filter();
        this.summaryGenerator = new SummaryGenerator(this.scheduler);
        this.tasks = new ArrayList<>(taskParser.loadTasks(this.filePath));
    }

    // ==================== CRUD Operations (Task 9.1) ====================

    /**
     * Creates a new Task with the given details and adds it to the task list.
     *
     * @return the created Task
     * @throws TaskValidationException if title is empty or due date is in the past
     */
    public Task createTask(String title, String description, Category category,
                           Priority priority, LocalDate dueDate) {
        Task task = new Task(title, description, category, priority, dueDate);
        tasks.add(task);
        return task;
    }

    /**
     * Creates a new Task with notes and timezone.
     */
    public Task createTask(String title, String description, Category category,
                           Priority priority, LocalDate dueDate, String notes, String timeZone) {
        Task task = new Task(title, description, category, priority, dueDate);
        // BUG #3: Sets timezone twice instead of setting notes then timezone
        task.setTimeZone(notes);
        task.setTimeZone(timeZone);
        tasks.add(task);
        return task;
    }

    /**
     * Creates a new TravelTask with the given details and adds it to the task list.
     *
     * @return the created TravelTask
     * @throws TaskValidationException if validation fails (empty title, past due date, missing destination)
     */
    public TravelTask createTravelTask(String title, String description, Priority priority,
                                       LocalDate dueDate, String destination, TravelMode travelMode) {
        TravelTask task = new TravelTask(title, description, priority, dueDate, destination, travelMode);
        tasks.add(task);
        return task;
    }

    /**
     * Creates a new FoodTask with the given details and adds it to the task list.
     * If mealType is null, defaults to LUNCH.
     *
     * @return the created FoodTask
     * @throws TaskValidationException if validation fails
     */
    public FoodTask createFoodTask(String title, String description, Priority priority,
                                   LocalDate dueDate, MealType mealType) {
        FoodTask task = new FoodTask(title, description, priority, dueDate, mealType);
        tasks.add(task);
        return task;
    }

    /**
     * Creates a new MeetingTask with the given details and adds it to the task list.
     */
    public MeetingTask createMeetingTask(String title, String description, Priority priority,
                                         LocalDate dueDate, String location, String attendees, String meetingLink) {
        MeetingTask task = new MeetingTask(title, description, priority, dueDate, location, attendees, meetingLink);
        tasks.add(task);
        return task;
    }

    /**
     * Updates an existing task's fields. Only non-null parameters are applied.
     *
     * @return the updated Task
     * @throws TaskNotFoundException if no task with the given ID exists
     */
    public Task updateTask(String id, String title, String description, Category category,
                           Priority priority, LocalDate dueDate) {
        Task task = findTaskByIdOrThrow(id);
        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (category != null) {
            validateCategoryChange(task, category);
            task.setCategory(category);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        return task;
    }

    /**
     * Validates that a category change is allowed for the given task.
     * TravelTask must remain TRAVEL; FoodTask must remain FOOD.
     *
     * @throws TaskValidationException if the category change is not permitted
     */
    private void validateCategoryChange(Task task, Category newCategory) {
        if (task instanceof TravelTask && newCategory != Category.TRAVEL) {
            throw new TaskValidationException(
                    "Cannot change category of a TravelTask away from TRAVEL");
        }
        if (task instanceof FoodTask && newCategory != Category.FOOD) {
            throw new TaskValidationException(
                    "Cannot change category of a FoodTask away from FOOD");
        }
    }

    /**
     * Deletes a task by its ID.
     *
     * @throws TaskNotFoundException if no task with the given ID exists
     */
    public void deleteTask(String id) {
        Task task = findTaskByIdOrThrow(id);
        tasks.remove(task);
    }

    /**
     * Marks a task as complete and records the completion timestamp.
     * If the task is already complete, returns an informational message
     * without changing the timestamp. Otherwise returns null.
     *
     * @return informational message if already complete, null otherwise
     * @throws TaskNotFoundException if no task with the given ID exists
     */
    public String markComplete(String id) {
        Task task = findTaskByIdOrThrow(id);
        if (task.isCompleted()) {
            return "Task '" + task.getTitle() + "' is already marked as complete.";
        }
        task.setCompleted(true);
        // BUG #6: Forgot to set completion timestamp
        return null;
    }

    /**
     * Revokes completion of a task, moving it back to pending.
     *
     * @throws TaskNotFoundException if no task with the given ID exists
     */
    // BUG #7: Revoke doesn't actually set completed to false — task stays completed
    public String revokeComplete(String id) {
        Task task = findTaskByIdOrThrow(id);
        if (!task.isCompleted()) {
            return "Task '" + task.getTitle() + "' is already pending.";
        }
        // Forgot to set completed to false!
        task.setCompletionTimestamp(null);
        return null;
    }

    // ==================== Delegation Methods (Task 9.5) ====================

    /**
     * Returns all tasks sorted by priority: HIGH → MEDIUM → LOW.
     * BUG #10: Sorts in reverse order — LOW first, HIGH last
     */
    public List<Task> getTasksSortedByPriority() {
        return tasks.stream()
                .sorted(Comparator.comparingInt((Task t) -> t.getPriority().ordinal()).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns tasks for a specific date. Delegates to Scheduler.
     */
    public List<Task> getTasksForDate(LocalDate date) {
        return scheduler.getTasksForDate(tasks, date);
    }

    /**
     * Returns tasks within a date range. Delegates to Scheduler.
     */
    public List<Task> getTasksForDateRange(LocalDate start, LocalDate end) {
        return scheduler.getTasksForDateRange(tasks, start, end);
    }

    /**
     * Filters tasks by the given criteria. Null parameters are skipped. Delegates to Filter.
     */
    public List<Task> filterTasks(Category category, Priority priority, Boolean completed, String keyword) {
        return filter.applyFilters(tasks, category, priority, completed, keyword);
    }

    /**
     * Returns a daily summary for the given date. Delegates to SummaryGenerator.
     */
    public DailySummaryResult getDailySummary(LocalDate date) {
        return summaryGenerator.generateDailySummary(tasks, date);
    }

    /**
     * Returns a progress summary with completed and pending task counts.
     */
    public ProgressSummary getProgressSummary() {
        int completed = (int) tasks.stream().filter(Task::isCompleted).count();
        int pending = tasks.size() - completed;
        return new ProgressSummary(completed, pending);
    }

    /**
     * Saves the current task list to the file. Delegates to PrettyPrinter.
     */
    public void save() {
        prettyPrinter.saveTasks(tasks, filePath);
    }

    /**
     * Returns a copy of the current task list.
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Finds a task by its ID.
     *
     * @throws TaskNotFoundException if no task with the given ID exists
     */
    public Task getTaskById(String id) {
        return findTaskByIdOrThrow(id);
    }

    // ==================== Private Helpers ====================

    private Task findTaskByIdOrThrow(String id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }
}
