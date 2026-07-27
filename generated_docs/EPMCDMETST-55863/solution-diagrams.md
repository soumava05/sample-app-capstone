# Solution Diagrams - EPMCDMETST-55863

## High Level Design

```mermaid
---
title: Todo Dashboard Filter - High Level Design
config:
  theme: default
---
flowchart LR
  User([Authenticated User]) --> Browser[Browser]
  Browser -->|HTTPS GET /tasks with optional query params| MVC[Spring MVC Todo Application]

  subgraph App["Todo App - Layered Modular Monolith"]
    direction TB
    Security[Spring Security Session Auth]
    TaskController[TaskController]
    TaskService[TaskService]
    TaskRepository[TaskRepository]
    Templates[Thymeleaf tasks.html]
  end

  MVC --> Security
  Security --> TaskController
  TaskController -->|TaskFilterForm| TaskService
  TaskService -->|normalized username plus filter| TaskRepository
  TaskRepository --> Storage[(JSON Task Files per User)]
  TaskRepository --> TaskService
  TaskService --> TaskController
  TaskController --> Templates
  Templates --> Browser

  classDef secure fill:#e8f4ff,stroke:#2b6cb0,color:#1a365d
  classDef data fill:#f0fff4,stroke:#2f855a,color:#22543d
  class Security secure
  class Storage data
```

## Low Level Design

```mermaid
---
title: Filtered Task Retrieval - Low Level Design
config:
  theme: default
---
flowchart TB
  Request([GET /tasks]) --> Bind[Bind query params to TaskFilterForm]
  Bind --> Validate{Is toDate before fromDate?}
  Validate -->|Yes| Error[Add validation error and preserve filter values]
  Error --> LoadDefault[Load current user's existing tasks or empty list]
  LoadDefault --> RenderError[Render tasks.html with error]
  Validate -->|No| AuthName[Read Authentication.getName]
  AuthName --> Normalize[Normalize username in service]
  Normalize --> Repo[TaskRepository.findByUsernameAndFilter]
  Repo --> ReadFile[Read storage/tasks/username.json]
  ReadFile --> Missing{File exists?}
  Missing -->|No| Empty[Return empty list]
  Missing -->|Yes| Filter[Apply status and inclusive date predicates]
  Filter --> Sort[Sort by taskDate then createdAt]
  Empty --> Render[Render tasks.html]
  Sort --> Render
```

## Component Diagram

```mermaid
---
title: Component Diagram - Todo Task Filters
config:
  theme: default
---
flowchart LR
  subgraph Web["Web Layer"]
    TaskController["TaskController\nGET /tasks\nPOST /tasks\nPOST /tasks/{id}/complete"]
    TaskFilterForm["TaskFilterForm\nstatus\nfromDate\ntoDate"]
    TaskForm["TaskForm\ntitle\ndescription\ntaskDate\nplannedFinishDate"]
    TasksView["tasks.html\nFilter Bar\nTask List\nCreate Task Form"]
  end

  subgraph Service["Service Layer"]
    TaskService["TaskService Interface"]
    DefaultTaskService["DefaultTaskService\nnormalize username\nvalidate ranges\norchestrate filtering"]
  end

  subgraph Domain["Domain Layer"]
    TodoTask["TodoTask\nid username title description taskDate plannedFinishDate status createdAt"]
    TaskStatus["TaskStatus\nOPEN\nCOMPLETED"]
  end

  subgraph Repository["Repository Layer"]
    TaskRepository["TaskRepository Interface"]
    FileTaskRepository["FileTaskRepository\nread user JSON\nfilter and sort"]
    FileStorageManager["FileStorageManager\nJSON read/write"]
  end

  Storage[("storage/tasks/<username>.json")]

  TaskController --> TaskFilterForm
  TaskController --> TaskForm
  TaskController --> TaskService
  TaskService --> DefaultTaskService
  DefaultTaskService --> TaskRepository
  TaskRepository --> FileTaskRepository
  FileTaskRepository --> FileStorageManager
  FileStorageManager --> Storage
  DefaultTaskService --> TaskStatus
  FileTaskRepository --> TodoTask
  TaskController --> TasksView
```

## Sequence Diagram

```mermaid
---
title: Sequence Diagram - Apply Task Filters
config:
  theme: default
---
sequenceDiagram
  actor User as Authenticated User
  participant Browser
  participant Security as Spring Security
  participant Controller as TaskController
  participant Service as DefaultTaskService
  participant Repository as FileTaskRepository
  participant Storage as User Task JSON File
  participant View as Thymeleaf tasks.html

  User->>Browser: Select status and date range, click Apply
  Browser->>Security: GET /tasks?status=OPEN&fromDate=2026-06-01&toDate=2026-06-30
  Security->>Controller: Authenticated request with principal
  Controller->>Service: getUserTasks(username, filter)
  Service->>Service: Validate fromDate <= toDate and normalize username
  alt Invalid date range
    Service-->>Controller: Validation error
    Controller->>View: Render tasks page with error and filter values
    View-->>Browser: HTML response
  else Valid filter
    Service->>Repository: findByUsername(username, filter)
    Repository->>Storage: Read storage/tasks/username.json
    Storage-->>Repository: List of TodoTask records
    Repository->>Repository: Apply status and inclusive date predicates
    Repository->>Repository: Sort by taskDate then createdAt
    Repository-->>Service: Filtered tasks
    Service-->>Controller: Filtered tasks
    Controller->>View: Render tasks page with filtered list and filter values
    View-->>Browser: HTML response
  end
```

## Deployment Diagram

```mermaid
---
title: Deployment Diagram - Local or Single Node Runtime
config:
  theme: default
---
flowchart TB
  UserDevice["User Device\nBrowser"] -->|HTTP or HTTPS| Host["Application Host"]

  subgraph Host["Application Host"]
    direction TB
    JVM["Java 21 JVM"]
    App["Spring Boot Todo App\nPort 8090"]
    Static["Static CSS and Thymeleaf Templates"]
    Files[("Local File System\nstorage/users.json\nstorage/tasks/<username>.json")]
    JVM --> App
    App --> Static
    App --> Files
  end

  App -->|BCrypt password verification| Security["Spring Security"]
  App -->|Jackson JavaTimeModule| Json["JSON Serialization"]
```

## Data Flow Diagram

```mermaid
---
title: Data Flow - Filtered Dashboard Request
config:
  theme: default
---
flowchart LR
  Query[/"Query Params: status, fromDate, toDate"/] --> Controller[TaskController]
  Principal[/"Authenticated Principal"/] --> Controller
  Controller --> FilterDTO[TaskFilterForm]
  FilterDTO --> Service[DefaultTaskService]
  Principal --> Service
  Service --> Guard{Valid date range?}
  Guard -->|No| ErrorModel[/"Model: validation error and filter values"/]
  ErrorModel --> View[tasks.html]
  Guard -->|Yes| UserScopedRead[User-scoped repository read]
  UserScopedRead --> JSON[(Task JSON for authenticated user only)]
  JSON --> Predicates[Apply filter predicates]
  Predicates --> Sorted[Sorted filtered tasks]
  Sorted --> Model[/"Model: tasks, taskForm, filterForm, username"/]
  Model --> View
  View --> HTML[/Rendered HTML/]
```

## Architecture Diagram

```mermaid
---
title: Architecture View - Security, Scalability, Observability
config:
  theme: default
---
flowchart TB
  subgraph Client["Client Boundary"]
    Browser[Browser]
  end

  subgraph SecurityBoundary["Authenticated Application Boundary"]
    SecurityFilter["SecurityFilterChain\n/login and /register public\n/tasks authenticated"]
    Controller["TaskController\nserver-side binding"]
    Service["TaskService\nbusiness validation"]
    Repository["TaskRepository\nuser-scoped persistence abstraction"]
  end

  subgraph Persistence["Persistence Boundary"]
    UserFile[(users.json)]
    TaskFiles[(tasks/<username>.json)]
  end

  subgraph Quality["Quality Attributes"]
    Tests["Unit and MVC Tests\nAC coverage"]
    Logs["Spring Boot Logs\nvalidation and exception diagnostics"]
    FutureDB["Future DB Adapter\nreplace repository implementation"]
  end

  Browser --> SecurityFilter
  SecurityFilter --> Controller
  Controller --> Service
  Service --> Repository
  Repository --> TaskFiles
  Repository --> UserFile
  Tests -.-> Controller
  Tests -.-> Service
  Tests -.-> Repository
  Service -.-> Logs
  Repository -.-> FutureDB
```

## Wireframe - Todo Dashboard Filter Bar

```mermaid
---
title: Wireframe - Todo Dashboard with Filters
config:
  theme: default
---
flowchart TB
  Page["Todo Dashboard"] --> Header["User: alice | Logout"]
  Header --> Create["Create New Task\nTitle | Description | Task Date | Planned Finish Date | Add Task"]
  Create --> Filter["Filter Tasks\nStatus dropdown: All, OPEN, COMPLETED\nFrom date input | To date input\nApply button | Clear link"]
  Filter --> Error["Optional validation message\nTo date cannot be before From date"]
  Filter --> List["My Task List"]
  List --> Card1["Task Card\nTitle, description, status\nTask Date, Planned Finish Date\nMark Completed button when OPEN"]
  List --> Empty["No tasks match the selected filters"]
```

## API and Contract Notes

| Endpoint | Method | Query or Form Fields | Response |
|---|---|---|---|
| `/tasks` | GET | Optional `status`, `fromDate`, `toDate` | `tasks.html` with filtered authenticated-user task list |
| `/tasks` | POST | Existing `TaskForm` fields | Redirect to `/tasks` on success or render validation errors |
| `/tasks/{taskId}/complete` | POST | Path variable `taskId` | Redirect to `/tasks` after status update |

## Configuration and Rollout Notes

- No new runtime configuration is required.
- Existing `app.storage.root-path` remains the persistence root.
- Roll out as a backward-compatible enhancement because task JSON schema is unchanged.
- If a future database adapter is introduced, keep the service contract stable and move predicates into indexed queries.
