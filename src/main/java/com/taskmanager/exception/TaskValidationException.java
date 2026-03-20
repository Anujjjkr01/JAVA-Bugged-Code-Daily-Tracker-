package com.taskmanager.exception;

/**
 * Thrown when task validation fails (empty title, past due date,
 * missing destination, invalid category/priority).
 */
public class TaskValidationException extends RuntimeException {

    public TaskValidationException(String message) {
        super(message);
    }

    public TaskValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
