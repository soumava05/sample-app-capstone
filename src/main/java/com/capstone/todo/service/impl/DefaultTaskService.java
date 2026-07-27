package com.capstone.todo.service.impl;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskFilterCriteria;
import com.capstone.todo.dto.TaskFilterStatus;
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
        return taskRepository.findByUsername(normalizeUsername(username));
    }

    @Override
    public List<TodoTask> getUserTasks(String username, TaskFilterCriteria criteria) {
        TaskFilterCriteria effectiveCriteria = criteria == null ? new TaskFilterCriteria() : criteria;
        TaskFilterStatus status = effectiveCriteria.getStatus() == null ? TaskFilterStatus.ALL : effectiveCriteria.getStatus();

        return getUserTasks(username).stream()
            .filter(task -> matchesStatus(task, status))
            .filter(task -> effectiveCriteria.getFrom() == null || !task.getTaskDate().isBefore(effectiveCriteria.getFrom()))
            .filter(task -> effectiveCriteria.getTo() == null || !task.getTaskDate().isAfter(effectiveCriteria.getTo()))
            .toList();
    }

    @Override
    public void markCompleted(String username, String taskId) {
        TodoTask task = taskRepository.findById(normalizeUsername(username), taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.update(task);
    }

    private boolean matchesStatus(TodoTask task, TaskFilterStatus status) {
        return switch (status) {
            case ALL -> true;
            case OPEN -> task.getStatus() == TaskStatus.OPEN;
            case COMPLETED -> task.getStatus() == TaskStatus.COMPLETED;
        };
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private void validateTaskDates(TaskForm taskForm) {
        if (taskForm.getPlannedFinishDate().isBefore(taskForm.getTaskDate())) {
            throw new IllegalArgumentException("Planned finish date cannot be before task date");
        }
    }
}
