# sample_todo_app Details

## Jira Context

- **Ticket:** EPMCDMETST-56035
- **Type:** Story
- **Summary:** Tasks dashboard: add status filter (Open/Completed/All) to reduce clutter and improve planning
- **Primary requirement:** Add a backend-supported and UI-visible status filter on `/tasks` using `status=OPEN|COMPLETED|ALL`, defaulting unsupported or missing values to `ALL`, and preserving the selected filter after create/complete redirects.

## Codebase Analysis Findings

### Application Type

`sample_todo_app` / `sample-app-capstone` is a server-rendered Java Todo web application. It combines Spring MVC controllers, Spring Security authentication, Thymeleaf templates, and local JSON file persistence.

### Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring MVC |
| UI | Thymeleaf, static CSS |
| Security | Spring Security, BCrypt password hashing |
| Validation | Jakarta Bean Validation via Spring Boot starter validation |
| Persistence | Jackson JSON files under `storage/` |
| Build | Maven |
| Testing | TestNG, Mockito |

### Current Architecture Patterns

- **Layered MVC:** `web` controllers delegate to `service` interfaces, which delegate to `repository` abstractions.
- **Repository Pattern:** `TaskRepository` and `UserRepository` hide file persistence details.
- **Service Layer Pattern:** `DefaultTaskService` centralizes task creation, completion, username normalization, and business validations.
- **DTO Pattern:** `TaskForm` and `RegistrationForm` isolate form binding and validation concerns.
- **Configuration Pattern:** Security, storage, and Jackson are configured through dedicated Spring configuration classes.

### Relevant Existing Components

| Component | Responsibility | Current Behavior |
|---|---|---|
| `TaskController` | `/`, `/tasks`, `/tasks/{taskId}/complete` MVC routes | Lists all tasks for authenticated user, creates tasks, completes tasks, redirects to `/tasks` |
| `TaskService` | Task business API | Creates OPEN tasks, lists all user tasks, marks task COMPLETED |
| `DefaultTaskService` | Task business implementation | Normalizes usernames, validates planned finish date, delegates persistence |
| `TaskRepository` | Task persistence abstraction | Finds all tasks by username, saves, finds by ID, updates |
| `FileTaskRepository` | File-backed persistence | Stores tasks per normalized username and sorts by task date then creation time |
| `TaskStatus` | Domain enum | `OPEN`, `COMPLETED` |
| `tasks.html` | Task dashboard | Displays create form and all tasks; no status filter yet |

### Requirement Fit

The requested feature can be implemented as a targeted extension of the current MVC/service/repository layering. No database or SPA migration is required. Recommended design is to introduce a filter parsing boundary in the web/service layer, preserve `TaskStatus` as the persisted status enum, and treat `ALL` as a UI/API filter option rather than a persisted task status.

## Design Constraints and Decisions

1. **Do not add database dependencies.** Existing local JSON persistence remains unchanged.
2. **Keep filtering user-scoped.** Filtering must occur only after username normalization and repository lookup for the authenticated user.
3. **Represent `ALL` as a filter value, not a domain task status.** `TaskStatus` should remain `OPEN` and `COMPLETED` only.
4. **Default safely.** Missing, blank, or unsupported status values must resolve to `ALL`.
5. **Preserve progressive enhancement.** UI filter should work through query parameters and server rendering without client-side state.

## Recommended Implementation Touchpoints

- Add a status filter type or parser, for example `TaskStatusFilter` with `ALL`, `OPEN`, `COMPLETED`.
- Extend `TaskService` with `getUserTasks(String username, TaskStatusFilter statusFilter)` or equivalent.
- Filter in `DefaultTaskService` after loading normalized user's tasks.
- Update `TaskController` to accept `@RequestParam(name = "status", required = false)` and populate `currentStatus`/filter options in the model.
- Include `status` hidden field in create/complete forms or redirect query construction.
- Update `tasks.html` to render filter tabs or dropdown and visually indicate selected value.
- Update controller and service tests for OPEN, COMPLETED, ALL/default fallback, and redirect preservation.
