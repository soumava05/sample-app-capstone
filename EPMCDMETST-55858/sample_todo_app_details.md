# sample_todo_app Details - EPMCDMETST-55858

## Jira Requirement

**Ticket:** EPMCDMETST-55858  
**Story:** Todo app: add Open/Completed filter and sort by planned finish date

### Functional Requirements

- Allow authenticated users to filter the Todo Dashboard task list by status: `All`, `OPEN`, and `COMPLETED`.
- Allow authenticated users to sort the visible task list by `plannedFinishDate` ascending or descending.
- Support combined filtering and sorting in the `/tasks` dashboard.
- Preserve existing task creation and completion workflows.
- Display active filter and sort selections in the Thymeleaf UI.

### Non-Functional Requirements

- Preserve user data isolation: every lookup must remain scoped to the authenticated username.
- Invalid query parameters must fall back to safe defaults and render `/tasks` without failure.
- Maintain layered architecture and avoid persistence logic in controllers.
- Add service and controller tests for filter/sort behavior.

## Codebase Analysis

### Application Type

Server-rendered Java web application built with Spring Boot MVC and Thymeleaf. It is a modular monolith with local file-based JSON persistence.

### Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring MVC, Thymeleaf |
| Security | Spring Security, form login, BCrypt |
| Validation | Jakarta Bean Validation |
| Persistence | JSON files through Jackson and file storage manager |
| Build | Maven |
| Tests | TestNG, Mockito |

### Existing Architecture Patterns

- Layered Architecture: `web` -> `service` -> `repository` -> `storage`.
- Repository Pattern: `TaskRepository`, `UserRepository` hide persistence implementation.
- Service Layer Pattern: business rules live in `DefaultTaskService` and `DefaultUserService`.
- DTO Pattern: `TaskForm`, `RegistrationForm` isolate form binding and validation.
- Configuration Pattern: `SecurityConfig`, `JacksonConfig`, `AppStorageProperties` wire framework concerns.

### Domain Model

| Entity | Key Fields | Notes |
|---|---|---|
| `User` | username, fullName, passwordHash, createdAt | Username is normalized to lowercase and used as identity. |
| `TodoTask` | id, username, title, description, taskDate, plannedFinishDate, status, createdAt | Task ownership is represented by username. |
| `TaskStatus` | OPEN, COMPLETED | Used for dashboard filtering. |

### Existing Routes

| Method | Route | Controller | Purpose |
|---|---|---|---|
| GET | `/` | TaskController | Redirects to `/tasks`. |
| GET | `/tasks` | TaskController | Shows authenticated user's tasks. |
| POST | `/tasks` | TaskController | Creates a task. |
| POST | `/tasks/{taskId}/complete` | TaskController | Marks a user-owned task completed. |
| GET | `/login` | AuthController | Shows login page. |
| GET | `/register` | AuthController | Shows registration page. |
| POST | `/register` | AuthController | Registers a user. |

## Design Decision for EPMCDMETST-55858

Implement filter/sort at the service layer over the authenticated user's task collection. Keep the repository contract simple because current persistence is file-based and already returns user-scoped task collections. Add a typed query model or enums for request parameters to avoid stringly typed business logic.

### Recommended Changes

- Extend `TaskService` with an overload such as `getUserTasks(String username, String statusFilter, String sortOrder)` or introduce a small query DTO such as `TaskListQuery`.
- Normalize invalid filter/sort parameters to defaults in service or controller helper methods.
- Preserve current `getUserTasks(String username)` as default behavior for compatibility.
- Update `TaskController.taskDashboard` to accept request parameters: `status` and `sort`.
- Populate model attributes: `selectedStatus`, `selectedSort`, `statusOptions`, `sortOptions`.
- Update `tasks.html` with GET controls for filter and sort.
- Add tests in `DefaultTaskServiceTest` and `TaskControllerTest`.

### Defaults

| Parameter | Allowed Values | Safe Default |
|---|---|---|
| `status` | `ALL`, `OPEN`, `COMPLETED` | `ALL` |
| `sort` | `PLANNED_FINISH_ASC`, `PLANNED_FINISH_DESC` | Existing ordering or `PLANNED_FINISH_ASC` based on final product choice |

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Invalid query params cause errors | Dashboard unavailable | Parse defensively and fallback to defaults. |
| Cross-user data exposure | Security defect | Always call service with `authentication.getName()` and repository `findByUsername`. |
| Null dates in legacy storage | Sort failure | Existing form requires planned finish date; still sort using null-safe comparator if needed. |
| Repository sorting conflicts | Unexpected order | Apply explicit final comparator in service after filtering. |

## Test Strategy

- Service tests: OPEN only, COMPLETED only, ALL, ascending planned finish date, descending planned finish date, combined filter/sort, invalid inputs fallback.
- Controller tests: query params are accepted; model contains tasks plus active filter/sort attributes; invalid inputs still render `tasks`.
- Existing regression tests for create/complete flows remain valid.
