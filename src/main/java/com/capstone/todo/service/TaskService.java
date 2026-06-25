package com.capstone.todo.service;

import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskForm;

import java.util.List;

public interface TaskService {

    TodoTask createTask(String username, TaskForm taskForm);

    List<TodoTask> getUserTasks(String username);

    void markCompleted(String username, String taskId);
}
