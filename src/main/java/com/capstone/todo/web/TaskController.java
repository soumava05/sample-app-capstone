package com.capstone.todo.web;

import com.capstone.todo.dto.TaskFilterCriteria;
import com.capstone.todo.dto.TaskFilterStatus;
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
                                @RequestParam(name = "status", required = false, defaultValue = "ALL") TaskFilterStatus status,
                                @RequestParam(name = "from", required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                @RequestParam(name = "to", required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                Model model) {
        String username = authentication.getName();
        TaskFilterCriteria filterCriteria = buildFilterCriteria(status, from, to);

        if (from != null && to != null && from.isAfter(to)) {
            model.addAttribute("tasks", taskService.getUserTasks(username));
            model.addAttribute("filterError", "From date cannot be after To date.");
        } else {
            model.addAttribute("tasks", taskService.getUserTasks(username, filterCriteria));
        }

        populateDashboardModel(model, username, filterCriteria);
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
            model.addAttribute("filterCriteria", new TaskFilterCriteria());
            model.addAttribute("username", username);
            return "tasks";
        }

        try {
            taskService.createTask(username, taskForm);
            return "redirect:/tasks";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("tasks", taskService.getUserTasks(username));
            model.addAttribute("filterCriteria", new TaskFilterCriteria());
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

    private TaskFilterCriteria buildFilterCriteria(TaskFilterStatus status, LocalDate from, LocalDate to) {
        TaskFilterCriteria filterCriteria = new TaskFilterCriteria();
        filterCriteria.setStatus(status);
        filterCriteria.setFrom(from);
        filterCriteria.setTo(to);
        return filterCriteria;
    }

    private void populateDashboardModel(Model model, String username, TaskFilterCriteria filterCriteria) {
        model.addAttribute("taskForm", new TaskForm());
        model.addAttribute("username", username);
        model.addAttribute("filterCriteria", filterCriteria);
        model.addAttribute("filterStatuses", TaskFilterStatus.values());
    }
}
