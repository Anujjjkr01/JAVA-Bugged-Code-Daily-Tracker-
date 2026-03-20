package com.taskmanager.service;

import com.taskmanager.model.Task;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduler component responsible for organizing tasks by date,
 * filtering by date ranges, and flagging overdue tasks.
 */
public class Scheduler {

    /**
     * Returns all tasks with a due date matching the specified date.
     *
     * @param tasks the list of tasks to filter
     * @param date  the target date
     * @return tasks with due date equal to the specified date
     */
    public List<Task> getTasksForDate(List<Task> tasks, LocalDate date) {
        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks with due dates falling within the specified range (inclusive),
     * ordered by due date ascending.
     *
     * @param tasks the list of tasks to filter
     * @param start the start date (inclusive)
     * @param end   the end date (inclusive)
     * @return tasks within the date range, sorted by due date ascending
     */
    public List<Task> getTasksForDateRange(List<Task> tasks, LocalDate start, LocalDate end) {
        return tasks.stream()
                .filter(task -> task.getDueDate() != null
                        && !task.getDueDate().isBefore(start)
                        && !task.getDueDate().isAfter(end))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    /**
     * Returns incomplete tasks whose due date is before today.
     *
     * @param tasks the list of tasks to check
     * @return overdue tasks (incomplete and past due)
     */
    public List<Task> getOverdueTasks(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(task -> !task.isCompleted()
                        && task.getDueDate() != null
                        && task.getDueDate().isBefore(today))
                .collect(Collectors.toList());
    }

    /**
     * Flags overdue tasks by setting their overdue field to true.
     * A task is overdue if it is incomplete and its due date is before today.
     *
     * @param tasks the list of tasks to flag
     */
    public void flagOverdueTasks(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        for (Task task : tasks) {
            if (!task.isCompleted()
                    && task.getDueDate() != null
                    && task.getDueDate().isBefore(today)) {
                task.setOverdue(true);
            }
        }
    }
}
