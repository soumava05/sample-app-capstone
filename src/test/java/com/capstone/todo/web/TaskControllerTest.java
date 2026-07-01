package com.capstone.todo.web;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.service.TaskService;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    public void taskDashboardShouldPopulateModelAndReturnTasksViewWithDefaults() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getFilteredTasks(eq("john"), isNull(), eq(""))).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(authentication, "ALL", null, model);

        assertEquals(view, "tasks");
        assertNotNull(model.getAttribute("tasks"));
        assertNotNull(model.getAttribute("taskForm"));
        assertEquals(model.getAttribute("username"), "john");
        assertEquals(model.getAttribute("filter"), "ALL");
        assertEquals(model.getAttribute("keyword"), "");
    }

    @Test
    public void taskDashboardShouldPassOpenFilterToService() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getFilteredTasks(eq("john"), eq(TaskStatus.OPEN), eq("meeting"))).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(authentication, "OPEN", "  meeting  ", model);

        assertEquals(view, "tasks");
        assertEquals(model.getAttribute("filter"), "OPEN");
        assertEquals(model.getAttribute("keyword"), "meeting");
        verify(taskService).getFilteredTasks("john", TaskStatus.OPEN, "meeting");
    }

    @Test
    public void taskDashboardShouldPassCompletedFilterToService() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getFilteredTasks(eq("john"), eq(TaskStatus.COMPLETED), eq(""))).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(authentication, "completed", null, model);

        assertEquals(view, "tasks");
        assertEquals(model.getAttribute("filter"), "COMPLETED");
        verify(taskService).getFilteredTasks("john", TaskStatus.COMPLETED, "");
    }

    @Test
    public void taskDashboardShouldDefaultToAllForInvalidFilter() {
        org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("john");
        when(taskService.getFilteredTasks(eq("john"), isNull(), eq(""))).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = taskController.taskDashboard(authentication, "UNKNOWN", null, model);

        assertEquals(view, "tasks");
        assertEquals(model.getAttribute("filter"), "ALL");
        verify(taskService).getFilteredTasks("john", null, "");
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
