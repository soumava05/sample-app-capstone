# Solution Diagrams - EPMCDMETST-55996

## High-Level Design

```mermaid
---
title: Todo Filter High-Level Design
config:
  theme: default
---
flowchart LR
  User([Authenticated User]) --> Browser["Browser with Thymeleaf UI"]
  Browser -->|"GET /tasks with optional status, from, to"| Security["Spring Security Filter Chain"]
  Security --> Controller["TaskController"]
  Controller --> Service["TaskService"]
  Service --> Repository["TaskRepository"]
  Repository --> Storage[("JSON File Storage")]
  Service -->|"Filtered and sorted tasks"| Controller
  Controller -->|"Model: tasks, filters, errors"| View["tasks.html"]
  View --> Browser

  subgraph SecurityBoundary["Security Boundary"]
    Security
    Controller
    Service
  end

  subgraph PersistenceBoundary["Persistence Boundary"]
    Repository
    Storage
  end
```

## Low-Level Component Diagram

```mermaid
---
title: Todo Filter Component Design
config:
  theme: default
---
flowchart TB
  subgraph WebLayer["Web Layer"]
    TaskController["TaskController\nGET /tasks"]
    TasksTemplate["tasks.html\nFilter form and task list"]
  end

  subgraph DtoLayer["DTO and Criteria Layer"]
    TaskFilterCriteria["TaskFilterCriteria\nstatus, from, to"]
    TaskForm["TaskForm\ncreate task input"]
  end

  subgraph ServiceLayer["Service Layer"]
    TaskService["TaskService interface"]
    DefaultTaskService["DefaultTaskService\nfilter, create, complete"]
  end

  subgraph DomainLayer["Domain Layer"]
    TodoTask["TodoTask"]
    TaskStatus["TaskStatus\nOPEN, COMPLETED"]
  end

  subgraph RepositoryLayer["Repository Layer"]
    TaskRepository["TaskRepository interface"]
    FileTaskRepository["FileTaskRepository\nread sorted user tasks"]
    FileStorageManager["FileStorageManager\nJSON read and write"]
    TaskFiles[("storage/tasks/<username>.json")]
  end

  TaskController --> TaskFilterCriteria
  TaskController --> TaskService
  TaskController --> TasksTemplate
  TaskService <|-.-> DefaultTaskService
  DefaultTaskService --> TaskRepository
  DefaultTaskService --> TodoTask
  DefaultTaskService --> TaskStatus
  TaskRepository <|-.-> FileTaskRepository
  FileTaskRepository --> FileStorageManager
  FileStorageManager --> TaskFiles
  TasksTemplate --> TaskFilterCriteria
  TaskForm --> TaskController
```

## Sequence Diagram

```mermaid
---
title: Apply Task Filters Sequence
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
  participant View as tasks.html

  User->>Browser: Select status and date range
  Browser->>Security: GET /tasks?status=OPEN&from=2026-06-01&to=2026-06-30
  Security->>Controller: Authenticated request with principal
  Controller->>Controller: Parse query parameters
  alt From date is after To date
    Controller->>Service: getUserTasks(username)
    Service->>Repo: findByUsername(normalizedUsername)
    Repo->>Store: Read user task JSON
    Store-->>Repo: Task list
    Repo-->>Service: Sorted user tasks
    Service-->>Controller: Unfiltered sorted tasks
    Controller->>View: Render model with filter error
    View-->>Browser: Dashboard with validation message
  else Valid filters
    Controller->>Service: getUserTasks(username, criteria)
    Service->>Repo: findByUsername(normalizedUsername)
    Repo->>Store: Read user task JSON
    Store-->>Repo: Task list
    Repo-->>Service: Sorted user tasks
    Service->>Service: Apply status and date predicates
    Service-->>Controller: Filtered sorted tasks
    Controller->>View: Render tasks with selected filters
    View-->>Browser: Filtered dashboard
  end
```

## Deployment Diagram

```mermaid
---
title: Todo App Deployment View
config:
  theme: default
---
flowchart TB
  UserDevice["User Device\nBrowser"] -->|"HTTPS in production or HTTP local"| AppHost["Application Host"]

  subgraph AppHost["Application Host"]
    JavaRuntime["Java 21 Runtime"]
    SpringBoot["Spring Boot Todo App\nPort 8090"]
    LocalDisk[("Local Disk\nstorage directory")]
    JavaRuntime --> SpringBoot
    SpringBoot --> LocalDisk
  end

  subgraph BuildSystem["Build and Test"]
    Maven["Maven"] --> TestNG["TestNG and Mockito Tests"]
    Maven --> Jar["Executable Spring Boot Artifact"]
  end

  Jar -.-> SpringBoot
```

## Data Flow Diagram

```mermaid
---
title: Task Filter Data Flow
config:
  theme: default
---
flowchart LR
  FilterInput[/"Filter Inputs\nstatus, from, to"/] --> Request["GET /tasks Query Parameters"]
  Request --> Controller["TaskController validates request"]
  Controller --> Criteria["TaskFilterCriteria"]
  Criteria --> Service["DefaultTaskService"]
  Principal["Authenticated Principal"] --> Controller
  Controller --> Service
  Service --> Repo["TaskRepository.findByUsername"]
  Repo --> TaskJson[("User Task JSON File")]
  TaskJson --> Repo
  Repo --> SortedTasks["Sorted user tasks"]
  SortedTasks --> Predicate["Apply status and date predicates"]
  Predicate --> FilteredTasks["Filtered task collection"]
  FilteredTasks --> Model["MVC Model"]
  Model --> Template["tasks.html"]
  Template --> Html[/"Rendered Dashboard HTML"/]
```

## Architecture Diagram

```mermaid
---
title: Layered Architecture for Task Filtering
config:
  theme: default
---
flowchart TB
  subgraph Presentation["Presentation Layer"]
    Browser["Browser"]
    Thymeleaf["Thymeleaf Templates"]
  end

  subgraph Web["Web Layer"]
    SecurityConfig["SecurityConfig"]
    AuthController["AuthController"]
    TaskController["TaskController"]
  end

  subgraph Business["Business Layer"]
    UserService["UserService"]
    TaskService["TaskService"]
    FilteringLogic["Task Filtering Logic"]
  end

  subgraph Domain["Domain Layer"]
    UserEntity["User"]
    TodoTaskEntity["TodoTask"]
    TaskStatusEnum["TaskStatus"]
  end

  subgraph Persistence["Persistence Layer"]
    UserRepository["UserRepository"]
    TaskRepository["TaskRepository"]
    FileRepositories["File Repository Implementations"]
    FileStorage["FileStorageManager"]
    JsonFiles[("JSON Files")]
  end

  Browser --> Thymeleaf
  Thymeleaf --> TaskController
  Thymeleaf --> AuthController
  SecurityConfig --> AuthController
  SecurityConfig --> TaskController
  TaskController --> TaskService
  AuthController --> UserService
  TaskService --> FilteringLogic
  FilteringLogic --> TodoTaskEntity
  TodoTaskEntity --> TaskStatusEnum
  UserService --> UserEntity
  TaskService --> TaskRepository
  UserService --> UserRepository
  TaskRepository --> FileRepositories
  UserRepository --> FileRepositories
  FileRepositories --> FileStorage
  FileStorage --> JsonFiles
```

## Wireframe and User Flow

```mermaid
---
title: Todo Dashboard Filter Wireframe
config:
  theme: default
---
flowchart TB
  Dashboard["Todo Dashboard"] --> Header["Header: username and logout"]
  Header --> CreatePanel["Create New Task Panel"]
  CreatePanel --> FilterPanel["Filter Panel"]
  FilterPanel --> StatusSelect["Status Select\nAll, Open, Completed"]
  FilterPanel --> FromDate["From Date Input"]
  FilterPanel --> ToDate["To Date Input"]
  FilterPanel --> ApplyButton["Apply Filters Button"]
  FilterPanel --> ClearLink["Clear Filters Link"]
  ApplyButton --> ValidDecision{"From date after To date?"}
  ValidDecision -->|"No"| TaskList["Filtered Task List"]
  ValidDecision -->|"Yes"| ErrorState["Validation Error Message"]
  TaskList --> EmptyDecision{"Any matching tasks?"}
  EmptyDecision -->|"Yes"| Cards["Task Cards"]
  EmptyDecision -->|"No"| EmptyState["No tasks match your filters."]
```

## Error Handling Flow

```mermaid
---
title: Filter Error Handling
config:
  theme: default
---
flowchart LR
  Request["GET /tasks"] --> Parse["Parse status, from, to"]
  Parse --> StatusValid{"Status valid?"}
  StatusValid -->|"No"| Error["Add filter validation error"]
  StatusValid -->|"Yes"| DatesValid{"Date range valid?"}
  DatesValid -->|"No"| Error
  DatesValid -->|"Yes"| Filter["Apply filters"]
  Error --> Unfiltered["Load unfiltered user tasks"]
  Filter --> Render["Render dashboard"]
  Unfiltered --> Render
```
