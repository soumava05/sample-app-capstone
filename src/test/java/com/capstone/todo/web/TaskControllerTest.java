package com.capstone.todo.web;

import com.capstone.todo.dto.TaskFilterForm;
import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.service.TaskService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
    public void taskDashboardShouldPopulateModelAndReturnTasksView() {
        Authentication authentication = authentication("john");
        when(taskService.getUserTasks(eq("john"), Mockito.any(TaskFilterForm.class))).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(authentication, new TaskFilterForm(), model);

        assertEquals(view, "tasks");
        assertNotNull(model.getAttribute("tasks"));
        assertNotNull(model.getAttribute("taskForm"));
        assertNotNull(model.getAttribute("filter"));
        assertEquals(model.getAttribute("username"), "john");
    }

    @Test
    public void taskDashboardShouldCallServiceWithBoundFilter() {
        Authentication authentication = authentication("john");
        when(taskService.getUserTasks(anyString(), Mockito.any(TaskFilterForm.class))).thenReturn(List.of());

        TaskFilterForm filter = new TaskFilterForm();
        filter.setKeyword("plan");
        filter.setStatus("OPEN");

        taskController.taskDashboard(authentication, filter, new ConcurrentModel());

        ArgumentCaptor<TaskFilterForm> captor = ArgumentCaptor.forClass(TaskFilterForm.class);
        verify(taskService).getUserTasks(eq("john"), captor.capture());
        assertEquals(captor.getValue().getKeyword(), "plan");
        assertEquals(captor.getValue().getStatus(), "OPEN");
    }

    @Test
    public void taskDashboardShouldReturnTasksViewWithErrorWhenDateRangeIsInvalid() {
        Authentication authentication = authentication("john");
        TaskFilterForm filter = new TaskFilterForm();
        filter.setDateFrom(java.time.LocalDate.of(2026, 6, 21));
        filter.setDateTo(java.time.LocalDate.of(2026, 6, 20));

        doThrow(new IllegalArgumentException("Date From cannot be after Date To"))
            .when(taskService).getUserTasks("john", filter);
        when(taskService.getUserTasks("john")).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = taskController.taskDashboard(authentication, filter, model);

        assertEquals(view, "tasks");
        assertEquals(model.getAttribute("filterError"), "Date From cannot be after Date To");
        assertNotNull(model.getAttribute("tasks"));
    }

    @Test
    public void createTaskShouldReturnTasksViewWhenValidationFails() {
        Authentication authentication = authentication("john");
        when(taskService.getUserTasks("john")).thenReturn(List.of());

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = taskController.createTask(authentication, new TaskForm(), bindingResult, model);

        assertEquals(view, "tasks");
        assertNotNull(model.getAttribute("filter"));
    }

    @Test
    public void createTaskShouldRedirectWhenSuccessful() {
        Authentication authentication = authentication("john");

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = taskController.createTask(authentication, new TaskForm(), bindingResult, new ConcurrentModel());

        verify(taskService).createTask(anyString(), any(TaskForm.class));
        assertEquals(view, "redirect:/tasks");
    }

    @Test
    public void createTaskShouldRejectAndReturnTasksViewWhenServiceThrows() {
        Authentication authentication = authentication("john");
        when(taskService.getUserTasks("john")).thenReturn(List.of());

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("Planned finish date cannot be before task date"))
            .when(taskService).createTask(anyString(), any(TaskForm.class));

        Model model = new ConcurrentModel();
        String view = taskController.createTask(authentication, new TaskForm(), bindingResult, model);

        verify(bindingResult).reject("task.error", "Planned finish date cannot be before task date");
        assertEquals(view, "tasks");
    }

    @Test
    public void markTaskCompletedShouldDelegateAndRedirect() {
        Authentication authentication = authentication("john");

        String view = taskController.markTaskCompleted(authentication, "task-1");

        verify(taskService).markCompleted("john", "task-1");
        assertEquals(view, "redirect:/tasks");
    }

    private Authentication authentication(String username) {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        return authentication;
    }
}
