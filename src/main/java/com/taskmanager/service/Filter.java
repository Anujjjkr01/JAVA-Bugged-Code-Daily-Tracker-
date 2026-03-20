package com.taskmanager.service;

import com.taskmanager.model.Category;
import com.taskmanager.model.Priority;
import com.taskmanager.model.Task;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides filtering and search capabilities for task lists.
 */
public class Filter {

    /**
     * Returns tasks matching the specified category.
     */
    public List<Task> filterByCategory(List<Task> tasks, Category category) {
        return tasks.stream()
                .filter(task -> task.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Returns tasks matching the specified priority.
     */
    public List<Task> filterByPriority(List<Task> tasks, Priority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    /**
     * Returns tasks matching the specified completion status.
     */
    public List<Task> filterByCompletion(List<Task> tasks, boolean completed) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toList());
    }

    /**
     * Returns tasks where the title or description contains the keyword (case-insensitive).
     * BUG #9: Search is case-SENSITIVE instead of case-insensitive — searching "meeting" won't find "Meeting"
     */
    public List<Task> searchByKeyword(List<Task> tasks, String keyword) {
        return tasks.stream()
                .filter(task -> {
                    String title = task.getTitle() != null ? task.getTitle() : "";
                    String description = task.getDescription() != null ? task.getDescription() : "";
                    return title.contains(keyword) || description.contains(keyword);
                })
                .collect(Collectors.toList());
    }

    /**
     * Combines multiple filters. Null parameters are skipped.
     */
    public List<Task> applyFilters(List<Task> tasks, Category category, Priority priority, Boolean completed, String keyword) {
        List<Task> result = tasks;
        if (category != null) {
            result = filterByCategory(result, category);
        }
        if (priority != null) {
            result = filterByPriority(result, priority);
        }
        if (completed != null) {
            result = filterByCompletion(result, completed);
        }
        if (keyword != null) {
            result = searchByKeyword(result, keyword);
        }
        return result;
    }
}
