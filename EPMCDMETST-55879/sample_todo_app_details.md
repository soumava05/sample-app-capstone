# EPMCDMETST-55879 - sample_todo_app Design Analysis

## Jira Requirement

**Ticket:** EPMCDMETST-55879  
**Type:** Story  
**Summary:** Todo Dashboard: add search + status filter for user tasks (server-side)

### Functional Requirements

- Add server-side search and filtering to `/tasks` dashboard.
- Search keyword must match task title or description case-insensitively.
- Status filter supports `ALL`, `OPEN`, and `COMPLETED`.
- Optional task date `from` and `to` filters apply inclusive range filtering using `taskDate`.
- Filters can be combined and all constraints must be applied together.
- Clear filters action returns the full authenticated user's task list.
- Invalid date ranges must show a user-visible validation message and must not crash.
- Filtering must never return another user's tasks.
- Existing create task and mark completed flows must remain unchanged.

### Non-Functional Requirements

- Preserve Spring Boot MVC and Thymeleaf server-rendered architecture.
- Preserve file-based JSON persistence; do not add a database.
- Keep layered boundaries between controller, service, repository, DTO, domain, and storage.
- Maintain secure session-based authentication via Spring Security.
- Maintain user data isolation as a core security constraint.
- Add/update controller and service tests for filtering behavior and user scoping.

## Codebase Analysis Findings

### Application Type

Server-rendered Java web application for personal task management.

### Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring MVC, Thymeleaf |
| Security | Spring Security form login, BCrypt password hashing |
| Validation | Jakarta Bean Validation |
| Persistence | Local JSON files using Jackson |
| Build | Maven |
| Tests | TestNG, Mockito |

### Current Architecture Pattern

The application follows a layered modular monolith pattern:

- `web`: MVC controllers for authentication and task dashboard.
- `dto`: form models and validation constraints.
- `service` / `service.impl`: business workflows and business-rule validation.
- `repository` / `repository.impl`: persistence abstraction and file-backed implementation.
- `domain`: core entities and enums.
- `config`: Spring Boot, Jackson, storage, and security configuration.

### Existing Domain Model

| Entity | Key Fields | Notes |
|---|---|---|
| `User` | username, fullName, passwordHash, createdAt | Username is normalized to lowercase in service logic. Password is stored as BCrypt hash. |
| `TodoTask` | id, username, title, description, taskDate, plannedFinishDate, status, createdAt | Task visibility is scoped by username. |
| `TaskStatus` | OPEN, COMPLETED | Used by mark-completed flow and proposed status filter. |

### Existing Main Flows

1. User registers through `/register`.
2. User logs in through `/login` using Spring Security form login.
3. Authenticated user opens `/tasks`.
4. Controller loads tasks via `TaskService.getUserTasks(username)`.
5. `DefaultTaskService` normalizes username and calls `TaskRepository.findByUsername`.
6. `FileTaskRepository` loads only `storage/tasks/{username}.json`, sorted by `taskDate` then `createdAt`.
7. User can create tasks via `POST /tasks`.
8. User can mark a task completed via `POST /tasks/{taskId}/complete`.

### Current Gaps for EPMCDMETST-55879

- `TaskController.taskDashboard` accepts no filter query parameters.
- `TaskService` exposes only `getUserTasks(username)` and has no filter API.
- No filter DTO exists for query parameters.
- `tasks.html` has no search/filter form and does not preserve filter values.
- Invalid date range handling is not present for dashboard filtering.
- Tests do not cover search/status/date filters.

## Recommended Design Summary

Add a small filter query DTO and service method while keeping repository file-based and user-scoped:

- Add `TaskFilter` DTO with keyword, status, dateFrom, and dateTo.
- Extend `TaskService` with `getUserTasks(String username, TaskFilter filter)`.
- Implement filtering in `DefaultTaskService` after retrieving user-scoped tasks from repository.
- Keep repository unchanged because persistence remains file-based and task volume is local-user scoped.
- Update `TaskController` to bind query parameters on `GET /tasks`.
- Update `tasks.html` to show filter form, preserve selected values, display validation errors, and provide clear filters link.
- Add tests for keyword, status, combined filters, inclusive date range, invalid date range, and cross-user isolation.

## Architectural Decision

**Decision:** Implement server-side filtering in service layer over user-scoped repository results.

**Rationale:**

- Preserves file-based persistence constraint.
- Keeps controllers thin and avoids persistence details in web layer.
- Reuses existing repository user-scoping as the first security boundary.
- Avoids premature repository API complexity while current storage is per-user JSON files.

## Alternatives Considered

| Alternative | Pros | Cons | Recommendation |
|---|---|---|---|
| Service-layer filtering over `findByUsername` | Minimal change, secure by default, simple tests, no persistence changes | Reads all tasks for one user before filtering | Recommended |
| Repository-level filter method | Can optimize filtering later | Adds persistence-specific query logic to file repository now | Defer until task volume requires it |
| Database-backed filtering | Efficient indexed queries | Violates explicit no-database constraint and broadens scope | Not recommended |
| Client-side filtering only | Fast UI implementation | Violates server-side requirement and exposes full list to client | Not recommended |

## Security Considerations

- Always derive username from `Authentication.getName()`; never from query parameters.
- Retrieve tasks using `findByUsername(normalizedUsername)` before applying filters.
- Keep `/tasks` authenticated through existing `SecurityConfig`.
- Preserve CSRF protection for POST create and complete flows.
- Validate and sanitize filter inputs; trim keyword and treat blank keyword as no filter.
- Avoid logging sensitive user data or passwords.

## Scalability and Performance

- Current per-user JSON files make service-layer filtering acceptable for sample/local deployments.
- For larger task volumes, migrate `TaskRepository` interface to expose query criteria and implement indexed database-backed retrieval.
- Keep filter code modular so future repository-level optimization does not affect controller/template contracts.

## Maintainability

- New filtering behavior should be isolated in a DTO and private predicate-style service methods.
- Preserve existing method `getUserTasks(username)` as a convenience overload delegating to the filtered method with an empty filter.
- Keep Thymeleaf form names aligned with query parameter names.
- Add focused unit tests for service logic and controller binding/model behavior.

## Observability

- No dedicated observability framework currently exists.
- Recommended minimal enhancement: log invalid date-range attempts at debug/info level without sensitive data.
- Future productionization: add Spring Boot Actuator and structured logs.

## Testing Scope

- Service tests:
  - Keyword filters title case-insensitively.
  - Keyword filters description case-insensitively.
  - Status filters `OPEN` and `COMPLETED`.
  - Combined keyword + status filters use AND semantics.
  - Date range is inclusive.
  - Invalid date range throws a clear `IllegalArgumentException` or returns handled validation outcome.
  - Repository is called only for authenticated/normalized username.
- Controller tests:
  - Dashboard populates tasks, `taskForm`, username, and filter model.
  - Invalid date range returns `tasks` view with visible error.
  - Existing create and complete redirects remain unchanged.
- Template tests/manual review:
  - Filter fields visible and preserve submitted values.
  - Clear filters link points to `/tasks`.

## Open Questions

- Whether invalid date range should return HTTP 200 with inline message or redirect with flash error. Recommended: HTTP 200 returning `tasks` view with inline message to preserve input.
- Whether status query parameter should use `ALL` string or blank value for all. Recommended: support both to improve UX resilience.
