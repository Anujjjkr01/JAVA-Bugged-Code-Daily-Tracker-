package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.LocalDate;
import java.util.Objects;

@JsonTypeName("MeetingTask")
public class MeetingTask extends Task {

    private String location;
    private String attendees;
    private String meetingLink;

    protected MeetingTask() {
        super();
    }

    public MeetingTask(String title, String description, Priority priority, LocalDate dueDate,
                       String location, String attendees, String meetingLink) {
        super(title, description, Category.MEETING, priority, dueDate);
        this.location = location;
        this.attendees = attendees;
        this.meetingLink = meetingLink;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAttendees() { return attendees; }
    public void setAttendees(String attendees) { this.attendees = attendees; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MeetingTask that = (MeetingTask) o;
        return Objects.equals(location, that.location)
                && Objects.equals(attendees, that.attendees)
                && Objects.equals(meetingLink, that.meetingLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), location, attendees, meetingLink);
    }

    @Override
    public String toString() {
        return "MeetingTask{" +
                "id='" + getId() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", location='" + location + '\'' +
                ", attendees='" + attendees + '\'' +
                ", dueDate=" + getDueDate() +
                ", completed=" + isCompleted() +
                '}';
    }
}
