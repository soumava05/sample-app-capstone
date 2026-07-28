package com.capstone.todo.service;

import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskForm;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    TodoTask createTask(String username, TaskForm taskForm);

    List<TodoTask> getUserTasks(String username);

    List<TodoTask> getUserTasks(String username, String status, LocalDate fromDate, LocalDate toDate);

    void markCompleted(String username, String taskId);
}
