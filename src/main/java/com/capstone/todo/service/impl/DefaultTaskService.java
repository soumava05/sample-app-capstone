package com.capstone.todo.service.impl;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.repository.TaskRepository;
import com.capstone.todo.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DefaultTaskService implements TaskService {

    private static final String ALL_STATUS = "ALL";

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
        return getUserTasks(username, null, null, null);
    }

    @Override
    public List<TodoTask> getUserTasks(String username, String status, LocalDate fromDate, LocalDate toDate) {
        validateFilterRange(fromDate, toDate);

        return taskRepository.findByUsername(normalizeUsername(username)).stream()
            .filter(task -> matchesStatus(task, status))
            .filter(task -> matchesFromDate(task, fromDate))
            .filter(task -> matchesToDate(task, toDate))
            .toList();
    }

    @Override
    public void markCompleted(String username, String taskId) {
        TodoTask task = taskRepository.findById(normalizeUsername(username), taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.update(task);
    }

    private boolean matchesStatus(TodoTask task, String status) {
        if (status == null || status.isBlank() || ALL_STATUS.equalsIgnoreCase(status)) {
            return true;
        }

        return task.getStatus().name().equalsIgnoreCase(status.trim());
    }

    private boolean matchesFromDate(TodoTask task, LocalDate fromDate) {
        return fromDate == null || !task.getTaskDate().isBefore(fromDate);
    }

    private boolean matchesToDate(TodoTask task, LocalDate toDate) {
        return toDate == null || !task.getTaskDate().isAfter(toDate);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private void validateTaskDates(TaskForm taskForm) {
        if (taskForm.getPlannedFinishDate().isBefore(taskForm.getTaskDate())) {
            throw new IllegalArgumentException("Planned finish date cannot be before task date");
        }
    }

    private void validateFilterRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
    }
}
