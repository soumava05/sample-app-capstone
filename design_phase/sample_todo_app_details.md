# sample_todo_app Details

## Repository

- Repository URL: https://github.com/soumava05/sample-app-capstone
- Repository name analyzed via available project context: `sample_todo_app` / GitHub repository `sample-app-capstone`
- Primary language: Java
- Default branch: `main`

## Application Type

Server-rendered Java web application for personal todo/task management. The application provides user registration, form-login authentication, user-scoped task listing, task creation with date planning, and completion tracking.

## Functional Requirements Observed

- Users can register with username, full name, password, and confirm password.
- Users can log in through Spring Security form login.
- Users can view only their own tasks.
- Users can create tasks with title, optional description, task date, and planned finish date.
- Users can mark tasks as completed.
- Root path redirects to `/tasks`.

## Non-Functional Requirements Observed

- Passwords are hashed using BCrypt.
- User input is validated through Jakarta Bean Validation DTO annotations and service-level business checks.
- Persistence is local JSON file-based storage under `storage/` with one user file and per-user task files.
- Code follows layered architecture and constructor injection.
- Build and test use Maven and TestNG/Mockito.

## Technology Stack

| Area | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring MVC |
| UI | Thymeleaf server-rendered HTML, static CSS |
| Security | Spring Security, BCrypt |
| Validation | Jakarta Bean Validation |
| Persistence | Local filesystem JSON files via Jackson |
| Serialization | Jackson with JavaTimeModule |
| Build | Maven |
| Testing | TestNG, Mockito |

## Architecture Patterns

- Layered monolith / modular monolith.
- MVC pattern for web request handling and Thymeleaf rendering.
- Service Layer Pattern for business workflows.
- Repository Pattern for persistence abstraction.
- DTO Pattern for form binding and validation boundaries.
- Configuration Pattern for Spring Security, Jackson, and storage settings.
- Interface-driven design across services and repositories.

## Main Modules and Responsibilities

| Module | Responsibility |
|---|---|
| `web` | MVC controllers for authentication and task workflows. |
| `dto` | Form DTOs and validation constraints. |
| `service` / `service.impl` | Business rules, username normalization, password hashing orchestration, task lifecycle operations. |
| `repository` / `repository.impl` | Persistence abstractions and file-backed implementations. |
| `domain` | Core domain models: `User`, `TodoTask`, `TaskStatus`. |
| `config` | Spring Boot configuration for storage properties, Jackson, and Spring Security. |
| `templates` | Thymeleaf pages for login, registration, and task dashboard. |
| `static/css` | Application styling. |

## Domain Model

### User

- `username`: normalized lower-case unique identifier.
- `fullName`: display name.
- `passwordHash`: BCrypt encoded password.
- `createdAt`: registration timestamp.

### TodoTask

- `id`: UUID string.
- `username`: normalized owner username.
- `title`: required task title.
- `description`: optional description normalized to empty string when absent.
- `taskDate`: planned task date.
- `plannedFinishDate`: required planned finish date.
- `status`: `OPEN` or `COMPLETED`.
- `createdAt`: creation timestamp.

## Business Rules

- Usernames are normalized by trimming and lowercasing.
- Registration fails when password and confirm password differ.
- Registration fails when username already exists ignoring case.
- Passwords are stored only as BCrypt hashes.
- Task planned finish date cannot be before task date.
- Task creation sets status to `OPEN` and assigns a UUID.
- Marking complete only searches within the authenticated user's task file.
- Task lists are sorted by task date, then creation timestamp.

## Validation Rules

### RegistrationForm

- `username`: required; regex `^[a-zA-Z0-9._-]{4,32}$`.
- `fullName`: required; 2 to 80 characters.
- `password`: required; 8 to 128 characters.
- `confirmPassword`: required.

### TaskForm

- `title`: required; 3 to 120 characters.
- `description`: optional; max 500 characters.
- `taskDate`: required ISO date.
- `plannedFinishDate`: required ISO date.

## Persistence Design

- `storage/users.json`: all users.
- `storage/tasks/<username>.json`: one task list per normalized username.
- `app.storage.root-path` configures the root storage path and defaults to `storage`.
- Jackson is configured to serialize Java time types as ISO strings.

## Security Design

- Public routes: `/login`, `/register`, `/css/**`.
- All other routes require authentication.
- Form login uses `/login` and redirects authenticated users to `/tasks`.
- Logout redirects to `/login?logout`.
- Remember-me is enabled with default Spring Security configuration.
- All authenticated users receive role `USER`.

## Observability and Operations

- No explicit custom logging, metrics, tracing, alerting, health endpoints, containerization, or deployment pipeline were observed in the analyzed files.
- Production hardening should add structured logging, actuator endpoints, metrics, secure secret/config management, backup/restore procedures for file storage, and deployment automation.

## Key Risks and Constraints

- File-based persistence is suitable for local/single-instance deployment but limits horizontal scalability.
- Repository imports reference `FileStorageManager`; the initial tree did not list `src/main/java/com/capstone/todo/storage/FileStorageManager.java`, so repository consistency should be verified before release.
- No database migrations or transaction manager exist because persistence is file-backed.
- Remember-me defaults should be reviewed for production key management.
- README references `http://localhost:8080`, while `application.yml` configures port `8090`; documentation/config alignment should be verified.

## Recommended Target Architecture Evolution

1. Preserve layered monolith for current scope.
2. Add operational hardening: Spring Boot Actuator, structured logs, central metrics, and health checks.
3. Add secure configuration for remember-me keys and production profile settings.
4. If multi-instance scalability is required, replace file repositories with database-backed implementations behind existing repository interfaces.
5. Add backup and recovery strategy for file storage while file persistence remains in use.
