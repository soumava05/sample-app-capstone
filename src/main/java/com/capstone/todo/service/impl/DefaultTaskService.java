package com.capstone.todo.service.impl;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskFilterForm;
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
        return getUserTasks(username, null);
    }

    @Override
    public List<TodoTask> getUserTasks(String username, TaskFilterForm taskFilterForm) {
        List<TodoTask> userTasks = taskRepository.findByUsername(normalizeUsername(username));
        if (taskFilterForm == null || !taskFilterForm.hasAnyFilter()) {
            return userTasks;
        }

        validateFilterDates(taskFilterForm);

        String normalizedKeyword = taskFilterForm.hasKeyword()
            ? taskFilterForm.getKeyword().trim().toLowerCase(Locale.ROOT)
            : null;
        TaskStatus status = taskFilterForm.getResolvedStatus();

        return userTasks.stream()
            .filter(task -> matchesKeyword(task, normalizedKeyword))
            .filter(task -> matchesStatus(task, status))
            .filter(task -> matchesDateFrom(task, taskFilterForm.getDateFrom()))
            .filter(task -> matchesDateTo(task, taskFilterForm.getDateTo()))
            .toList();
    }

    @Override
    public void markCompleted(String username, String taskId) {
        TodoTask task = taskRepository.findById(normalizeUsername(username), taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.update(task);
    }

    private boolean matchesKeyword(TodoTask task, String normalizedKeyword) {
        if (normalizedKeyword == null) {
            return true;
        }

        String title = task.getTitle() == null ? "" : task.getTitle().toLowerCase(Locale.ROOT);
        String description = task.getDescription() == null ? "" : task.getDescription().toLowerCase(Locale.ROOT);
        return title.contains(normalizedKeyword) || description.contains(normalizedKeyword);
    }

    private boolean matchesStatus(TodoTask task, TaskStatus status) {
        return status == null || task.getStatus() == status;
    }

    private boolean matchesDateFrom(TodoTask task, java.time.LocalDate dateFrom) {
        return dateFrom == null || !task.getTaskDate().isBefore(dateFrom);
    }

    private boolean matchesDateTo(TodoTask task, java.time.LocalDate dateTo) {
        return dateTo == null || !task.getTaskDate().isAfter(dateTo);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private void validateTaskDates(TaskForm taskForm) {
        if (taskForm.getPlannedFinishDate().isBefore(taskForm.getTaskDate())) {
            throw new IllegalArgumentException("Planned finish date cannot be before task date");
        }
    }

    private void validateFilterDates(TaskFilterForm taskFilterForm) {
        if (taskFilterForm.getDateFrom() != null
            && taskFilterForm.getDateTo() != null
            && taskFilterForm.getDateFrom().isAfter(taskFilterForm.getDateTo())) {
            throw new IllegalArgumentException("Date From cannot be after Date To");
        }
    }
}
