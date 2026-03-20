package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.taskmanager.exception.TaskValidationException;

import java.time.LocalDate;
import java.util.Objects;

@JsonTypeName("TravelTask")
public class TravelTask extends Task {

    private String destination;
    private TravelMode travelMode;

    /**
     * Default constructor for Jackson deserialization.
     */
    protected TravelTask() {
        super();
    }

    public TravelTask(String title, String description, Priority priority, LocalDate dueDate,
                      String destination, TravelMode travelMode) {
        super(title, description, Category.TRAVEL, priority, dueDate);
        if (destination == null || destination.isBlank()) {
            throw new TaskValidationException("Travel task must have a destination");
        }
        this.destination = destination;
        this.travelMode = travelMode;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TravelTask that = (TravelTask) o;
        return Objects.equals(destination, that.destination) && travelMode == that.travelMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), destination, travelMode);
    }

    @Override
    public String toString() {
        return "TravelTask{" +
                "id='" + getId() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", destination='" + destination + '\'' +
                ", travelMode=" + travelMode +
                ", dueDate=" + getDueDate() +
                ", completed=" + isCompleted() +
                '}';
    }
}
