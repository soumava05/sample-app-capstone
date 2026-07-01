package com.capstone.todo.service;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskForm;

import java.util.List;

public interface TaskService {

    TodoTask createTask(String username, TaskForm taskForm);

    List<TodoTask> getUserTasks(String username);

    List<TodoTask> getFilteredTasks(String username, TaskStatus filter, String keyword);

    void markCompleted(String username, String taskId);
}
