package com.capstone.todo.repository.impl;

import com.capstone.todo.config.AppStorageProperties;
import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.storage.FileStorageManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class FileTaskRepositoryTest {

    private Path testStorageRoot;
    private FileTaskRepository fileTaskRepository;

    @BeforeMethod
    public void setUp() throws IOException {
        testStorageRoot = Files.createTempDirectory("todo-task-repo-test-");

        AppStorageProperties appStorageProperties = new AppStorageProperties();
        appStorageProperties.setRootPath(testStorageRoot.toString());

        FileStorageManager fileStorageManager = new FileStorageManager(objectMapper());
        fileTaskRepository = new FileTaskRepository(appStorageProperties, fileStorageManager);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        if (testStorageRoot != null) {
            deleteRecursively(testStorageRoot);
        }
    }

    @Test
    public void saveAndFindByUsernameShouldReturnTasksSortedByTaskDateThenCreatedAt() {
        TodoTask task2 = createTask("2", "alice", LocalDate.of(2026, 6, 22), LocalDateTime.of(2026, 6, 20, 11, 0));
        TodoTask task1 = createTask("1", "alice", LocalDate.of(2026, 6, 21), LocalDateTime.of(2026, 6, 20, 12, 0));
        TodoTask task3 = createTask("3", "alice", LocalDate.of(2026, 6, 21), LocalDateTime.of(2026, 6, 20, 10, 0));

        fileTaskRepository.save(task2);
        fileTaskRepository.save(task1);
        fileTaskRepository.save(task3);

        List<TodoTask> tasks = fileTaskRepository.findByUsername("alice");

        assertEquals(tasks.size(), 3);
        assertEquals(tasks.get(0).getId(), "3");
        assertEquals(tasks.get(1).getId(), "1");
        assertEquals(tasks.get(2).getId(), "2");
    }

    @Test
    public void findByIdShouldReturnTaskWhenItExists() {
        TodoTask task = createTask("task-1", "alice", LocalDate.of(2026, 6, 21), LocalDateTime.now());
        fileTaskRepository.save(task);

        TodoTask foundTask = fileTaskRepository.findById("alice", "task-1").orElseThrow();

        assertEquals(foundTask.getId(), "task-1");
        assertEquals(foundTask.getUsername(), "alice");
    }

    @Test
    public void updateShouldPersistModifiedTask() {
        TodoTask task = createTask("task-1", "alice", LocalDate.of(2026, 6, 21), LocalDateTime.now());
        fileTaskRepository.save(task);

        task.setStatus(TaskStatus.COMPLETED);
        fileTaskRepository.update(task);

        TodoTask updatedTask = fileTaskRepository.findById("alice", "task-1").orElseThrow();
        assertEquals(updatedTask.getStatus(), TaskStatus.COMPLETED);

        Path taskFile = testStorageRoot.resolve("tasks").resolve("alice.json");
        assertTrue(Files.exists(taskFile));
        assertNotNull(updatedTask.getCreatedAt());
    }

    @Test
    public void updateShouldFailWhenTaskDoesNotExist() {
        TodoTask missingTask = createTask("missing", "alice", LocalDate.of(2026, 6, 21), LocalDateTime.now());

        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class,
            () -> fileTaskRepository.update(missingTask));

        assertTrue(exception.getMessage().contains("Task not found"));
    }

    private TodoTask createTask(String id, String username, LocalDate taskDate, LocalDateTime createdAt) {
        return new TodoTask(
            id,
            username,
            "Task " + id,
            "Description " + id,
            taskDate,
            taskDate.plusDays(1),
            TaskStatus.OPEN,
            createdAt
        );
    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            try (var children = Files.list(path)) {
                for (Path child : children.toList()) {
                    deleteRecursively(child);
                }
            }
        }

        Files.deleteIfExists(path);
    }
}
