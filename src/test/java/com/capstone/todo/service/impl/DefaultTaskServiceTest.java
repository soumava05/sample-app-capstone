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
    public void getUserTasksShouldReturnAllTasksWhenStatusIsAllAndSearchBlank() {
        List<TodoTask> repositoryTasks = List.of(
            todoTask("task-1", "alice", "Prepare report", "Monthly summary", TaskStatus.OPEN),
            todoTask("task-2", "alice", "Review notes", "Finalize release", TaskStatus.COMPLETED)
        );
        when(taskRepository.findByUsername("alice")).thenReturn(repositoryTasks);

        List<TodoTask> tasks = taskService.getUserTasks("Alice", "ALL", "   ");

        assertEquals(tasks, repositoryTasks);
    }

    @Test
    public void getUserTasksShouldFilterByStatus() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            todoTask("task-1", "alice", "Prepare report", "Monthly summary", TaskStatus.OPEN),
            todoTask("task-2", "alice", "Review notes", "Finalize release", TaskStatus.COMPLETED)
        ));

        List<TodoTask> tasks = taskService.getUserTasks("Alice", "completed", null);

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getId(), "task-2");
    }

    @Test
    public void getUserTasksShouldFilterBySearchAcrossTitleAndDescription() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            todoTask("task-1", "alice", "Prepare report", "Monthly summary", TaskStatus.OPEN),
            todoTask("task-2", "alice", "Review notes", "Finalize release", TaskStatus.COMPLETED)
        ));

        List<TodoTask> tasks = taskService.getUserTasks("Alice", "ALL", "  RELEASE ");

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getId(), "task-2");
    }

    @Test
    public void getUserTasksShouldCombineStatusAndSearchFilters() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            todoTask("task-1", "alice", "Release report", "Monthly summary", TaskStatus.OPEN),
            todoTask("task-2", "alice", "Release notes", "Finalize release", TaskStatus.COMPLETED),
            todoTask("task-3", "alice", "Prepare backlog", "Release planning", TaskStatus.COMPLETED)
        ));

        List<TodoTask> tasks = taskService.getUserTasks("Alice", "COMPLETED", "notes");

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getId(), "task-2");
    }

    @Test
    public void getUserTasksShouldFallbackToAllForInvalidStatus() {
        List<TodoTask> repositoryTasks = List.of(
            todoTask("task-1", "alice", "Prepare report", "Monthly summary", TaskStatus.OPEN),
            todoTask("task-2", "alice", "Review notes", "Finalize release", TaskStatus.COMPLETED)
        );
        when(taskRepository.findByUsername("alice")).thenReturn(repositoryTasks);

        List<TodoTask> tasks = taskService.getUserTasks("Alice", "INVALID", null);

        assertEquals(tasks, repositoryTasks);
    }

    private TodoTask todoTask(String id, String username, String title, String description, TaskStatus status) {
        return new TodoTask(
            id,
            username,
            title,
            description,
            LocalDate.of(2026, 6, 20),
            LocalDate.of(2026, 6, 21),
            status,
            LocalDateTime.of(2026, 6, 20, 10, 0)
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
