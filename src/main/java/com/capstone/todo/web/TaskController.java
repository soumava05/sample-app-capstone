package com.capstone.todo.web;

import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
                                @RequestParam(defaultValue = DEFAULT_STATUS) String status,
                                @RequestParam(required = false) String search) {
        String username = authentication.getName();
        model.addAttribute("tasks", taskService.getUserTasks(username, status, search));
        model.addAttribute("taskForm", new TaskForm());
        model.addAttribute("username", username);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchTerm", search == null ? "" : search);
        return "tasks";
    }

    @PostMapping("/tasks")
    public String createTask(Authentication authentication,
                             @Valid @ModelAttribute("taskForm") TaskForm taskForm,
                             BindingResult bindingResult,
                             Model model) {
        String username = authentication.getName();

        if (bindingResult.hasErrors()) {
            model.addAttribute("tasks", taskService.getUserTasks(username, DEFAULT_STATUS, null));
            model.addAttribute("username", username);
            model.addAttribute("selectedStatus", DEFAULT_STATUS);
            model.addAttribute("searchTerm", "");
            return "tasks";
        }

        try {
            taskService.createTask(username, taskForm);
            return "redirect:/tasks";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("tasks", taskService.getUserTasks(username, DEFAULT_STATUS, null));
            model.addAttribute("username", username);
            model.addAttribute("selectedStatus", DEFAULT_STATUS);
            model.addAttribute("searchTerm", "");
            bindingResult.reject("task.error", exception.getMessage());
            return "tasks";
        }
    }

    @PostMapping("/tasks/{taskId}/complete")
    public String markTaskCompleted(Authentication authentication, @PathVariable String taskId) {
        taskService.markCompleted(authentication.getName(), taskId);
        return "redirect:/tasks";
    }
}
