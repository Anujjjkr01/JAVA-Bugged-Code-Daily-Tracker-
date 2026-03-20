package com.taskmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.exception.StorageException;
import com.taskmanager.model.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes tasks from JSON files using Jackson with polymorphic type handling.
 */
public class TaskParser {

    private final ObjectMapper objectMapper;

    public TaskParser() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Loads tasks from a JSON file.
     *
     * @param filePath path to the JSON file
     * @return list of deserialized tasks, or empty list if file is missing/empty
     * @throws StorageException if the JSON is invalid
     */
    public List<Task> loadTasks(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            String content = Files.readString(filePath);
            if (content.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(content, new TypeReference<List<Task>>() {});
        } catch (IOException e) {
            throw new StorageException("Failed to parse tasks from file: " + filePath, e);
        }
    }
}
