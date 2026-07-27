# sample_todo_app Details - EPMCDMETST-55863

## Jira Requirement Summary

**Ticket:** EPMCDMETST-55863  
**Story:** Add task list filters (status + date range) to improve navigation on Todo Dashboard.

Users need server-side filtering on the existing `/tasks` page so they can narrow their private task list by task status and task date range. The feature must preserve authenticated-user task isolation and show a validation error when `toDate` is before `fromDate`.

## Functional Requirements

| ID | Requirement |
|---|---|
| FR-1 | Show a filter bar above the task list on `/tasks`. |
| FR-2 | Filter tasks by `TaskStatus` values `OPEN` and `COMPLETED`. |
| FR-3 | Filter tasks by inclusive `taskDate` range using optional from/to dates. |
| FR-4 | Combine status and date-range filters when multiple values are provided. |
| FR-5 | Show all user tasks when no filter is supplied. |
| FR-6 | Reject invalid ranges where `toDate` is before `fromDate` and render a validation error on `tasks.html`. |
| FR-7 | Always scope filtered results to the authenticated username. |
| FR-8 | Clear filters by navigating to `/tasks` without query parameters. |

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | Maintain Spring Security authenticated access and user-scoped file reads. |
| Maintainability | Preserve controller-service-repository layering and interface-driven design. |
| Scalability | Filtering may be in-memory for current file-per-user persistence; design should allow repository-level optimization later. |
| Observability | Validation failures should be visible in UI; application exceptions should remain diagnosable via Spring logs. |
| Compatibility | Existing tasks stored in `storage/tasks/<username>.json` must remain readable without migration. |
| Testability | Add controller, service, and repository tests covering status, range, combined filters, default behavior, invalid range, and ownership isolation. |

## Codebase Analysis Findings

### App Type

Server-rendered Java web application for personal task management.

### Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring MVC + Thymeleaf |
| Security | Spring Security form login, BCrypt password encoder, session authentication |
| Validation | Jakarta Bean Validation |
| Persistence | Local JSON files through Jackson and file-storage abstraction |
| Build | Maven |
| Tests | TestNG + Mockito |

### Current Architecture Pattern

The application is a layered modular monolith:

- `web`: MVC controllers (`AuthController`, `TaskController`)
- `service`: business interfaces (`TaskService`, `UserService`)
- `service.impl`: business implementation (`DefaultTaskService`, `DefaultUserService`)
- `repository`: persistence interfaces (`TaskRepository`, `UserRepository`)
- `repository.impl`: file-backed repositories (`FileTaskRepository`, `FileUserRepository`)
- `domain`: `TodoTask`, `User`, `TaskStatus`
- `dto`: form DTOs (`RegistrationForm`, `TaskForm`)
- `config`: Spring Security, Jackson, and storage properties

### Existing Task Flow

1. Authenticated user opens `/tasks`.
2. `TaskController` calls `TaskService.getUserTasks(username)`.
3. `DefaultTaskService` normalizes username and delegates to `TaskRepository.findByUsername`.
4. `FileTaskRepository` reads `storage/tasks/<username>.json`, sorts tasks by `taskDate` and `createdAt`, and returns them.
5. `tasks.html` renders task cards and allows marking open tasks completed.

## Proposed Design Summary

Introduce a dedicated filter DTO and query path while preserving default behavior:

- Add `TaskFilterForm` or equivalent DTO with optional `TaskStatus status`, `LocalDate fromDate`, and `LocalDate toDate`.
- Extend `TaskService` with `getUserTasks(String username, TaskFilterForm filter)`.
- Extend `TaskRepository` with a filtered query method or keep filtering in service while repository continues user-scoped reads. Preferred: repository method `findByUsername(username, filter)` for persistence abstraction.
- Add validation that `toDate` must not be before `fromDate`; return `tasks` view with error rather than redirecting.
- Keep `/tasks` as a GET endpoint using query parameters for filters.
- Add filter bar in `tasks.html`, retaining selected values after apply and using a Clear link to `/tasks`.

## Architecture Decisions

| Decision | Rationale |
|---|---|
| Keep server-side filtering | Aligns with Thymeleaf MVC and avoids client-only duplication of security logic. |
| Keep file-based persistence | Explicit project constraint; no database dependency requested. |
| Use GET query parameters | Bookmarkable, shareable dashboard state and simple Clear behavior. |
| Validate date range before repository read | Prevents unnecessary I/O and centralizes business validation in service/controller boundary. |
| Preserve user scoping in all repository calls | Satisfies AC6 and existing security/data-isolation rules. |

## Alternatives Considered

| Alternative | Pros | Cons | Recommendation |
|---|---|---|---|
| Service-level in-memory filtering after `findByUsername` | Minimal repository changes; easy tests | Repository abstraction cannot optimize filtering later | Acceptable for small scope, but less extensible |
| Repository-level filter method | Encapsulates query semantics near persistence; easier future DB migration | More interface/test changes | Preferred |
| Client-side filtering in browser | Responsive after page load | Exposes all tasks to client, duplicates logic, no server-side validation | Not recommended |
| Database migration | Better indexing and large-scale querying | Out of scope and violates current project guidance | Not recommended |

## Implementation Impact

| Area | Expected Change |
|---|---|
| `TaskController` | Accept filter query parameters on `GET /tasks`, validate and bind selected filter values. |
| `TaskService` | Add filtered retrieval method and date-range guard. |
| `TaskRepository` / `FileTaskRepository` | Add filtered lookup or repository-side predicate after user file read. |
| `tasks.html` | Add filter bar with status dropdown, from/to date inputs, Apply and Clear actions. |
| CSS | Add responsive filter-bar styling. |
| Tests | Extend controller/service/repository tests for acceptance criteria. |

## Edge Cases

- No filters: return all authenticated user's tasks sorted as today.
- Only status: filter by status only.
- Only from date: include tasks on/after from date.
- Only to date: include tasks on/before to date.
- Both dates: inclusive range.
- Combined: all predicates must match.
- Invalid range: render validation error and preserve entered filter values.
- Unknown status query parameter: Spring binding should reject or controller should handle gracefully with validation feedback.
- Empty user task file or missing task file: return empty list.
- Cross-user task access: impossible because username remains derived from `Authentication`, not request parameters.

## Acceptance Criteria Traceability

| AC | Design Coverage |
|---|---|
| AC1 | Status dropdown mapped to `TaskStatus`; service/repository predicate filters OPEN/COMPLETED. |
| AC2 | `fromDate` and `toDate` query fields filter `TodoTask.taskDate` inclusively. |
| AC3 | Predicate composition applies status and date filters together. |
| AC4 | Null/empty filter values call default all-task path. |
| AC5 | Date-range validator rejects `toDate < fromDate` and returns `tasks.html` with error. |
| AC6 | Username always comes from `Authentication.getName()` and is normalized in service. |
