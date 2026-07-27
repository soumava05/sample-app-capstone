# EPMCDMETST-55854 Solution Diagrams

## Architecture Summary

Design for adding task list filtering and case-insensitive title/description search to the server-rendered Todo Dashboard while preserving layered architecture, Spring Security authentication, user isolation, and existing task sort order.

## High-Level Design

```mermaid
---
title: Todo App High-Level Architecture for Task Filtering and Search
config:
  theme: default
---
flowchart LR
  User([Authenticated User]) --> Browser[Browser]
  Browser -->|HTTPS GET /tasks?status&q| Security[Spring Security Filter Chain]
  Security -->|Authenticated principal| TaskController[TaskController]
  TaskController -->|username, status, q| TaskService[TaskService]
  TaskService -->|normalized username| TaskRepository[TaskRepository]
  TaskRepository --> FileStorage[(Per-user JSON task file)]
  TaskService -->|filter by status and q| FilterLogic[Filtering Logic]
  FilterLogic -->|sorted filtered tasks| TaskController
  TaskController -->|model: tasks, status, q, empty state| Thymeleaf[Thymeleaf tasks.html]
  Thymeleaf --> Browser
```

## Low-Level Design

```mermaid
---
title: Low-Level Component Design for Filtered Task Dashboard
config:
  theme: default
---
classDiagram
  direction LR
  class TaskController {
    <<controller>>
    +taskDashboard(Authentication, String status, String q, Model) String
    +createTask(Authentication, TaskForm, BindingResult, Model) String
    +markTaskCompleted(Authentication, String taskId) String
  }
  class TaskService {
    <<interface>>
    +createTask(String username, TaskForm taskForm) TodoTask
    +getUserTasks(String username) List~TodoTask~
    +getUserTasks(String username, TaskFilter filter) List~TodoTask~
    +markCompleted(String username, String taskId) void
  }
  class DefaultTaskService {
    <<service>>
    -TaskRepository taskRepository
    +getUserTasks(String username, TaskFilter filter) List~TodoTask~
    -matchesStatus(TodoTask task, TaskFilter filter) boolean
    -matchesQuery(TodoTask task, TaskFilter filter) boolean
  }
  class TaskFilter {
    <<dto>>
    +TaskStatus status
    +String query
    +boolean hasStatusFilter()
    +boolean hasQuery()
  }
  class TaskRepository {
    <<interface>>
    +findByUsername(String username) List~TodoTask~
    +save(TodoTask task) TodoTask
    +findById(String username, String taskId) Optional~TodoTask~
    +update(TodoTask task) void
  }
  class FileTaskRepository {
    <<repository>>
    +findByUsername(String username) List~TodoTask~
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
  TaskController --> TaskService : delegates
  TaskService <|.. DefaultTaskService : implements
  DefaultTaskService --> TaskRepository : reads user tasks
  TaskRepository <|.. FileTaskRepository : implements
  DefaultTaskService --> TaskFilter : applies
  FileTaskRepository --> TodoTask : returns sorted list
  TodoTask --> TaskStatus : has
```

## Component Diagram

```mermaid
C4Component
  title Component Diagram - Todo Task Filtering and Search
  Person(user, "Authenticated User", "Manages private todo tasks")
  Container_Boundary(app, "Todo Spring Boot Application") {
    Component(security, "Security Filter Chain", "Spring Security", "Protects /tasks")
    Component(controller, "TaskController", "Spring MVC", "Accepts status and q parameters")
    Component(view, "tasks.html", "Thymeleaf", "Renders filters, tasks, and empty states")
    Component(service, "DefaultTaskService", "Service", "Applies status and search filtering")
    Component(repo, "FileTaskRepository", "Repository", "Reads per-user JSON files")
    Component(storage, "FileStorageManager", "Storage Utility", "JSON file read/write")
  }
  ContainerDb(files, "File Storage", "JSON Files", "storage/tasks/{username}.json")
  Rel(user, security, "Requests /tasks", "HTTPS")
  Rel(security, controller, "Authenticated request")
  Rel(controller, service, "getUserTasks(username, filter)")
  Rel(service, repo, "findByUsername(normalizedUsername)")
  Rel(repo, storage, "readList/writeList")
  Rel(storage, files, "Read/write JSON")
  Rel(controller, view, "Model with tasks and filters")
```

## Sequence Diagram

```mermaid
sequenceDiagram
  actor User
  participant Browser
  participant Security as Spring Security
  participant Controller as TaskController
  participant Service as DefaultTaskService
  participant Repo as FileTaskRepository
  participant File as User Task JSON
  participant View as Thymeleaf tasks.html
  User->>Browser: Select status and enter search text
  Browser->>Security: GET /tasks?status=OPEN&q=report
  Security->>Security: Verify authenticated session
  Security->>Controller: Forward request with Authentication
  Controller->>Controller: Build TaskFilter from status and q
  Controller->>Service: getUserTasks(username, filter)
  Service->>Service: Normalize username
  Service->>Repo: findByUsername(normalizedUsername)
  Repo->>File: Read storage/tasks/{username}.json
  File-->>Repo: List of user's tasks
  Repo-->>Service: Tasks sorted by taskDate then createdAt
  Service->>Service: Apply status filter
  Service->>Service: Apply case-insensitive title/description search
  Service-->>Controller: Filtered task list
  Controller->>View: Add tasks, status, q, empty-state fields to model
  View-->>Browser: Render dashboard HTML
```

## Deployment Diagram

```mermaid
---
title: Deployment View - Server-Rendered Todo App
config:
  theme: default
---
flowchart TB
  subgraph Client["Client Device"]
    Browser[Web Browser]
  end
  subgraph Host["Application Host"]
    JVM[Java 21 JVM]
    App[Spring Boot Todo Application]
    Storage[(Local File Storage)]
  end
  Browser -->|HTTP on configured port 8090| App
  App --> JVM
  App -->|Read/write users.json and tasks JSON| Storage
```

## Data Flow Diagram

```mermaid
---
title: Data Flow - Task Filter and Search
config:
  theme: default
---
flowchart TD
  A([User submits filter form]) --> B[/GET query params: status, q/]
  B --> C[TaskController validates request context]
  C --> D[Extract authenticated username]
  D --> E[Create TaskFilter]
  E --> F[DefaultTaskService normalizes username]
  F --> G[Repository reads only that user's task file]
  G --> H[Sorted task list]
  H --> I{Status filter?}
  I -->|OPEN or COMPLETED| J[Keep matching status]
  I -->|ALL or blank| K[Keep all statuses]
  J --> L{Search query?}
  K --> L
  L -->|Present| M[Case-insensitive contains in title or description]
  L -->|Absent| N[No search filtering]
  M --> O[Filtered result]
  N --> O
  O --> P{Any matches?}
  P -->|Yes| Q[Render task cards]
  P -->|No| R[Render no-results empty state]
```

## Architecture Diagram

```mermaid
---
title: Layered Architecture - Filter/Search Enhancement
config:
  theme: default
---
flowchart LR
  subgraph Presentation["Presentation Layer"]
    TasksTemplate["tasks.html\nGET filter form"]
    TaskController["TaskController\nGET /tasks"]
  end
  subgraph SecurityLayer["Security Layer"]
    SpringSecurity["Spring Security\nform login and sessions"]
  end
  subgraph Business["Business Layer"]
    TaskServiceInterface["TaskService interface"]
    DefaultTaskService["DefaultTaskService\nfilter/search rules"]
    TaskFilter["TaskFilter\nstatus and query"]
  end
  subgraph Persistence["Persistence Layer"]
    TaskRepositoryInterface["TaskRepository interface"]
    FileTaskRepository["FileTaskRepository\nuser-scoped JSON access"]
    JsonStorage[("storage/tasks/{username}.json")]
  end
  Browser([Browser]) --> SpringSecurity
  SpringSecurity --> TaskController
  TaskController --> TasksTemplate
  TaskController --> TaskServiceInterface
  TaskServiceInterface --> DefaultTaskService
  DefaultTaskService --> TaskFilter
  DefaultTaskService --> TaskRepositoryInterface
  TaskRepositoryInterface --> FileTaskRepository
  FileTaskRepository --> JsonStorage
```

## Wireframes and User Flow

```mermaid
journey
  title Filter and Search User Journey
  section Open Dashboard
    Login successfully: 5: User
    View Todo Dashboard: 5: User
  section Filter Tasks
    Select Open or Completed status: 5: User
    Submit GET filter form: 5: User, System
    Review filtered tasks: 5: User
  section Search Tasks
    Enter title or description keywords: 5: User
    Submit search: 5: User, System
    See matching tasks or no-results message: 4: User
  section Continue Existing Actions
    Create a new task: 5: User
    Mark task completed: 5: User
```

```mermaid
---
title: Todo Dashboard Wireframe
config:
  theme: default
---
flowchart TB
  Page["Todo Dashboard\nUser: alice | Logout"] --> Create["Create New Task Form\nTitle | Description | Task Date | Planned Finish Date | Add Task"]
  Create --> Filter["Filter and Search Form\nStatus: All/Open/Completed | Search: keyword | Apply | Clear"]
  Filter --> State["Current filter state\nShowing: OPEN, Search: keyword"]
  State --> Results{Matching tasks?}
  Results -->|Yes| Cards["Task Cards\nTitle, Description, Dates, Status, Mark Completed"]
  Results -->|No| Empty["No tasks match the current filter. Clear filters or try another search."]
```

## Alternatives Considered

| Alternative | Pros | Cons | Decision |
|---|---|---|---|
| Service-layer filtering after `findByUsername` | Minimal change, preserves repository contract and sorting, easy to test | Reads all user tasks before filtering | Recommended |
| Repository-level filtering | Better for larger datasets and future DB migration | Adds complexity to current file-backed app | Defer |
| Client-side filtering | Fast UI interactions | Conflicts with server-rendered requirement and may expose unnecessary data | Rejected |

## Security, Observability, Scalability, Rollout

- Security: derive username from `Authentication`; never from request parameters.
- User isolation: use normalized authenticated username for repository access.
- Validation: normalize blank query to no search and support known statuses safely.
- Observability: existing logs are sufficient for this change; add debug logs only if needed.
- Scalability: current in-memory filtering is acceptable; future DB repositories can push filters into indexed queries.
- Rollout: implement optional parameters on existing `/tasks`, add tests, verify no-filter behavior.
