package com.capstone.todo.service.impl;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskForm;
import com.capstone.todo.repository.TaskRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

public class DefaultTaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private AutoCloseable mocks;
    private DefaultTaskService taskService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        taskService = new DefaultTaskService(taskRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void createTaskShouldSaveOpenTaskWithNormalizedUsername() {
        TaskForm taskForm = taskForm(
            "  Prepare release notes  ",
            "  Include deployment checklist  ",
            LocalDate.of(2026, 6, 20),
            LocalDate.of(2026, 6, 21)
        );

        when(taskRepository.save(any(TodoTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodoTask createdTask = taskService.createTask("  Alice  ", taskForm);

        ArgumentCaptor<TodoTask> taskCaptor = ArgumentCaptor.forClass(TodoTask.class);
        verify(taskRepository).save(taskCaptor.capture());

        TodoTask savedTask = taskCaptor.getValue();
        assertEquals(savedTask.getUsername(), "alice");
        assertEquals(savedTask.getTitle(), "Prepare release notes");
        assertEquals(savedTask.getDescription(), "Include deployment checklist");
        assertEquals(savedTask.getStatus(), TaskStatus.OPEN);
        assertNotNull(savedTask.getId());
        assertNotNull(savedTask.getCreatedAt());
        assertEquals(createdTask.getUsername(), "alice");
    }

    @Test
    public void createTaskShouldFailWhenPlannedFinishIsBeforeTaskDate() {
        TaskForm taskForm = taskForm(
            "Prepare docs",
            "desc",
            LocalDate.of(2026, 6, 21),
            LocalDate.of(2026, 6, 20)
        );

        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class,
            () -> taskService.createTask("alice", taskForm));

        assertEquals(exception.getMessage(), "Planned finish date cannot be before task date");
    }

    @Test
    public void markCompletedShouldUpdateTaskStatus() {
        TodoTask existingTask = new TodoTask(
            "task-1",
            "alice",
            "Task",
            "Desc",
            LocalDate.of(2026, 6, 20),
            LocalDate.of(2026, 6, 21),
            TaskStatus.OPEN,
            LocalDateTime.now()
        );

        when(taskRepository.findById("alice", "task-1")).thenReturn(Optional.of(existingTask));

        taskService.markCompleted("  ALICE ", "task-1");

        ArgumentCaptor<TodoTask> taskCaptor = ArgumentCaptor.forClass(TodoTask.class);
        verify(taskRepository).update(taskCaptor.capture());
        assertEquals(taskCaptor.getValue().getStatus(), TaskStatus.COMPLETED);
    }

    @Test
    public void markCompletedShouldFailWhenTaskDoesNotExist() {
        when(taskRepository.findById("alice", "task-404")).thenReturn(Optional.empty());

        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class,
            () -> taskService.markCompleted("Alice", "task-404"));

        assertEquals(exception.getMessage(), "Task not found");
    }

    @Test
    public void getUserTasksShouldNormalizeUsernameBeforeLookup() {
        TodoTask todoTask = new TodoTask();
        todoTask.setId("task-1");
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(todoTask));

        List<TodoTask> tasks = taskService.getUserTasks("  ALICE ");

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getId(), "task-1");
    }

    @Test
    public void getUserTasksShouldReturnAllTasksWhenNoFiltersSupplied() {
        List<TodoTask> tasks = sampleTasks();
        when(taskRepository.findByUsername("alice")).thenReturn(tasks);

        List<TodoTask> filteredTasks = taskService.getUserTasks("alice", null, null, null);

        assertEquals(filteredTasks.size(), 3);
    }

    @Test
    public void getUserTasksShouldFilterByOpenStatus() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks("alice", "OPEN", null, null);

        assertEquals(filteredTasks.size(), 2);
        assertEquals(filteredTasks.get(0).getStatus(), TaskStatus.OPEN);
        assertEquals(filteredTasks.get(1).getStatus(), TaskStatus.OPEN);
    }

    @Test
    public void getUserTasksShouldFilterByCompletedStatus() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks("alice", "COMPLETED", null, null);

        assertEquals(filteredTasks.size(), 1);
        assertEquals(filteredTasks.get(0).getStatus(), TaskStatus.COMPLETED);
    }

    @Test
    public void getUserTasksShouldIgnoreAllStatusFilter() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks("alice", "ALL", null, null);

        assertEquals(filteredTasks.size(), 3);
    }

    @Test
    public void getUserTasksShouldFilterByFromDateInclusively() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks("alice", null, LocalDate.of(2026, 6, 15), null);

        assertEquals(filteredTasks.size(), 2);
        assertEquals(filteredTasks.get(0).getId(), "task-2");
        assertEquals(filteredTasks.get(1).getId(), "task-3");
    }

    @Test
    public void getUserTasksShouldFilterByToDateInclusively() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks("alice", null, null, LocalDate.of(2026, 6, 15));

        assertEquals(filteredTasks.size(), 2);
        assertEquals(filteredTasks.get(0).getId(), "task-1");
        assertEquals(filteredTasks.get(1).getId(), "task-2");
    }

    @Test
    public void getUserTasksShouldFilterByInclusiveDateRange() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks(
            "alice",
            null,
            LocalDate.of(2026, 6, 15),
            LocalDate.of(2026, 6, 20)
        );

        assertEquals(filteredTasks.size(), 2);
        assertEquals(filteredTasks.get(0).getId(), "task-2");
        assertEquals(filteredTasks.get(1).getId(), "task-3");
    }

    @Test
    public void getUserTasksShouldFilterByCombinedStatusAndDateRange() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks(
            "alice",
            "OPEN",
            LocalDate.of(2026, 6, 15),
            LocalDate.of(2026, 6, 30)
        );

        assertEquals(filteredTasks.size(), 1);
        assertEquals(filteredTasks.get(0).getId(), "task-3");
    }

    @Test
    public void getUserTasksShouldValidateInvalidDateRange() {
        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class,
            () -> taskService.getUserTasks(
                "alice",
                "OPEN",
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 6, 1)
            ));

        assertEquals(exception.getMessage(), "From date cannot be after to date");
    }

    @Test
    public void getUserTasksShouldNormalizeUsernameWhenFiltering() {
        when(taskRepository.findByUsername("alice")).thenReturn(sampleTasks());

        List<TodoTask> filteredTasks = taskService.getUserTasks("  ALICE ", "OPEN", null, null);

        assertEquals(filteredTasks.size(), 2);
    }

    private List<TodoTask> sampleTasks() {
        return List.of(
            todoTask("task-1", LocalDate.of(2026, 6, 10), TaskStatus.OPEN),
            todoTask("task-2", LocalDate.of(2026, 6, 15), TaskStatus.COMPLETED),
            todoTask("task-3", LocalDate.of(2026, 6, 20), TaskStatus.OPEN)
        );
    }

    private TodoTask todoTask(String id, LocalDate taskDate, TaskStatus status) {
        return new TodoTask(
            id,
            "alice",
            "Task " + id,
            "Desc",
            taskDate,
            taskDate.plusDays(1),
            status,
            LocalDateTime.now()
        );
    }

    private TaskForm taskForm(String title, String description, LocalDate taskDate, LocalDate plannedFinishDate) {
        TaskForm taskForm = new TaskForm();
        taskForm.setTitle(title);
        taskForm.setDescription(description);
        taskForm.setTaskDate(taskDate);
        taskForm.setPlannedFinishDate(plannedFinishDate);
        return taskForm;
    }
}
