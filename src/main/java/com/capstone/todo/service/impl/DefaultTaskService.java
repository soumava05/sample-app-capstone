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
import java.util.stream.Collectors;

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
    public List<TodoTask> getFilteredTasks(String username, TaskStatus filter, String keyword) {
        List<TodoTask> tasks = taskRepository.findByUsername(normalizeUsername(username));

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        return tasks.stream()
            .filter(task -> filter == null || task.getStatus() == filter)
            .filter(task -> normalizedKeyword.isEmpty() || matchesKeyword(task, normalizedKeyword))
            .collect(Collectors.toList());
    }

    @Override
    public void markCompleted(String username, String taskId) {
        TodoTask task = taskRepository.findById(normalizeUsername(username), taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.update(task);
    }

    private boolean matchesKeyword(TodoTask task, String normalizedKeyword) {
        String title = task.getTitle() == null ? "" : task.getTitle().toLowerCase(Locale.ROOT);
        String description = task.getDescription() == null ? "" : task.getDescription().toLowerCase(Locale.ROOT);
        return title.contains(normalizedKeyword) || description.contains(normalizedKeyword);
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
