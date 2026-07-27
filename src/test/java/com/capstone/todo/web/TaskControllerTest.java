package com.capstone.todo.web;

import com.capstone.todo.dto.TaskFilterCriteria;
import com.capstone.todo.dto.TaskFilterStatus;
import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.service.TaskService;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TaskControllerTest {

    private TaskService taskService;
    private TaskController taskController;

    @BeforeMethod
    public void setUp() {
        taskService = Mockito.mock(TaskService.class);
        taskController = new TaskController(taskService);
    }

    @Test
    public void rootRedirectShouldPointToTasks() {
        String view = taskController.rootRedirect();

        assertEquals(view, "redirect:/tasks");
    }

    @Test
    public void taskDashboardShouldPopulateDefaultFilterModelAndReturnTasksView() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getUserTasks(eq("john"), any(TaskFilterCriteria.class))).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(authentication, TaskFilterStatus.ALL, null, null, model);

        assertEquals(view, "tasks");
        assertNotNull(model.getAttribute("tasks"));
        assertNotNull(model.getAttribute("taskForm"));
        assertEquals(model.getAttribute("username"), "john");
        assertNotNull(model.getAttribute("filterCriteria"));
        assertNotNull(model.getAttribute("filterStatuses"));
    }

    @Test
    public void taskDashboardShouldUseQueryParametersForFiltering() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getUserTasks(eq("john"), any(TaskFilterCriteria.class))).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(
            authentication,
            TaskFilterStatus.OPEN,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            model
        );

        assertEquals(view, "tasks");
        TaskFilterCriteria filterCriteria = (TaskFilterCriteria) model.getAttribute("filterCriteria");
        assertEquals(filterCriteria.getStatus(), TaskFilterStatus.OPEN);
        assertEquals(filterCriteria.getFrom(), LocalDate.of(2026, 6, 1));
        assertEquals(filterCriteria.getTo(), LocalDate.of(2026, 6, 30));
        verify(taskService).getUserTasks(eq("john"), any(TaskFilterCriteria.class));
    }

    @Test
    public void taskDashboardShouldShowValidationErrorAndFallbackToUnfilteredTasksForInvalidRange() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getUserTasks("john")).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(
            authentication,
            TaskFilterStatus.ALL,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 6, 1),
            model
        );

        assertEquals(view, "tasks");
        assertEquals(model.getAttribute("filterError"), "From date cannot be after To date.");
        verify(taskService).getUserTasks("john");
    }

    @Test
    public void createTaskShouldReturnTasksViewWhenValidationFails() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getUserTasks("john")).thenReturn(List.of());

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = taskController.createTask(authentication, new TaskForm(), bindingResult, model);

        assertEquals(view, "tasks");
        assertNotNull(model.getAttribute("filterCriteria"));
    }

    @Test
    public void createTaskShouldRedirectWhenSuccessful() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = taskController.createTask(authentication, new TaskForm(), bindingResult, new ConcurrentModel());

        verify(taskService).createTask(anyString(), any(TaskForm.class));
        assertEquals(view, "redirect:/tasks");
    }

    @Test
    public void createTaskShouldRejectAndReturnTasksViewWhenServiceThrows() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getUserTasks("john")).thenReturn(List.of());

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("Planned finish date cannot be before task date"))
            .when(taskService).createTask(anyString(), any(TaskForm.class));

        Model model = new ConcurrentModel();
        String view = taskController.createTask(authentication, new TaskForm(), bindingResult, model);

        verify(bindingResult).reject("task.error", "Planned finish date cannot be before task date");
        assertEquals(view, "tasks");
        assertNotNull(model.getAttribute("filterCriteria"));
    }

    @Test
    public void markTaskCompletedShouldDelegateAndRedirect() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");

        String view = taskController.markTaskCompleted(authentication, "task-1");

        verify(taskService).markCompleted("john", "task-1");
        assertEquals(view, "redirect:/tasks");
    }
}
