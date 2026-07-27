# sample_todo_app Solution Diagrams

## Design Scope

This document captures the design phase architecture for the Java Spring Boot Todo application. Diagrams are authored in Mermaid as the canonical representation.

## High Level Design

### System Context and Architecture

```mermaid
flowchart LR
  User(["Authenticated user"])
  Browser["Web browser"]

  subgraph TodoSystem["Todo App - Spring Boot Monolith"]
    MVC["Spring MVC controllers"]
    Security["Spring Security"]
    Services["Service layer"]
    Repositories["Repository interfaces"]
    FileRepos["File repository implementations"]
    Views["Thymeleaf templates"]
  end

  Storage[("Local JSON file storage")]

  User --> Browser
  Browser -->|"HTTP form requests"| Security
  Security --> MVC
  MVC --> Services
  Services --> Repositories
  Repositories --> FileRepos
  FileRepos -->|"Jackson read and write"| Storage
  MVC --> Views
  Views -->|"HTML response"| Browser
```

### High Level Responsibilities

| Layer | Responsibilities | Main Classes or Assets |
|---|---|---|
| Client | Uses server-rendered pages and submits forms. | Browser |
| Security | Authenticates users, hashes passwords, protects routes. | `SecurityConfig`, Spring Security, BCrypt |
| Web | Handles MVC routing, model population, redirects, validation errors. | `AuthController`, `TaskController` |
| DTO | Defines input validation boundaries. | `RegistrationForm`, `TaskForm` |
| Service | Enforces business rules and task/user workflows. | `DefaultUserService`, `DefaultTaskService` |
| Repository | Abstracts persistence. | `UserRepository`, `TaskRepository` |
| Persistence | Stores JSON files under configured root path. | `FileUserRepository`, `FileTaskRepository`, `FileStorageManager` |
| UI | Renders login, registration, and task dashboard. | Thymeleaf templates and CSS |

### Security, Observability, Scalability, and Maintainability

- Security: Spring Security session/form login, BCrypt password hashing, authenticated access to `/tasks`, user-scoped repository lookup.
- Observability: current design has no explicit observability; target production posture should add Spring Boot Actuator, structured logs, metrics, dashboards, and alerting.
- Scalability: current file storage is best for single-node deployment. Repository interfaces allow future migration to database-backed repositories.
- Maintainability: layered architecture, constructor injection, DTO validation, service-level business rules, and interface-driven repository/service boundaries.

## Low Level Design

### Detailed Component Design

```mermaid
classDiagram
  direction LR

  class AuthController {
    <<controller>>
    +loginPage() String
    +registerPage(model) String
    +register(form, bindingResult) String
  }

  class TaskController {
    <<controller>>
    +rootRedirect() String
    +taskDashboard(authentication, model) String
    +createTask(authentication, form, bindingResult, model) String
    +markTaskCompleted(authentication, taskId) String
  }

  class UserService {
    <<interface>>
    +register(registrationForm) User
    +findByUsername(username) Optional~User~
  }

  class TaskService {
    <<interface>>
    +createTask(username, taskForm) TodoTask
    +getUserTasks(username) List~TodoTask~
    +markCompleted(username, taskId) void
  }

  class DefaultUserService {
    <<service>>
    -UserRepository userRepository
    -PasswordEncoder passwordEncoder
  }

  class DefaultTaskService {
    <<service>>
    -TaskRepository taskRepository
  }

  class UserRepository {
    <<interface>>
    +findByUsername(username) Optional~User~
    +save(user) User
    +findAll() List~User~
  }

  class TaskRepository {
    <<interface>>
    +findByUsername(username) List~TodoTask~
    +save(task) TodoTask
    +findById(username, taskId) Optional~TodoTask~
    +update(task) void
  }

  class FileUserRepository {
    <<repository>>
    -FileStorageManager fileStorageManager
    -Path usersFilePath
  }

  class FileTaskRepository {
    <<repository>>
    -FileStorageManager fileStorageManager
    -Path storageRootPath
  }

  class FileStorageManager {
    <<storage>>
    +readList(path, type) List
    +writeList(path, values) void
  }

  class User {
    +String username
    +String fullName
    +String passwordHash
    +LocalDateTime createdAt
  }

  class TodoTask {
    +String id
    +String username
    +String title
    +String description
    +LocalDate taskDate
    +LocalDate plannedFinishDate
    +TaskStatus status
    +LocalDateTime createdAt
  }

  class TaskStatus {
    <<enumeration>>
    OPEN
    COMPLETED
  }

  AuthController --> UserService
  TaskController --> TaskService
  DefaultUserService ..|> UserService
  DefaultTaskService ..|> TaskService
  DefaultUserService --> UserRepository
  DefaultTaskService --> TaskRepository
  FileUserRepository ..|> UserRepository
  FileTaskRepository ..|> TaskRepository
  FileUserRepository --> FileStorageManager
  FileTaskRepository --> FileStorageManager
  UserRepository --> User
  TaskRepository --> TodoTask
  TodoTask --> TaskStatus
```

### APIs and Routes

| Route | Method | Auth | Controller | Request Model | Response |
|---|---:|---|---|---|---|
| `/` | GET | Required | `TaskController` | None | Redirect to `/tasks` |
| `/login` | GET | Public | `AuthController` | None | `login.html` |
| `/register` | GET | Public | `AuthController` | None | `register.html` |
| `/register` | POST | Public | `AuthController` | `RegistrationForm` | Redirect to login or register view with errors |
| `/tasks` | GET | Required | `TaskController` | Auth principal | `tasks.html` |
| `/tasks` | POST | Required | `TaskController` | `TaskForm` | Redirect to `/tasks` or task view with errors |
| `/tasks/{taskId}/complete` | POST | Required | `TaskController` | Path variable | Redirect to `/tasks` |

### Data Schema

```mermaid
erDiagram
  USER {
    string username PK
    string fullName
    string passwordHash
    datetime createdAt
  }

  TODO_TASK {
    string id PK
    string username FK
    string title
    string description
    date taskDate
    date plannedFinishDate
    string status
    datetime createdAt
  }

  USER ||--o{ TODO_TASK : owns
```

### Sequence Diagram

```mermaid
sequenceDiagram
  actor User as User
  participant Browser as Browser
  participant Security as Spring Security
  participant AuthController as AuthController
  participant TaskController as TaskController
  participant UserService as UserService
  participant TaskService as TaskService
  participant UserRepo as FileUserRepository
  participant TaskRepo as FileTaskRepository
  participant Storage as JSON Storage

  User->>Browser: Open registration page
  Browser->>AuthController: GET /register
  AuthController-->>Browser: Render register.html
  User->>Browser: Submit registration form
  Browser->>AuthController: POST /register
  AuthController->>UserService: register(form)
  UserService->>UserRepo: findByUsername(username)
  UserRepo->>Storage: Read users.json
  Storage-->>UserRepo: Existing users
  alt Username available and passwords match
    UserService->>UserService: Hash password with BCrypt
    UserService->>UserRepo: save(user)
    UserRepo->>Storage: Write users.json
    AuthController-->>Browser: Redirect /login?registered
  else Validation or business error
    AuthController-->>Browser: Render register.html with errors
  end

  User->>Browser: Submit login form
  Browser->>Security: POST /login
  Security->>UserService: findByUsername(username)
  UserService->>UserRepo: findByUsername(username)
  UserRepo->>Storage: Read users.json
  Storage-->>UserRepo: User record
  Security-->>Browser: Redirect /tasks

  User->>Browser: Create task
  Browser->>TaskController: POST /tasks
  TaskController->>TaskService: createTask(username, taskForm)
  TaskService->>TaskService: Validate dates and normalize username
  TaskService->>TaskRepo: save(task)
  TaskRepo->>Storage: Read and write tasks username json
  TaskController-->>Browser: Redirect /tasks

  User->>Browser: Mark task complete
  Browser->>TaskController: POST /tasks/{taskId}/complete
  TaskController->>TaskService: markCompleted(username, taskId)
  TaskService->>TaskRepo: findById(username, taskId)
  TaskRepo->>Storage: Read tasks username json
  Storage-->>TaskRepo: User tasks
  TaskService->>TaskRepo: update(task status completed)
  TaskRepo->>Storage: Write tasks username json
  TaskController-->>Browser: Redirect /tasks
```

### Error Handling and Edge Cases

| Scenario | Handling |
|---|---|
| Registration DTO validation fails | Controller returns `register` view with field errors. |
| Password confirmation mismatch | `DefaultUserService` throws `IllegalArgumentException`; controller rejects registration. |
| Username already taken | `DefaultUserService` throws `IllegalArgumentException`; controller rejects registration. |
| Invalid login credentials | Spring Security redirects with login error. |
| Task DTO validation fails | Controller returns `tasks` view with existing task list and field errors. |
| Planned finish date before task date | `DefaultTaskService` throws `IllegalArgumentException`; controller rejects task creation. |
| Task ID not found for authenticated user | Service throws `IllegalArgumentException`. Current controller does not explicitly catch this path for completion. |
| Empty storage file or missing file | Repository delegates to storage manager; expected behavior is empty list when no records exist. |
| Cross-user task access attempt | Task lookup is scoped by authenticated username and per-user JSON file path. |

### Configuration

| Property | Default | Purpose |
|---|---|---|
| `server.port` | `8090` | Local HTTP port. |
| `spring.application.name` | `todo-app` | Spring application name. |
| `spring.thymeleaf.cache` | `false` | Template cache disabled for local development. |
| `app.storage.root-path` | `storage` | Root folder for JSON persistence. |

### Rollout and Operations

1. Build with `mvn clean package`.
2. Run tests with `mvn test`.
3. Deploy as a single Spring Boot application instance while file persistence remains in use.
4. Ensure persistent storage volume is mounted to `app.storage.root-path`.
5. Backup `users.json` and `tasks/*.json` before upgrades.
6. Add readiness and liveness checks before production deployment.
7. For horizontal scaling, introduce database-backed repository implementations and migrate data from JSON files.

## Component Diagram

```mermaid
flowchart TB
  subgraph Client["Client Tier"]
    Browser["Browser"]
  end

  subgraph App["Spring Boot Application"]
    SecurityFilter["Security Filter Chain"]
    AuthController["AuthController"]
    TaskController["TaskController"]
    RegistrationForm["RegistrationForm"]
    TaskForm["TaskForm"]
    UserService["DefaultUserService"]
    TaskService["DefaultTaskService"]
    UserRepository["FileUserRepository"]
    TaskRepository["FileTaskRepository"]
    StorageManager["FileStorageManager"]
    Thymeleaf["Thymeleaf Views"]
    Jackson["Jackson ObjectMapper"]
  end

  subgraph FileSystem["Local File System"]
    UsersFile[("storage/users.json")]
    TasksFiles[("storage/tasks/username.json")]
  end

  Browser --> SecurityFilter
  SecurityFilter --> AuthController
  SecurityFilter --> TaskController
  AuthController --> RegistrationForm
  TaskController --> TaskForm
  AuthController --> UserService
  TaskController --> TaskService
  UserService --> UserRepository
  TaskService --> TaskRepository
  UserRepository --> StorageManager
  TaskRepository --> StorageManager
  StorageManager --> Jackson
  StorageManager --> UsersFile
  StorageManager --> TasksFiles
  AuthController --> Thymeleaf
  TaskController --> Thymeleaf
  Thymeleaf --> Browser
```

## Deployment Diagram

```mermaid
flowchart LR
  User(["End user"])

  subgraph RuntimeHost["Single runtime host"]
    Browser["Browser"]
    JVM["Java 21 JVM"]
    App["todo-app Spring Boot jar"]
    StorageVolume[("Persistent storage volume")]
  end

  subgraph AppProcess["Application process"]
    Spring["Spring Boot 3.3.2"]
    MVC["Spring MVC and Thymeleaf"]
    Sec["Spring Security"]
    Json["Jackson JSON persistence"]
  end

  User --> Browser
  Browser -->|"HTTP port 8090"| App
  App --> JVM
  App --> Spring
  Spring --> MVC
  Spring --> Sec
  Spring --> Json
  Json --> StorageVolume
```

## Data Flow Diagram

```mermaid
flowchart TB
  User(["User"])
  RegisterForm["Registration form"]
  LoginForm["Login form"]
  TaskFormNode["Task form"]
  AuthController["AuthController"]
  TaskController["TaskController"]
  UserService["UserService"]
  TaskService["TaskService"]
  PasswordEncoder["BCrypt PasswordEncoder"]
  UserRepo["UserRepository"]
  TaskRepo["TaskRepository"]
  UsersJson[("users.json")]
  TasksJson[("tasks username json")]
  TaskList["Rendered task dashboard"]

  User --> RegisterForm
  RegisterForm --> AuthController
  AuthController --> UserService
  UserService --> PasswordEncoder
  UserService --> UserRepo
  UserRepo --> UsersJson

  User --> LoginForm
  LoginForm --> AuthController
  AuthController --> UserService
  UserService --> UserRepo
  UserRepo --> UsersJson

  User --> TaskFormNode
  TaskFormNode --> TaskController
  TaskController --> TaskService
  TaskService --> TaskRepo
  TaskRepo --> TasksJson
  TasksJson --> TaskRepo
  TaskRepo --> TaskService
  TaskService --> TaskController
  TaskController --> TaskList
  TaskList --> User
```

## Architecture Diagram

```mermaid
architecture-beta
  group client(internet)[Client]
  service browser(internet)[Browser] in client

  group app(server)[Spring Boot Monolith]
  service security(server)[Spring Security] in app
  service mvc(server)[MVC Controllers] in app
  service services(server)[Services] in app
  service repositories(server)[Repositories] in app
  service thymeleaf(server)[Thymeleaf Views] in app

  group storage(disk)[File Persistence]
  service users(database)[users json] in storage
  service tasks(database)[tasks json] in storage

  browser:R --> L:security
  security:R --> L:mvc
  mvc:R --> L:services
  services:R --> L:repositories
  repositories:R --> L:users
  repositories:B --> T:tasks
  mvc:B --> T:thymeleaf
  thymeleaf:L --> R:browser
```

## Wireframes and User Flows

### User Journey

```mermaid
journey
  title Todo App User Journey
  section Registration
    Open registration page: 5: User
    Enter account details: 4: User
    Submit registration: 4: User, System
    See registration errors if invalid: 3: User, System
  section Login
    Open login page: 5: User
    Submit credentials: 4: User
    Redirect to dashboard: 5: User, System
  section Task Management
    View personal task list: 5: User
    Create task with dates: 4: User
    See validation errors if invalid: 3: User, System
    Mark task completed: 5: User, System
    Logout: 5: User
```

### Screen Flow

```mermaid
flowchart LR
  Login["Login screen"]
  Register["Register screen"]
  Tasks["Todo dashboard"]
  ErrorLogin["Login error message"]
  ErrorRegister["Registration validation errors"]
  ErrorTask["Task validation errors"]

  Login -->|"New user link"| Register
  Register -->|"Successful registration"| Login
  Register -->|"Invalid form or duplicate username"| ErrorRegister
  ErrorRegister --> Register
  Login -->|"Valid credentials"| Tasks
  Login -->|"Invalid credentials"| ErrorLogin
  ErrorLogin --> Login
  Tasks -->|"Add valid task"| Tasks
  Tasks -->|"Invalid task dates or fields"| ErrorTask
  ErrorTask --> Tasks
  Tasks -->|"Mark completed"| Tasks
  Tasks -->|"Logout"| Login
```

## Alternatives Considered

| Alternative | Pros | Cons | Recommendation |
|---|---|---|---|
| Continue file-based persistence | Simple, no database dependency, aligned with current code. | Limited concurrency and horizontal scaling, backup complexity. | Keep for sample/local single-instance scope. |
| Add relational database repository implementation | Scalable, transactional, easier reporting and backup. | Adds infrastructure and migration complexity. | Use when production multi-user or multi-instance requirements emerge. |
| Convert UI to SPA plus REST API | Rich client interactivity and API reuse. | Larger scope, client build pipeline, duplicated validation risk. | Not recommended unless explicitly required. |
| Containerize and deploy behind reverse proxy | Repeatable deployment, portable runtime. | Requires Dockerfile, volume strategy, image scanning. | Recommended as next operational hardening step. |
