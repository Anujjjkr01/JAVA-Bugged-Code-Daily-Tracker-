package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.taskmanager.exception.TaskValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = Task.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Task.class, name = "Task"),
        @JsonSubTypes.Type(value = TravelTask.class, name = "TravelTask"),
        @JsonSubTypes.Type(value = FoodTask.class, name = "FoodTask"),
        @JsonSubTypes.Type(value = MeetingTask.class, name = "MeetingTask")
})
@JsonTypeName("Task")
public class Task {

    private String id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dueDate;

    private boolean completed;
    private boolean overdue;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime completionTimestamp;

    private String notes;
    private String timeZone;
    private List<String> tags;

    /**
     * Default constructor for Jackson deserialization.
     */
    protected Task() {
        this.tags = new ArrayList<>();
    }

    /**
     * Full constructor with validation.
     */
    public Task(String title, String description, Category category, Priority priority, LocalDate dueDate) {
        validate(title, dueDate);
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.category = category != null ? category : Category.WORK;
        // BUG #1: Should default to MEDIUM, but defaults to LOW
        this.priority = priority != null ? priority : Priority.LOW;
        this.dueDate = dueDate;
        this.completed = false;
        this.completionTimestamp = null;
        this.tags = new ArrayList<>();
    }

    private static void validate(String title, LocalDate dueDate) {
        if (title == null || title.isBlank()) {
            throw new TaskValidationException("Task title cannot be empty");
        }
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new TaskValidationException("Due date cannot be in the past");
        }
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public LocalDateTime getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(LocalDateTime completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        // BUG #2: Truncates timezone string to 10 chars — breaks long timezone IDs like "America/New_York"
        if (timeZone != null && timeZone.length() > 10) {
            this.timeZone = timeZone.substring(0, 10);
        } else {
            this.timeZone = timeZone;
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return completed == task.completed
                && overdue == task.overdue
                && Objects.equals(id, task.id)
                && Objects.equals(title, task.title)
                && Objects.equals(description, task.description)
                && category == task.category
                && priority == task.priority
                && Objects.equals(dueDate, task.dueDate)
                && Objects.equals(completionTimestamp, task.completionTimestamp)
                && Objects.equals(notes, task.notes)
                && Objects.equals(timeZone, task.timeZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, category, priority, dueDate, completed, overdue, completionTimestamp, notes, timeZone);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", completed=" + completed +
                ", overdue=" + overdue +
                ", notes='" + notes + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", completionTimestamp=" + completionTimestamp +
                '}';
    }
}
