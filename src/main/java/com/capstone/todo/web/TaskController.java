package com.capstone.todo.web;

import com.capstone.todo.domain.TaskStatus;
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

import java.util.Locale;

@Controller
public class TaskController {

    private static final String FILTER_ALL = "ALL";

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
                                @RequestParam(name = "filter", defaultValue = FILTER_ALL) String filter,
                                @RequestParam(name = "keyword", required = false) String keyword,
                                Model model) {
        String username = authentication.getName();
        String normalizedFilter = normalizeFilter(filter);
        TaskStatus statusFilter = resolveStatusFilter(normalizedFilter);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        model.addAttribute("tasks", taskService.getFilteredTasks(username, statusFilter, normalizedKeyword));
        model.addAttribute("taskForm", new TaskForm());
        model.addAttribute("username", username);
        model.addAttribute("filter", normalizedFilter);
        model.addAttribute("keyword", normalizedKeyword);
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
            model.addAttribute("username", username);
            return "tasks";
        }

        try {
            taskService.createTask(username, taskForm);
            return "redirect:/tasks";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("tasks", taskService.getUserTasks(username));
            model.addAttribute("username", username);
            bindingResult.reject("task.error", exception.getMessage());
            return "tasks";
        }
    }

    @PostMapping("/tasks/{taskId}/complete")
    public String markTaskCompleted(Authentication authentication, @PathVariable String taskId) {
        taskService.markCompleted(authentication.getName(), taskId);
        return "redirect:/tasks";
    }

    private String normalizeFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            return FILTER_ALL;
        }
        String upper = filter.trim().toUpperCase(Locale.ROOT);
        if (FILTER_ALL.equals(upper) || TaskStatus.OPEN.name().equals(upper) || TaskStatus.COMPLETED.name().equals(upper)) {
            return upper;
        }
        // Unknown/invalid filter values default to ALL (graceful handling; design does not specify).
        return FILTER_ALL;
    }

    private TaskStatus resolveStatusFilter(String normalizedFilter) {
        if (TaskStatus.OPEN.name().equals(normalizedFilter)) {
            return TaskStatus.OPEN;
        }
        if (TaskStatus.COMPLETED.name().equals(normalizedFilter)) {
            return TaskStatus.COMPLETED;
        }
        return null; // ALL
    }
}
