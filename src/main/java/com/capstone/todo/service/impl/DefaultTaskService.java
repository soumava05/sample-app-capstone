package com.capstone.todo.service.impl;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.repository.TaskRepository;
import com.capstone.todo.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DefaultTaskService implements TaskService {

    private static final String STATUS_ALL = "ALL";

    private final TaskRepository taskRepository;

    public DefaultTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TodoTask createTask(String username, TaskForm taskForm) {
        validateTaskDates(taskForm);

        TodoTask task = new TodoTask(
            UUID.randomUUID().toString(),
            normalizeUsername(username),
            taskForm.getTitle().trim(),
            taskForm.getDescription() == null ? "" : taskForm.getDescription().trim(),
            taskForm.getTaskDate(),
            taskForm.getPlannedFinishDate(),
            TaskStatus.OPEN,
            LocalDateTime.now()
        );

        return taskRepository.save(task);
    }

    @Override
    public List<TodoTask> getUserTasks(String username) {
        return taskRepository.findByUsername(normalizeUsername(username));
    }

    @Override
    public List<TodoTask> getUserTasks(String username, String status, String search) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedSearch = normalizeSearch(search);

        return taskRepository.findByUsername(normalizeUsername(username)).stream()
            .filter(task -> matchesStatus(task, normalizedStatus))
            .filter(task -> matchesSearch(task, normalizedSearch))
            .toList();
    }

    @Override
    public void markCompleted(String username, String taskId) {
        TodoTask task = taskRepository.findById(normalizeUsername(username), taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.update(task);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_ALL;
        }

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (STATUS_ALL.equals(normalizedStatus)) {
            return STATUS_ALL;
        }

        try {
            TaskStatus.valueOf(normalizedStatus);
            return normalizedStatus;
        } catch (IllegalArgumentException exception) {
            return STATUS_ALL;
        }
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return "";
        }
        return search.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesStatus(TodoTask task, String status) {
        return STATUS_ALL.equals(status) || task.getStatus().name().equals(status);
    }

    private boolean matchesSearch(TodoTask task, String search) {
        if (search.isBlank()) {
            return true;
        }

        return containsIgnoreCase(task.getTitle(), search) || containsIgnoreCase(task.getDescription(), search);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private void validateTaskDates(TaskForm taskForm) {
        if (taskForm.getPlannedFinishDate().isBefore(taskForm.getTaskDate())) {
            throw new IllegalArgumentException("Planned finish date cannot be before task date");
        }
    }
}
