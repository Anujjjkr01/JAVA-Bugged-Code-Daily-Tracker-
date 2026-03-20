package com.taskmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.exception.StorageException;
import com.taskmanager.model.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Serializes tasks to human-readable JSON using Jackson with indented output.
 */
public class PrettyPrinter {

    private final ObjectMapper objectMapper;

    public PrettyPrinter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serializes a list of tasks to a human-readable JSON file.
     *
     * @param tasks    the tasks to serialize
     * @param filePath the file to write to
     * @throws StorageException if writing fails
     */
    public void saveTasks(List<Task> tasks, Path filePath) {
        try {
            Path parent = filePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerFor(objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Task.class))
                    .writeValue(filePath.toFile(), tasks);
        } catch (IOException e) {
            throw new StorageException("Failed to save tasks to file: " + filePath, e);
        }
    }

    /**
     * Formats a single task to a human-readable JSON string.
     *
     * @param task the task to format
     * @return JSON string representation
     * @throws StorageException if serialization fails
     */
    public String formatTask(Task task) {
        try {
            return objectMapper.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            throw new StorageException("Failed to format task to JSON", e);
        }
    }
}
