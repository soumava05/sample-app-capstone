# sample_todo_app Details

## Jira Requirement

- Jira: EPMCDMETST-55854
- Type: Story
- Summary: Add task list filtering (Open/Completed) and search by title/description
- Goal: Enhance the Todo Dashboard UX by enabling logged-in users to quickly find tasks.

## Functional Requirements

1. Add optional query parameters to `GET /tasks`: `status` (`OPEN`, `COMPLETED`, `ALL`) and `q` (free-text search).
2. Filter tasks by status when `status=OPEN` or `status=COMPLETED`.
3. Treat missing, blank, or `ALL` status as no status filter.
4. Filter tasks when title or description contains `q`, case-insensitive.
5. Allow combined status and search filters.
6. Preserve current sort order: `taskDate`, then `createdAt`.
7. Preserve user isolation: only authenticated user's tasks are queried.
8. Update `tasks.html` with a server-rendered GET filter/search form above the task list.
9. Preserve create-task form and mark-complete button.
10. Display current filter state and a clear no-results empty state.
11. Preserve existing behavior when no filters are provided.

## Non-Functional Requirements

- Security: `/tasks` remains authenticated by Spring Security; username must come from `Authentication`, not request parameters.
- Maintainability: preserve controller/service/repository/template separation.
- Compatibility: keep Thymeleaf server rendering; do not convert to SPA.
- Testability: update service and controller tests for status, search, combined filters, and no-filter behavior.
- Performance: in-memory service filtering is acceptable for the current file-backed sample app.

## Codebase Analysis Findings

### App Type

Server-rendered Java web application for user-specific todo management.

### Technology Stack

- Java 21
- Spring Boot 3.3.2
- Spring MVC
- Spring Security with form login and BCrypt password hashing
- Thymeleaf templates
- Jackson JSON persistence with Java Time module
- Maven
- TestNG and Mockito
- Local file-system JSON persistence under `storage/`

### Architecture Pattern

Layered modular monolith:

- `web`: MVC controllers (`AuthController`, `TaskController`)
- `service`: service interfaces (`UserService`, `TaskService`)
- `service.impl`: business logic (`DefaultUserService`, `DefaultTaskService`)
- `repository`: persistence interfaces (`UserRepository`, `TaskRepository`)
- `repository.impl`: file-backed implementations (`FileUserRepository`, `FileTaskRepository`)
- `domain`: core models (`User`, `TodoTask`, `TaskStatus`)
- `dto`: form DTOs and validation (`RegistrationForm`, `TaskForm`)
- `config`: application, Jackson, and Spring Security config
- `templates`: Thymeleaf views (`login.html`, `register.html`, `tasks.html`)

### Current Relevant Behavior

- `GET /tasks` loads all tasks for `authentication.getName()` through `taskService.getUserTasks(username)`.
- `DefaultTaskService.getUserTasks` normalizes username and delegates to `TaskRepository.findByUsername`.
- `FileTaskRepository.findByUsername` reads `storage/tasks/{username}.json` and sorts by `taskDate`, then `createdAt`.
- `POST /tasks` creates a task after DTO and service date validation.
- `POST /tasks/{taskId}/complete` marks an authenticated user's task as completed.

### Recommended Design

- Introduce a lightweight `TaskFilter` DTO or equivalent service method parameters.
- Extend `TaskService` with filtered retrieval while preserving existing no-filter method if useful for compatibility.
- Apply filtering in `DefaultTaskService` after user-scoped repository lookup.
- Keep `FileTaskRepository` contract and sorting behavior unchanged.
- Update `TaskController.taskDashboard` to accept optional `status` and `q` request parameters and populate model attributes for filter state.
- Update `tasks.html` with GET form fields named `status` and `q`, current filter display, clear action, and no-results message.

### Risks and Considerations

- File-backed in-memory filtering is not optimized for high task volumes, but is suitable for this app.
- Query matching must be null-safe for description and case-insensitive for title/description.
- Invalid status input should fail safely, preferably defaulting to `ALL` or returning a controlled validation message.
- Redirects after create/complete should remain unchanged unless filter-preservation is separately requested.
