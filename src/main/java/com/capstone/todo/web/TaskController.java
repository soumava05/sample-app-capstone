package com.capstone.todo.web;

import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class TaskController {

    private static final String DEFAULT_STATUS = "ALL";

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/tasks";
    }

    @GetMapping("/tasks")
    public String taskDashboard(Authentication authentication,
                                Model model,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        String username = authentication.getName();
        String selectedStatus = normalizeSelectedStatus(status);

        try {
            model.addAttribute("tasks", taskService.getUserTasks(username, selectedStatus, fromDate, toDate));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("tasks", taskService.getUserTasks(username));
            model.addAttribute("filterError", exception.getMessage());
        }

        populateDashboardModel(model, username, selectedStatus, fromDate, toDate);
        return "tasks";
    }

    @PostMapping("/tasks")
    public String createTask(Authentication authentication,
                             @Valid @ModelAttribute("taskForm") TaskForm taskForm,
                             BindingResult bindingResult,
                             Model model) {
        String username = authentication.getName();

        if (bindingResult.hasErrors()) {
            model.addAttribute("tasks", taskService.getUserTasks(username));
            populateDashboardModel(model, username, DEFAULT_STATUS, null, null);
            return "tasks";
        }

        try {
            taskService.createTask(username, taskForm);
            return "redirect:/tasks";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("tasks", taskService.getUserTasks(username));
            populateDashboardModel(model, username, DEFAULT_STATUS, null, null);
            bindingResult.reject("task.error", exception.getMessage());
            return "tasks";
        }
    }

    @PostMapping("/tasks/{taskId}/complete")
    public String markTaskCompleted(Authentication authentication, @PathVariable String taskId) {
        taskService.markCompleted(authentication.getName(), taskId);
        return "redirect:/tasks";
    }

    private void populateDashboardModel(Model model,
                                        String username,
                                        String selectedStatus,
                                        LocalDate fromDate,
                                        LocalDate toDate) {
        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", new TaskForm());
        }
        model.addAttribute("username", username);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
    }

    private String normalizeSelectedStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS;
        }
        return status.trim().toUpperCase();
    }
}
