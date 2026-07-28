# sample_todo_app Details — EPMCDMETST-55979

## Jira Requirement Summary

**Story:** Todo Dashboard: Add task filtering (status/date) for faster task navigation

Users with many tasks need filtering controls on `/tasks` so they can narrow visible tasks by task status and task date range. Filters must be applied only to the authenticated user's tasks and selected values must be preserved after applying filters.

## Functional Requirements

- Add Status filter on `/tasks` with values `ALL`, `OPEN`, and `COMPLETED`.
- Add optional inclusive date range filters: `fromDate` and `toDate`.
- Apply filters server-side through Spring MVC controller and service logic.
- Preserve selected filter values after the page reloads.
- Reset action clears filters and returns to all authenticated-user tasks.
- Automated tests must cover none, status only, date only, and combined filters.

## Non-Functional Requirements

- Preserve user isolation by using `Authentication.getName()` and normalized username before repository access.
- Keep current server-rendered Thymeleaf architecture; do not introduce SPA/client-only filtering.
- Maintain layered boundaries: controller handles request/model, service handles filtering rules, repository remains file-based persistence abstraction.
- Keep filtering deterministic and in-memory over the authenticated user's existing task list.
- Avoid adding database dependencies unless explicitly required.

## Codebase Analysis Findings

### Application Type

Server-rendered Todo web application using Spring Boot MVC, Spring Security, Thymeleaf, and local JSON file persistence.

### Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring MVC, Thymeleaf |
| Security | Spring Security form login, BCrypt password hashing |
| Validation | Jakarta Bean Validation |
| Persistence | Local file-system JSON files via Jackson |
| Build | Maven |
| Tests | TestNG, Mockito |

### Current Architecture Patterns

- **Layered MVC:** `web` → `service` → `repository` → `storage`.
- **Repository Pattern:** `TaskRepository` abstracts persistence; `FileTaskRepository` implements JSON file access.
- **Service Layer Pattern:** `TaskService` and `DefaultTaskService` centralize task workflows.
- **DTO Pattern:** `TaskForm` protects form binding and validation boundaries.
- **Configuration Pattern:** Security, Jackson, and storage properties are configured under `config`.

### Relevant Existing Components

| Component | Responsibility |
|---|---|
| `TaskController` | Handles `/`, `GET /tasks`, `POST /tasks`, and task completion routes. |
| `TaskService` | Contract for create, list, and complete task workflows. |
| `DefaultTaskService` | Normalizes usernames, validates task dates, creates tasks, marks tasks complete. |
| `TaskRepository` | Provides user-scoped task access and persistence abstraction. |
| `FileTaskRepository` | Reads/writes per-user task files under `storage/tasks/{username}.json`. |
| `TodoTask` | Domain model with task date, planned finish date, status, ownership, and audit fields. |
| `TaskStatus` | Existing enum with `OPEN` and `COMPLETED`. |
| `tasks.html` | Thymeleaf dashboard for creating and listing tasks. |

## Proposed Design Summary

Add a lightweight filtering model to the existing Spring MVC flow without changing persistence. The controller accepts optional query parameters, delegates filtering to the service, and repopulates the model with both filtered tasks and selected filter values. The service retrieves tasks for the normalized authenticated username, then applies status and date predicates in memory.

## Recommended API/Method Changes

- Extend `TaskService` with `getUserTasks(String username, TaskStatus status, LocalDate fromDate, LocalDate toDate)` or introduce a small `TaskFilterCriteria` DTO.
- Update `TaskController.taskDashboard(...)` to accept optional `status`, `fromDate`, and `toDate` request parameters.
- Keep `TaskRepository.findByUsername(String username)` unchanged to avoid persistence coupling and because Jira explicitly states repository query method can be in-memory over user task list.

## Security Considerations

- Never accept username as a request parameter for filtering.
- Always derive username from Spring Security `Authentication`.
- Normalize username in service before repository lookup.
- Filter only after retrieving the authenticated user's tasks.
- Keep CSRF protection enabled for POST routes via existing Spring Security defaults.

## Test Strategy

- Add service tests for:
  - no filters returns all user tasks;
  - status `OPEN` returns only open tasks;
  - status `COMPLETED` returns only completed tasks;
  - date range filters are inclusive;
  - combined status and date filtering;
  - username normalization is preserved.
- Add controller tests for:
  - optional filter params delegated to service;
  - model contains tasks, username, taskForm, selected status, fromDate, and toDate;
  - reset route/link points to `/tasks` without query parameters.

## Open Questions

- Whether invalid `fromDate > toDate` should show a validation message or return an empty list is not specified by Jira. Recommended: validate in service/controller and show a user-friendly error while preserving filter inputs.
