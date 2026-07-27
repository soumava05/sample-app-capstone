# Solution Diagrams — EPMCDMETST-55979

## High Level Design

### Architecture Approach

The task filtering feature is implemented as an incremental enhancement to the existing Spring Boot layered MVC architecture. The UI submits filter criteria as query parameters to `GET /tasks`; `TaskController` binds optional values, `TaskService` retrieves only the authenticated user's tasks, and filtering is performed in memory over the user-scoped task list returned by the file repository.

### Technology Choices

| Concern | Choice | Rationale |
|---|---|---|
| UI | Thymeleaf server-rendered filter form | Preserves existing architecture and progressive enhancement. |
| Backend | Spring MVC controller + service method overload | Keeps web binding separate from business filtering rules. |
| Persistence | Existing JSON file repository | Jira asks for in-memory filtering over user task list; no DB dependency needed. |
| Security | Spring Security authenticated principal | Prevents accepting user identity from request parameters. |
| Observability | Application logs around invalid filters and request handling if added later | Lightweight for sample application. |
| Scalability | Suitable for per-user local task files; can later move predicate to repository/database | Maintains extension path without premature migration. |

### High Level Architecture Diagram

```mermaid
---
title: Todo Dashboard Filtering - High Level Architecture
config:
  theme: default
---
flowchart LR
  User(["Authenticated User"])
  Browser["Browser\nThymeleaf tasks page"]

  subgraph App["Spring Boot Todo Application"]
    Security["Spring Security\nSession and principal"]
    Controller["TaskController\nGET /tasks with filters"]
    Service["TaskService\nUser-scoped filtering rules"]
    Repository["TaskRepository\nUser task lookup"]
    Storage["FileStorageManager\nJSON read/write"]
  end

  Files[("storage/tasks/{username}.json")]

  User --> Browser
  Browser -->|"GET /tasks?status=OPEN&fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD"| Security
  Security --> Controller
  Controller -->|"username from Authentication, filter params"| Service
  Service -->|"normalized username"| Repository
  Repository --> Storage
  Storage --> Files
  Repository -->|"all tasks for user"| Service
  Service -->|"filtered tasks"| Controller
  Controller -->|"model: tasks and selected filter values"| Browser
```

### Alternatives Considered

| Alternative | Pros | Cons | Decision |
|---|---|---|---|
| Filter in controller | Minimal service contract change | Leaks business rules into web layer and weakens testability | Rejected |
| Add repository filtering method | Encapsulates query closer to persistence | Current persistence is file list; couples repository to UI filters prematurely | Deferred |
| Add `TaskFilterCriteria` DTO | Clean extensibility for more filters | Adds an extra type for a small feature | Recommended if more filters are expected |
| Use query parameter method overload | Simple, targeted change | Signature can grow if filters expand | Acceptable for this story |

## Low Level Design

### Component Diagram

```mermaid
---
title: Task Filtering Component Design
config:
  theme: default
---
flowchart TB
  subgraph Web["web package"]
    TaskController["TaskController\n- binds optional request params\n- populates model\n- returns tasks view"]
  end

  subgraph DTO["dto package"]
    TaskForm["TaskForm\nCreate task validation"]
    FilterParams["Filter Inputs\nstatus, fromDate, toDate"]
  end

  subgraph Service["service package"]
    TaskService["TaskService interface\ngetUserTasks with optional filters"]
    DefaultTaskService["DefaultTaskService\n- normalize username\n- retrieve user tasks\n- apply predicates\n- validate date range"]
  end

  subgraph Domain["domain package"]
    TodoTask["TodoTask\nid, username, title, taskDate, status"]
    TaskStatus["TaskStatus\nOPEN, COMPLETED"]
  end

  subgraph Repository["repository package"]
    TaskRepository["TaskRepository\nfindByUsername"]
    FileTaskRepository["FileTaskRepository\nread per-user JSON file"]
  end

  FileStore[("JSON File Store\nstorage/tasks/{username}.json")]

  TaskController --> TaskForm
  TaskController --> FilterParams
  TaskController --> TaskService
  TaskService <|-.-> DefaultTaskService
  DefaultTaskService --> TaskRepository
  TaskRepository <|-.-> FileTaskRepository
  FileTaskRepository --> FileStore
  DefaultTaskService --> TodoTask
  DefaultTaskService --> TaskStatus
```

### API and Contract Design

```mermaid
---
title: Filtering Contract and Data Model
config:
  theme: default
---
classDiagram
  direction LR

  class TaskController {
    +taskDashboard(authentication, model, status, fromDate, toDate) String
    +createTask(authentication, taskForm, bindingResult, model) String
    +markTaskCompleted(authentication, taskId) String
  }

  class TaskService {
    <<interface>>
    +createTask(username, taskForm) TodoTask
    +getUserTasks(username) List~TodoTask~
    +getUserTasks(username, status, fromDate, toDate) List~TodoTask~
    +markCompleted(username, taskId) void
  }

  class DefaultTaskService {
    -TaskRepository taskRepository
    +getUserTasks(username, status, fromDate, toDate) List~TodoTask~
    -normalizeUsername(username) String
    -validateFilterRange(fromDate, toDate) void
  }

  class TaskRepository {
    <<interface>>
    +findByUsername(username) List~TodoTask~
    +save(task) TodoTask
    +findById(username, taskId) Optional~TodoTask~
    +update(task) void
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

  TaskController --> TaskService
  TaskService <|.. DefaultTaskService
  DefaultTaskService --> TaskRepository
  DefaultTaskService --> TodoTask
  TodoTask --> TaskStatus
```

### Sequence Diagram

```mermaid
---
title: Apply Status and Date Filters Sequence
config:
  theme: default
---
sequenceDiagram
  actor User as Authenticated User
  participant Browser as Browser
  participant Security as Spring Security
  participant Controller as TaskController
  participant Service as DefaultTaskService
  participant Repo as FileTaskRepository
  participant Store as JSON Task File

  User->>Browser: Select Status and Date range
  Browser->>Security: GET /tasks?status=OPEN&fromDate=2026-06-01&toDate=2026-06-30
  Security->>Controller: Authenticated request with principal
  Controller->>Controller: Bind optional filter parameters
  Controller->>Service: getUserTasks(username, status, fromDate, toDate)
  Service->>Service: Normalize username
  Service->>Repo: findByUsername(normalizedUsername)
  Repo->>Store: Read storage/tasks/{username}.json
  Store-->>Repo: User task list
  Repo-->>Service: Sorted user task list
  Service->>Service: Apply status predicate if not ALL
  Service->>Service: Apply inclusive taskDate range predicates
  Service-->>Controller: Filtered task list
  Controller->>Controller: Add tasks and selected filters to model
  Controller-->>Browser: Render tasks.html
  Browser-->>User: Display filtered task list
```

### Data Flow Diagram

```mermaid
---
title: Task Filter Data Flow
config:
  theme: default
---
flowchart TD
  A(["User selects filters"])
  B["HTTP query parameters\nstatus, fromDate, toDate"]
  C["TaskController\nparameter binding"]
  D["Authentication principal\nusername"]
  E["DefaultTaskService\nnormalize and validate"]
  F["TaskRepository.findByUsername"]
  G[("Per-user JSON task file")]
  H["In-memory filter predicates"]
  I["Filtered List<TodoTask>"]
  J["Thymeleaf model\ntasks, taskForm, username, selected filters"]
  K(["Rendered dashboard"])

  A --> B
  B --> C
  D --> C
  C --> E
  E --> F
  F --> G
  G --> F
  F --> H
  H --> I
  I --> J
  J --> K
```

### Deployment Diagram

```mermaid
---
title: Local Deployment View
config:
  theme: default
---
flowchart LR
  UserDevice["User Device\nWeb Browser"]

  subgraph Host["Application Host"]
    JVM["Java 21 JVM"]
    App["todo-app.jar\nSpring Boot 3.3.2"]
    Templates["Thymeleaf Templates\ntasks.html"]
    Static["Static Assets\nstyles.css"]
    Storage[("Local Storage\nusers.json and tasks/*.json")]
  end

  UserDevice -->|"HTTPS or HTTP in local dev"| App
  App --> JVM
  App --> Templates
  App --> Static
  App --> Storage
```

### Architecture Diagram

```mermaid
---
title: Runtime Architecture for Filtering Feature
config:
  theme: default
---
C4Container
  title Todo App Container View - Task Filtering

  Person(user, "Authenticated User", "Registered user managing personal tasks")

  System_Boundary(todo, "Todo Application") {
    Container(web, "Spring MVC Web Layer", "Spring Boot Controller", "Handles /tasks requests and model rendering")
    Container(service, "Task Service", "Java Service", "Applies task workflows and filter predicates")
    Container(repo, "File Repository", "Java Repository", "Loads and saves per-user task JSON files")
    ContainerDb(files, "Local JSON Storage", "File System", "Stores users and user-specific tasks")
    Container(ui, "Thymeleaf UI", "Server-rendered HTML", "Displays filters, create form, and task list")
  }

  Rel(user, ui, "Uses", "Browser")
  Rel(ui, web, "Submits filter query", "HTTP GET")
  Rel(web, service, "Delegates with authenticated username and filters", "Java call")
  Rel(service, repo, "Retrieves authenticated user's tasks", "Java call")
  Rel(repo, files, "Read and write JSON", "File I/O")
  Rel(web, ui, "Renders selected filters and filtered tasks", "HTML")
```

## Wireframes and User Flow

### Filter Dashboard Wireframe

```mermaid
---
title: Tasks Page Wireframe with Filters
config:
  theme: default
---
flowchart TB
  Page["Tasks Dashboard"]
  Header["Header: username and logout"]
  Create["Create New Task Form\nTitle, Description, Task Date, Planned Finish Date, Add Task"]
  Filters["Filter Tasks Form\nStatus dropdown: All/Open/Completed\nFrom date input\nTo date input\nApply button\nReset link"]
  List["My Task List\nFiltered task cards"]
  Empty["Empty state\nNo tasks match selected filters"]

  Page --> Header
  Page --> Create
  Page --> Filters
  Filters --> List
  Filters --> Empty
```

### User Journey

```mermaid
journey
  title Task Filtering User Journey
  section Open Dashboard
    Login successfully: 5: User
    Open tasks page: 5: User
  section Apply Filters
    Select status filter: 5: User
    Select optional date range: 4: User
    Click Apply: 5: User, System
    Review narrowed task list: 5: User
  section Clear Filters
    Click Reset: 5: User
    View all personal tasks: 5: User, System
```

## Error Handling and Edge Cases

| Case | Expected Handling |
|---|---|
| No filters supplied | Return all authenticated user's tasks. |
| Status is `ALL` or blank | Do not apply status predicate. |
| Status is `OPEN` | Return only open tasks. |
| Status is `COMPLETED` | Return only completed tasks. |
| Only `fromDate` supplied | Return tasks with `taskDate >= fromDate`. |
| Only `toDate` supplied | Return tasks with `taskDate <= toDate`. |
| Both dates supplied | Return tasks with `fromDate <= taskDate <= toDate`. |
| `fromDate > toDate` | Recommended: reject with model error and preserve inputs. |
| Authenticated user has no tasks | Render normal empty state. |
| Filters match no tasks | Render empty filtered state. |
| User attempts cross-user filtering | Not possible through API because username is derived from authentication only. |

## Rollout Plan

1. Add service filtering contract and implementation.
2. Update controller binding and model population.
3. Update `tasks.html` with GET filter form and Reset link.
4. Add/extend unit tests for service and controller combinations.
5. Run `mvn test`.
6. Deploy as a regular application update; no data migration required.

## Mermaid Validation Notes

All diagrams are authored as Mermaid source blocks. No rendering tool was available in this execution environment, so diagrams are published as Mermaid source only.
