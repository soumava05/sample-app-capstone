# sample_todo_app Analysis Details

## Jira Requirement

- **Ticket:** EPMCDMETST-55956
- **Summary:** To-Do Dashboard: add status filter and search to quickly find tasks
- **Requirement Type:** User Story
- **Functional Scope:** Add status filtering and free-text search to the authenticated Tasks dashboard.

## Functional Requirements

1. `GET /tasks` shall accept optional query parameters:
   - `status`: `ALL`, `OPEN`, or `COMPLETED`; default is `ALL`.
   - `search`: free-text keyword; optional.
2. Filtering and search shall apply only to tasks owned by the authenticated user.
3. Search shall match case-insensitive substrings in task title and description.
4. Status filtering and search shall be combinable in a single request.
5. UI shall preserve selected status and search term after reload.
6. Existing task creation and Mark Completed flows shall continue to work.

## Non-Functional Requirements

- Preserve current Spring MVC + Thymeleaf server-rendered architecture.
- Preserve strict user data isolation.
- Avoid new database dependencies; continue using file-system persistence.
- Keep implementation maintainable using Controller-Service-Repository layering.
- Keep behavior deterministic and covered by unit/controller tests.

## Codebase Findings

### App Type

Server-rendered Java web application for personal task management with registration, login, user-specific task visibility, file-backed persistence, and task completion workflow.

### Technology Stack

- Java 21
- Spring Boot 3.3.2
- Spring MVC
- Spring Security with form login and BCrypt password hashing
- Thymeleaf templates
- Jackson with Java Time module for JSON persistence
- Maven
- TestNG and Mockito
- Local file-system JSON storage

### Architecture Patterns

- Layered MVC architecture
- Controller pattern for HTTP request handling
- Service Layer pattern for business workflows
- Repository pattern for persistence abstraction
- DTO pattern for form binding and validation
- Configuration pattern for Spring/Security/storage wiring

### Current Task Flow

- `TaskController.taskDashboard(Authentication, Model)` renders `/tasks` with all user tasks.
- `TaskService.getUserTasks(username)` normalizes username and delegates to repository.
- `FileTaskRepository.findByUsername(username)` reads `storage/tasks/{username}.json` and returns tasks sorted by task date and creation time.
- `TaskController.createTask(...)` creates tasks and redirects to `/tasks`.
- `TaskController.markTaskCompleted(...)` marks a user-owned task as completed and redirects to `/tasks`.

### Recommended Design Summary

Add filtering/search at the service layer while keeping repository API unchanged initially. The repository already returns only tasks for the normalized username. The service should filter this user-owned collection in memory using a small request model or method parameters. The controller should bind query parameters, normalize default values, preserve them in the model, and pass them to the service. Thymeleaf should add a GET filter form above the task list and retain selected values.

## Proposed Component Changes

| Component | Change |
|---|---|
| `TaskController` | Add `@RequestParam(defaultValue = "ALL") String status` and optional `search` to `GET /tasks`; populate `selectedStatus` and `searchTerm`. |
| `TaskService` | Add `getUserTasks(String username, String status, String search)` or equivalent typed method. |
| `DefaultTaskService` | Normalize username, parse status, trim search, filter user-owned tasks by status and case-insensitive title/description. |
| `TaskRepository` | No required change for current file-backed scale; continue returning user-owned sorted tasks. |
| `tasks.html` | Add status dropdown and search input GET form; preserve current values across refresh. |
| `styles.css` | Add responsive filter bar styling. |
| Tests | Update service and controller tests for default, status-only, search-only, combined, invalid/blank values, and preservation model attributes. |

## Design Decisions

- Perform filtering in `DefaultTaskService` to keep business rules outside MVC and preserve repository abstraction.
- Keep `ALL` as a UI/query value only; do not add it to `TaskStatus` enum because persisted domain statuses remain `OPEN` and `COMPLETED`.
- Treat blank search as no search.
- Match search against `title` and `description` with null-safe, case-insensitive substring matching.
- Preserve task sort order from repository after filtering.
- Redirect existing POST actions to `/tasks`; optional future enhancement can preserve filter query parameters after mutations if desired, but not required by acceptance criteria.

## Open Questions

- Should invalid `status` query values fall back to `ALL` or return a validation error? Recommended: fallback to `ALL` to keep dashboard resilient.
- Should search trim leading/trailing whitespace? Recommended: yes.
