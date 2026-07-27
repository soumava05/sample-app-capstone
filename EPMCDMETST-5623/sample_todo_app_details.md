# sample_todo_app Details

## Jira Requirement

- Jira ticket: EPMCDMETST-5623
- Summary: DEV: Implement Todo Application in Spring Boot
- Business context: provide a simple and intuitive Todo application for users to manage personal tasks.
- Current implementation: server-rendered registration/login plus user-scoped task create, list, and complete.
- Jira target scope also references REST APIs, admin features, JWT, database storage, GDPR encryption, Kubernetes deployment, and OpenAPI. These are noted as gaps requiring explicit implementation approval.

## Codebase Findings

### App Type

Server-rendered Java Spring Boot Todo web application using Spring MVC, Thymeleaf, Spring Security form login, and local JSON file persistence.

### Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring MVC, Thymeleaf |
| Security | Spring Security, BCrypt, form login, remember-me |
| Validation | Jakarta Bean Validation |
| Persistence | Jackson JSON files under `app.storage.root-path` |
| Build | Maven |
| Testing | TestNG, Mockito |

### Architecture Patterns

- Layered architecture: controller, service, repository, storage.
- Repository Pattern for persistence abstraction.
- Service Layer Pattern for user and task workflows.
- DTO Pattern for form binding and validation boundaries.
- Configuration Pattern for framework wiring.

## Current Components

- `AuthController`: login/register pages and registration submission.
- `TaskController`: root redirect, dashboard, create task, complete task.
- `DefaultUserService`: username normalization, password confirmation, duplicate user check, BCrypt hashing.
- `DefaultTaskService`: username normalization, task date validation, task creation, task completion.
- `FileUserRepository`: `storage/users.json`, case-insensitive username lookup, sorted users.
- `FileTaskRepository`: `storage/tasks/{username}.json`, sorted tasks by task date and created timestamp.

## Domain Model

- `User`: username, fullName, passwordHash, createdAt.
- `TodoTask`: id, username, title, description, taskDate, plannedFinishDate, status, createdAt.
- `TaskStatus`: OPEN, COMPLETED.

## Business Rules and Validation

- Username: required, 4-32 characters, letters, numbers, dot, underscore, hyphen.
- Full name: required, 2-80 characters.
- Password: required, 8-128 characters, must match confirmation.
- Task title: required, 3-120 characters.
- Description: optional, maximum 500 characters.
- Task date and planned finish date are required.
- Planned finish date cannot be before task date.
- Usernames are trimmed and lowercased for persistence and lookup.
- Tasks are isolated by authenticated username.
- New tasks start with OPEN status; completion changes status to COMPLETED.

## Gaps vs Full Jira Scope

- REST API endpoints are not implemented; current app is MVC form-based.
- Admin role/features are not implemented.
- JWT authentication is not implemented.
- Database/GDPR encrypted storage is not implemented.
- Kubernetes/stateless scalability is constrained by local filesystem persistence.
- Profile management and todo update/delete operations are not implemented.
- OpenAPI/Swagger documentation is not present.
- Production observability is not configured.

## Recommended Target Direction

Keep the current MVC implementation for the sample app. For production, migrate persistence to PostgreSQL, add observability, configure HTTPS and secure cookies, and implement REST/JWT/admin features only when explicitly approved as in scope.
