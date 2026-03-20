package com.taskmanager.exception;

/**
 * Thrown when attempting to edit or delete a task that does not exist.
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
