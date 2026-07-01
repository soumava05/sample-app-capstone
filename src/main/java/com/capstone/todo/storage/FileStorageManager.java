package com.capstone.todo.storage;

import com.capstone.todo.exception.StorageException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileStorageManager {

    private final ObjectMapper objectMapper;

    public FileStorageManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> List<T> readList(Path filePath, Class<T> elementType) {
        if (Files.notExists(filePath)) {
            return new ArrayList<>();
        }

        try {
            JavaType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);
            List<T> values = objectMapper.readValue(filePath.toFile(), listType);
            return values != null ? new ArrayList<>(values) : new ArrayList<>();
        } catch (IOException exception) {
            throw new StorageException("Failed to read storage file: " + filePath, exception);
        }
    }

    public <T> void writeList(Path filePath, List<T> values) {
        try {
            createParentDirectories(filePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), values);
        } catch (IOException exception) {
            throw new StorageException("Failed to write storage file: " + filePath, exception);
        }
    }

    private void createParentDirectories(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }
}