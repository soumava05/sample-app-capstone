# Design Diagrams - EPMCDMETST-55858

## High Level Design

The feature is implemented inside the existing Spring Boot modular monolith. Browser requests remain server-rendered through Thymeleaf. Authenticated users access `/tasks` with optional query parameters for task status filtering and planned finish date sorting. The controller delegates to the service layer, which enforces user scoping, validates parameters, filters the user task list, sorts the visible tasks, and returns the result to the view.

```mermaid
C4Context
  title Todo App System Context - Filter and Sort Enhancement
  Person(user, "Authenticated User", "Registered user managing personal tasks")
  System(todo, "Todo Web Application", "Spring Boot MVC and Thymeleaf application for personal task management")
  SystemDb(files, "Local JSON Storage", "users.json and per-user task JSON files")
  Rel(user, todo, "Uses dashboard, filter, sort, create, complete", "HTTPS or local HTTP")
  Rel(todo, files, "Reads and writes user-scoped task data", "File I/O with Jackson JSON")
```

```mermaid
flowchart LR
  User(["Authenticated User"])
  Browser["Browser"]
  Controller["TaskController"]
  Service["TaskService"]
  Repository["TaskRepository"]
  Storage[("Per-user task JSON file")]
  View["Thymeleaf tasks.html"]

  User --> Browser
  Browser -->|"GET /tasks with status and sort"| Controller
  Controller -->|"Authenticated username plus query params"| Service
  Service -->|"findByUsername normalized username"| Repository
  Repository --> Storage
  Repository -->|"User task list"| Service
  Service -->|"Filter by TaskStatus and sort by plannedFinishDate"| Controller
  Controller -->|"Model: tasks, selectedStatus, selectedSort"| View
  View --> Browser
```

## Low Level Design

### Component Diagram

```mermaid
classDiagram
  direction LR

  class TaskController {
    <<controller>>
    +taskDashboard(authentication, status, sort, model) String
    +createTask(authentication, taskForm, bindingResult, model) String
    +markTaskCompleted(authentication, taskId) String
  }

  class TaskService {
    <<interface>>
    +createTask(username, taskForm) TodoTask
    +getUserTasks(username) List~TodoTask~
    +getUserTasks(username, status, sort) List~TodoTask~
    +markCompleted(username, taskId) void
  }

  class DefaultTaskService {
    <<service>>
    -TaskRepository taskRepository
    +getUserTasks(username, status, sort) List~TodoTask~
    -normalizeUsername(username) String
    -parseStatusFilter(status) TaskStatusFilter
    -parseSortOrder(sort) TaskSortOrder
  }

  class TaskRepository {
    <<interface>>
    +findByUsername(username) List~TodoTask~
    +save(task) TodoTask
    +findById(username, taskId) Optional~TodoTask~
    +update(task) void
  }

  class FileTaskRepository {
    <<repository>>
    -FileStorageManager fileStorageManager
    +findByUsername(username) List~TodoTask~
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

  class TaskStatusFilter {
    <<enumeration>>
    ALL
    OPEN
    COMPLETED
  }

  class TaskSortOrder {
    <<enumeration>>
    PLANNED_FINISH_ASC
    PLANNED_FINISH_DESC
  }

  TaskController --> TaskService : delegates
  DefaultTaskService ..|> TaskService
  DefaultTaskService --> TaskRepository : reads user-scoped tasks
  FileTaskRepository ..|> TaskRepository
  TaskRepository --> TodoTask : returns
  TodoTask --> TaskStatus : has
  DefaultTaskService --> TaskStatusFilter : parses
  DefaultTaskService --> TaskSortOrder : parses
```

### Sequence Diagram

```mermaid
sequenceDiagram
  actor User
  participant Browser
  participant Controller as TaskController
  participant Service as DefaultTaskService
  participant Repo as FileTaskRepository
  participant Storage as UserTaskJsonFile
  participant View as tasks.html

  User->>Browser: Select status filter and planned finish sort
  Browser->>Controller: GET /tasks?status=OPEN&sort=PLANNED_FINISH_ASC
  Controller->>Controller: Read authenticated username
  Controller->>Service: getUserTasks(username, status, sort)
  Service->>Service: Normalize username
  Service->>Service: Parse filter and sort with safe defaults
  Service->>Repo: findByUsername(normalizedUsername)
  Repo->>Storage: Read storage/tasks/username.json
  Storage-->>Repo: List of user-owned tasks
  Repo-->>Service: Tasks sorted by existing repository default
  Service->>Service: Apply status filter
  Service->>Service: Sort by plannedFinishDate
  Service-->>Controller: Visible task list
  Controller->>View: Populate tasks, selectedStatus, selectedSort
  View-->>Browser: Render dashboard
  Browser-->>User: Filtered and sorted task list
```

### Deployment Diagram

```mermaid
flowchart TB
  subgraph Client["Client Device"]
    Browser["Web Browser"]
  end

  subgraph Runtime["Application Host"]
    App["Spring Boot Todo Application"]
    Session["Spring Security Session"]
    JsonStorage[("Local File Storage")]
  end

  Users[("storage/users.json")]
  Tasks[("storage/tasks/username.json")]

  Browser -->|"HTTP GET and POST"| App
  App --> Session
  App -->|"Jackson JSON read/write"| JsonStorage
  JsonStorage --> Users
  JsonStorage --> Tasks
```

### Data Flow Diagram

```mermaid
flowchart TD
  A(["Start GET /tasks"])
  B["Extract authenticated username"]
  C["Read query params status and sort"]
  D{"Are params valid?"}
  E["Use safe defaults"]
  F["Use provided params"]
  G["Load tasks by username"]
  H["Filter status: ALL, OPEN, or COMPLETED"]
  I["Sort by plannedFinishDate ascending or descending"]
  J["Populate model attributes"]
  K["Render tasks.html"]
  L(["End"])

  A --> B --> C --> D
  D -->|"No"| E --> G
  D -->|"Yes"| F --> G
  G --> H --> I --> J --> K --> L
```

### Architecture Diagram

```mermaid
architecture-beta
  group client(internet)[Client]
  service browser(internet)[Web Browser] in client

  group app(server)[Todo Application Host]
  service spring(server)[Spring Boot MVC App] in app
  service security(server)[Spring Security] in app
  service thymeleaf(server)[Thymeleaf Views] in app

  group data(database)[File Persistence]
  service users(disk)[Users JSON] in data
  service tasks(disk)[Per User Tasks JSON] in data

  browser:R --> L:spring
  spring:R --> L:security
  spring:B --> T:thymeleaf
  spring:R --> L:tasks
  spring:B --> T:users
```

## API and View Contract

| Endpoint | Method | Parameters | Behavior |
|---|---|---|---|
| `/tasks` | GET | `status`, `sort` | Returns authenticated user's filtered and sorted tasks. |
| `/tasks` | POST | `TaskForm` | Creates a task and redirects to `/tasks`. Existing behavior remains unchanged. |
| `/tasks/{taskId}/complete` | POST | `taskId` path variable | Marks authenticated user's task as completed. Existing behavior remains unchanged. |

## Error Handling and Edge Cases

- Unknown `status` values are treated as `ALL`.
- Unknown `sort` values are treated as the selected safe default.
- Empty parameter values are treated as defaults.
- No matching tasks renders the existing empty-state message.
- Only tasks loaded via the authenticated username are filtered and sorted.
- Sorting should be null-safe even though planned finish date is currently required by `TaskForm`.

## Wireframes and User Flow

```mermaid
flowchart TD
  Login["Login Page"] --> Dashboard["Todo Dashboard"]
  Dashboard --> Controls["Filter and Sort Controls"]
  Controls --> All["All Tasks"]
  Controls --> Open["Open Tasks Only"]
  Controls --> Completed["Completed Tasks Only"]
  Controls --> Asc["Planned Finish Ascending"]
  Controls --> Desc["Planned Finish Descending"]
  All --> List["Task List"]
  Open --> List
  Completed --> List
  Asc --> List
  Desc --> List
  List --> Complete["Mark Completed"]
  List --> Create["Create New Task"]
  Complete --> Dashboard
  Create --> Dashboard
```

```mermaid
flowchart TB
  Page["Todo Dashboard"] --> Header["Header: username and logout"]
  Header --> Form["Create New Task form"]
  Form --> Toolbar["Task List Toolbar"]
  Toolbar --> StatusSelect["Status: All | Open | Completed"]
  Toolbar --> SortSelect["Sort: Planned finish ascending | descending"]
  Toolbar --> ApplyButton["Apply"]
  ApplyButton --> TaskCards["Task cards with title, dates, status, complete action"]
```

## Alternatives Considered

| Alternative | Pros | Cons | Decision |
|---|---|---|---|
| Service-layer filtering/sorting | Minimal change, preserves repository abstraction, easy to test | Loads all user tasks before filtering | Recommended for current file-based app. |
| Repository-level query methods | Efficient if storage becomes database-backed | Over-specializes file repository and expands interface now | Defer until persistence changes. |
| Client-side filtering/sorting | Fast UI after load | Exposes all current user's tasks to page and duplicates logic | Not preferred for server-rendered flow. |

## Rollout Plan

1. Add query enums or parser helpers for status filter and sort order.
2. Extend service method and implement filtering/sorting.
3. Update controller GET `/tasks` to accept optional params and populate model attributes.
4. Update `tasks.html` and CSS for filter/sort controls.
5. Add service and controller tests.
6. Run `mvn test` and regression-test registration, login, create, complete, filter, and sort flows.
