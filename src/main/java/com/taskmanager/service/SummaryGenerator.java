package com.taskmanager.service;

import com.taskmanager.model.Category;
import com.taskmanager.model.Task;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates daily activity summaries grouped by category.
 */
public class SummaryGenerator {

    private final Scheduler scheduler;

    public SummaryGenerator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * A record representing the summary for a single category on a given date.
     */
    public record DailySummary(int completedCount, int pendingCount, List<Task> tasks) {
        public DailySummary {
            tasks = tasks != null ? Collections.unmodifiableList(tasks) : List.of();
        }
    }

    /**
     * Wrapper result holding the category-to-summary map and an optional informational message.
     */
    public record DailySummaryResult(Map<Category, DailySummary> summaries, String message) {
    }

    /**
     * Generates a daily summary for the given date. Tasks are retrieved via the Scheduler,
     * then grouped by category with completed/pending counts per category.
     *
     * @param tasks the full list of tasks
     * @param date  the date to summarize
     * @return a DailySummaryResult containing the grouped summary and an optional message
     */
    public DailySummaryResult generateDailySummary(List<Task> tasks, LocalDate date) {
        List<Task> tasksForDate = scheduler.getTasksForDate(tasks, date);

        if (tasksForDate.isEmpty()) {
            return new DailySummaryResult(
                    Collections.emptyMap(),
                    "No tasks scheduled for " + date
            );
        }

        Map<Category, List<Task>> grouped = tasksForDate.stream()
                .collect(Collectors.groupingBy(Task::getCategory));

        Map<Category, DailySummary> summaries = new LinkedHashMap<>();
        for (Map.Entry<Category, List<Task>> entry : grouped.entrySet()) {
            List<Task> categoryTasks = entry.getValue();
            int completed = (int) categoryTasks.stream().filter(Task::isCompleted).count();
            int pending = categoryTasks.size() - completed;
            summaries.put(entry.getKey(), new DailySummary(completed, pending, categoryTasks));
        }

        return new DailySummaryResult(summaries, null);
    }
}
