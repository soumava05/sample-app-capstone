package com.capstone.todo.storage;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class FileStorageManager {

    private final ObjectMapper objectMapper;
    private final ConcurrentMap<Path, ReentrantLock> fileLocks = new ConcurrentHashMap<>();

    public FileStorageManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> List<T> readList(Path filePath, Class<T> elementType) {
        Path normalizedPath = filePath.toAbsolutePath().normalize();
        ReentrantLock lock = fileLocks.computeIfAbsent(normalizedPath, ignored -> new ReentrantLock());
        lock.lock();
        try {
            if (Files.notExists(normalizedPath) || Files.size(normalizedPath) == 0) {
                ensureParentDirectoryExists(normalizedPath);
                return new ArrayList<>();
            }

            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            List<T> values = objectMapper.readValue(normalizedPath.toFile(), listType);
            return values == null ? new ArrayList<>() : new ArrayList<>(values);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read file: " + normalizedPath, exception);
        } finally {
            lock.unlock();
        }
    }

    public <T> void writeList(Path filePath, List<T> values) {
        Path normalizedPath = filePath.toAbsolutePath().normalize();
        ReentrantLock lock = fileLocks.computeIfAbsent(normalizedPath, ignored -> new ReentrantLock());
        lock.lock();
        try {
            ensureParentDirectoryExists(normalizedPath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(normalizedPath.toFile(), values == null ? List.of() : values);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write file: " + normalizedPath, exception);
        } finally {
            lock.unlock();
        }
    }

    private void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
