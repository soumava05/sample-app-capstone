# sample_todo_app Details

## Jira Requirement

- **Ticket:** EPMCDMETST-55996
- **Type:** Story
- **Summary:** Add task list filters using status and task date range to improve Todo Dashboard usability.
- **Primary actor:** Authenticated Todo user
- **Business value:** Improves productivity by reducing time to locate relevant tasks as task volume grows.

## Functional Requirements

1. Display filter controls above the task list on the Todo Dashboard.
2. Support status filter values: `All`, `Open`, and `Completed`.
3. Default status filter value is `All`.
4. Support optional task date range filters using `From` and `To` fields.
5. Apply `from` as `taskDate >= from` when present.
6. Apply `to` as `taskDate <= to` when present.
7. Combine status and date filters together.
8. Preserve sorting by `taskDate` then `createdAt`.
9. Preserve filter selections after applying filters by reflecting query parameters in the URL.
10. Show an empty-state message when no tasks match filters.
11. If `From > To`, display a validation error and show the unfiltered list or last valid state without server error.

## Non-Functional Requirements

- Preserve existing Spring Security session-based authentication.
- Preserve user data isolation; only the authenticated user's tasks may be filtered and displayed.
- Preserve file-based JSON persistence; no database dependency is required.
- Maintain layered boundaries between controller, service, repository, storage, DTO, and domain layers.
- Add or update tests for service filtering logic and controller query parameter handling.

## Codebase Analysis Findings

### App Type

Server-rendered Java web application: Spring Boot MVC application with Thymeleaf templates and file-based persistence.

### Technology Stack

- Java 21
- Spring Boot 3.3.2
- Spring MVC
- Spring Security
- Thymeleaf
- Jakarta Bean Validation
- Jackson with JavaTimeModule
- Maven
- TestNG and Mockito
- Local JSON files under `storage/`

### Architecture Pattern

The app follows a layered modular monolith pattern:

- `web`: MVC controllers for HTTP request handling.
- `dto`: form models and input validation.
- `service` and `service.impl`: business workflows.
- `repository` and `repository.impl`: persistence contracts and file-backed implementations.
- `storage`: file I/O utility layer referenced by repositories.
- `domain`: `User`, `TodoTask`, and `TaskStatus` domain models.
- `config`: Spring Security, Jackson, and storage property configuration.

### Current Relevant Components

- `TaskController`: handles `/tasks`, task creation, and completion.
- `TaskService`: currently exposes `createTask`, `getUserTasks`, and `markCompleted`.
- `DefaultTaskService`: normalizes usernames, validates task dates, creates tasks, gets tasks, and marks completion.
- `TaskRepository`: retrieves, saves, finds, and updates user-scoped tasks.
- `FileTaskRepository`: reads and writes `storage/tasks/<username>.json`, returning tasks sorted by `taskDate` then `createdAt`.
- `TodoTask`: stores `id`, `username`, `title`, `description`, `taskDate`, `plannedFinishDate`, `status`, and `createdAt`.
- `TaskStatus`: enum values `OPEN` and `COMPLETED`.
- `tasks.html`: displays task creation and task list; filter controls need to be added here.

## Proposed Design Summary

Implement filtering in the service layer after retrieving the authenticated user's sorted task list. Keep repositories unchanged to preserve file-based persistence abstraction and minimize scope. Extend the GET `/tasks` controller to accept optional request parameters `status`, `from`, and `to`, validate date ranges, and populate filter state back into the model for Thymeleaf rendering.

## Proposed API and Model Changes

### Controller

`GET /tasks?status=<ALL|OPEN|COMPLETED>&from=<yyyy-MM-dd>&to=<yyyy-MM-dd>`

- `status`: optional; defaults to `ALL`.
- `from`: optional ISO date.
- `to`: optional ISO date.
- Invalid range produces model-level filter error and displays unfiltered tasks.

### Service

Recommended method: `List<TodoTask> getUserTasks(String username, TaskFilterCriteria criteria)`.

Recommended criteria fields:

- `status`: `ALL`, `OPEN`, or `COMPLETED`.
- `from`: optional `LocalDate` lower bound.
- `to`: optional `LocalDate` upper bound.

### Template

Add a GET form above the task list with status select, from date input, to date input, apply button, clear filters link to `/tasks`, and empty-state message `No tasks match your filters.`

## Business Rules

1. Only authenticated users can access task dashboard and filters.
2. Filters apply only to tasks owned by the authenticated user.
3. `ALL` status does not filter by status.
4. `OPEN` shows only tasks with `TaskStatus.OPEN`.
5. `COMPLETED` shows only tasks with `TaskStatus.COMPLETED`.
6. Missing `from` does not apply a lower task date bound.
7. Missing `to` does not apply an upper task date bound.
8. When both dates exist and `from` is after `to`, a validation error is shown and no server error occurs.
9. Sorting remains `taskDate` ascending then `createdAt` ascending.
10. Create and complete flows continue to function as before.

## Alternatives Considered

### Alternative A: Controller-level Filtering

- **Pros:** Minimal service API changes.
- **Cons:** Puts business rules in controller, weakens testability, violates layered design.
- **Decision:** Not recommended.

### Alternative B: Service-level In-Memory Filtering

- **Pros:** Aligns with existing architecture, simple, testable, no persistence changes, preserves sorting.
- **Cons:** Less efficient for very large per-user task lists.
- **Decision:** Recommended for current scope.

### Alternative C: Repository-level Filtering

- **Pros:** More scalable if later backed by database queries.
- **Cons:** Unnecessary complexity for JSON storage, duplicates filtering details across persistence layer.
- **Decision:** Defer until storage requirements change.

## Security Considerations

- Use `Authentication.getName()` as the only source of task owner identity.
- Do not accept username as request parameter.
- Keep task repository calls scoped by normalized username.
- Continue using Spring Security form login and BCrypt for passwords.
- Avoid exposing storage file paths in errors.

## Observability and Error Handling

- Validation errors should be shown in UI via model attributes or BindingResult-equivalent model errors.
- Invalid enum/date query values should fail gracefully and present a user-friendly filter error.
- Existing server logs are sufficient for this small monolith; consider structured logging if productionized.

## Test Strategy

Add or update:

- `DefaultTaskServiceTest`: status-only, from-only, to-only, combined, all status, invalid range behavior if validation is placed in service.
- `TaskControllerTest`: default filter state, query parameter binding, invalid date range model error, persistence of filter values.
- Template smoke/manual check: filter form displayed above list and empty state shown.

## Rollout Plan

1. Add filter criteria DTO or service method signature.
2. Implement service filtering while preserving existing `getUserTasks(username)` compatibility if useful.
3. Extend `TaskController.taskDashboard` to accept query parameters.
4. Update `tasks.html` filter form and empty-state logic.
5. Add tests.
6. Run `mvn test`.
7. Manual verification with authenticated user and tasks across statuses and dates.

## Open Questions

- Whether invalid date range should show an unfiltered list or last valid filter state. Jira allows either; recommended implementation is unfiltered list for deterministic server-rendered behavior.
