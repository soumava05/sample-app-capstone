package com.capstone.todo.service.impl;

import com.capstone.todo.domain.TaskStatus;
import com.capstone.todo.domain.TodoTask;
import com.capstone.todo.dto.TaskFilterForm;
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
        TodoTask existingTask = task(
            "task-1",
            "alice",
            "Task",
            "Desc",
            LocalDate.of(2026, 6, 20),
            TaskStatus.OPEN
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
    public void getUserTasksShouldFilterByKeywordInTitleCaseInsensitively() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            task("task-1", "alice", "Prepare Release Notes", "Desc", LocalDate.of(2026, 6, 20), TaskStatus.OPEN),
            task("task-2", "alice", "Sprint Demo", "Desc", LocalDate.of(2026, 6, 21), TaskStatus.OPEN)
        ));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setKeyword("release");

        List<TodoTask> tasks = taskService.getUserTasks("Alice", filter);

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getId(), "task-1");
    }

    @Test
    public void getUserTasksShouldFilterByKeywordInDescriptionCaseInsensitively() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            task("task-1", "alice", "Task One", "Plan Production Rollout", LocalDate.of(2026, 6, 20), TaskStatus.OPEN),
            task("task-2", "alice", "Task Two", "Backlog refinement", LocalDate.of(2026, 6, 21), TaskStatus.OPEN)
        ));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setKeyword("rollout");

        List<TodoTask> tasks = taskService.getUserTasks("Alice", filter);

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getId(), "task-1");
    }

    @Test
    public void getUserTasksShouldFilterByOpenStatus() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            task("task-1", "alice", "Open Task", "Desc", LocalDate.of(2026, 6, 20), TaskStatus.OPEN),
            task("task-2", "alice", "Completed Task", "Desc", LocalDate.of(2026, 6, 21), TaskStatus.COMPLETED)
        ));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setStatus("OPEN");

        List<TodoTask> tasks = taskService.getUserTasks("Alice", filter);

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getStatus(), TaskStatus.OPEN);
    }

    @Test
    public void getUserTasksShouldFilterByCompletedStatus() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            task("task-1", "alice", "Open Task", "Desc", LocalDate.of(2026, 6, 20), TaskStatus.OPEN),
            task("task-2", "alice", "Completed Task", "Desc", LocalDate.of(2026, 6, 21), TaskStatus.COMPLETED)
        ));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setStatus("COMPLETED");

        List<TodoTask> tasks = taskService.getUserTasks("Alice", filter);

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getStatus(), TaskStatus.COMPLETED);
    }

    @Test
    public void getUserTasksShouldTreatAllStatusAsNoStatusFilter() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            task("task-1", "alice", "Open Task", "Desc", LocalDate.of(2026, 6, 20), TaskStatus.OPEN),
            task("task-2", "alice", "Completed Task", "Desc", LocalDate.of(2026, 6, 21), TaskStatus.COMPLETED)
        ));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setStatus("ALL");

        List<TodoTask> tasks = taskService.getUserTasks("Alice", filter);

        assertEquals(tasks.size(), 2);
    }

    @Test
    public void getUserTasksShouldApplyCombinedFiltersWithAndSemantics() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            task("task-1", "alice", "Release Plan", "Prod rollout", LocalDate.of(2026, 6, 20), TaskStatus.OPEN),
            task("task-2", "alice", "Release Plan", "Prod rollout", LocalDate.of(2026, 6, 21), TaskStatus.COMPLETED),
            task("task-3", "alice", "Backlog", "Prod rollout", LocalDate.of(2026, 6, 20), TaskStatus.OPEN)
        ));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setKeyword("release");
        filter.setStatus("OPEN");
        filter.setDateFrom(LocalDate.of(2026, 6, 20));
        filter.setDateTo(LocalDate.of(2026, 6, 20));

        List<TodoTask> tasks = taskService.getUserTasks("Alice", filter);

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0).getId(), "task-1");
    }

    @Test
    public void getUserTasksShouldApplyInclusiveDateRange() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(
            task("task-1", "alice", "Day One", "Desc", LocalDate.of(2026, 6, 20), TaskStatus.OPEN),
            task("task-2", "alice", "Day Two", "Desc", LocalDate.of(2026, 6, 21), TaskStatus.OPEN),
            task("task-3", "alice", "Day Three", "Desc", LocalDate.of(2026, 6, 22), TaskStatus.OPEN)
        ));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setDateFrom(LocalDate.of(2026, 6, 20));
        filter.setDateTo(LocalDate.of(2026, 6, 21));

        List<TodoTask> tasks = taskService.getUserTasks("Alice", filter);

        assertEquals(tasks.size(), 2);
        assertEquals(tasks.get(0).getId(), "task-1");
        assertEquals(tasks.get(1).getId(), "task-2");
    }

    @Test
    public void getUserTasksShouldFailForInvalidDateRange() {
        when(taskRepository.findByUsername("alice")).thenReturn(List.of(task(
            "task-1", "alice", "Task", "Desc", LocalDate.of(2026, 6, 20), TaskStatus.OPEN
        )));

        TaskFilterForm filter = new TaskFilterForm();
        filter.setDateFrom(LocalDate.of(2026, 6, 21));
        filter.setDateTo(LocalDate.of(2026, 6, 20));

        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class,
            () -> taskService.getUserTasks("Alice", filter));

        assertEquals(exception.getMessage(), "Date From cannot be after Date To");
    }

    private TaskForm taskForm(String title, String description, LocalDate taskDate, LocalDate plannedFinishDate) {
        TaskForm taskForm = new TaskForm();
        taskForm.setTitle(title);
        taskForm.setDescription(description);
        taskForm.setTaskDate(taskDate);
        taskForm.setPlannedFinishDate(plannedFinishDate);
        return taskForm;
    }

    private TodoTask task(String id, String username, String title, String description, LocalDate taskDate, TaskStatus status) {
        return new TodoTask(
            id,
            username,
            title,
            description,
            taskDate,
            taskDate.plusDays(1),
            status,
            LocalDateTime.now()
        );
    }
}
