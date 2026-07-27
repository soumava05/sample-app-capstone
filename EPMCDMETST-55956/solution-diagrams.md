# EPMCDMETST-55956 Solution Diagrams

## High-Level Design

```mermaid
---
title: To-Do Dashboard Filter and Search - High-Level Architecture
config:
  theme: default
---
flowchart LR
  User([Authenticated User]) --> Browser[Browser]
  Browser -->|GET /tasks with status and search| Controller[TaskController]
  Controller -->|username, status, search| Service[TaskService]
  Service --> Impl[DefaultTaskService]
  Impl -->|findByUsername normalized username| Repo[TaskRepository]
  Repo --> FileRepo[FileTaskRepository]
  FileRepo --> Storage[(storage/tasks/username.json)]
  Impl -->|filter status and search text| Filtered[Filtered Task List]
  Filtered --> Controller
  Controller -->|model attributes| View[tasks.html]
  View --> Browser

  subgraph Security[Security Boundary]
    Auth[Spring Security Form Login]
    Session[Authenticated Session]
  end
  Browser --> Auth
  Auth --> Session
  Session --> Controller
```

## Low-Level Design

```mermaid
---
title: Low-Level Components for Task Dashboard Filtering
config:
  theme: default
---
classDiagram
  direction LR

  class TaskController {
    -TaskService taskService
    +rootRedirect() String
    +taskDashboard(Authentication, Model, String, String) String
    +createTask(Authentication, TaskForm, BindingResult, Model) String
    +markTaskCompleted(Authentication, String) String
  }

  class TaskService {
    <<interface>>
    +createTask(String, TaskForm) TodoTask
    +getUserTasks(String) List
    +getUserTasks(String, String, String) List
    +markCompleted(String, String) void
  }

  class DefaultTaskService {
    -TaskRepository taskRepository
    +createTask(String, TaskForm) TodoTask
    +getUserTasks(String) List
    +getUserTasks(String, String, String) List
    +markCompleted(String, String) void
    -normalizeUsername(String) String
    -matchesStatus(TodoTask, String) boolean
    -matchesSearch(TodoTask, String) boolean
  }

  class TaskRepository {
    <<interface>>
    +findByUsername(String) List
    +save(TodoTask) TodoTask
    +findById(String, String) Optional
    +update(TodoTask) void
  }

  class FileTaskRepository {
    -FileStorageManager fileStorageManager
    -Path storageRootPath
    +findByUsername(String) List
    +save(TodoTask) TodoTask
    +findById(String, String) Optional
    +update(TodoTask) void
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
  FileTaskRepository --> TodoTask : persists
  TodoTask --> TaskStatus : has status
```

## Component Diagram

```mermaid
C4Component
  title Component Diagram - Task Dashboard Filtering and Search

  Person(user, "Authenticated User", "Registered user managing personal tasks")
  Container(browser, "Browser", "HTML/CSS", "Submits filter/search requests and renders task dashboard")
  Container_Boundary(app, "Todo Spring Boot Application") {
    Component(security, "Spring Security Filter Chain", "Spring Security", "Authenticates requests and provides principal")
    Component(controller, "TaskController", "Spring MVC Controller", "Binds query parameters, prepares model, returns Thymeleaf view")
    Component(service, "TaskService", "Service Interface", "Defines task workflows")
    Component(defaultService, "DefaultTaskService", "Spring Service", "Normalizes user and applies status/search filters")
    Component(repository, "TaskRepository", "Repository Interface", "Abstracts task persistence")
    Component(fileRepository, "FileTaskRepository", "Repository Implementation", "Reads and writes user-specific task JSON files")
    Component(view, "tasks.html", "Thymeleaf Template", "Displays filter bar, create form, and filtered task list")
  }
  ContainerDb(storage, "Task JSON Files", "Local file system", "storage/tasks/{username}.json")

  Rel(user, browser, "Uses")
  Rel(browser, security, "HTTP request")
  Rel(security, controller, "Authenticated request")
  Rel(controller, service, "Calls with username status search")
  Rel(service, defaultService, "Implemented by")
  Rel(defaultService, repository, "Queries user tasks")
  Rel(repository, fileRepository, "Implemented by")
  Rel(fileRepository, storage, "Read/write JSON")
  Rel(controller, view, "Renders model")
  Rel(view, browser, "HTML response")
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
  participant Storage as User Task JSON
  participant View as tasks.html

  User->>Browser: Select status and enter search
  Browser->>Security: GET /tasks?status=COMPLETED&search=report
  Security->>Security: Validate authenticated session
  alt Authenticated
    Security->>Controller: Forward request with principal
    Controller->>Controller: Bind status and search query params
    Controller->>Service: getUserTasks(username, status, search)
    Service->>Service: Normalize username and search term
    Service->>Repo: findByUsername(normalizedUsername)
    Repo->>Storage: Read storage/tasks/username.json
    Storage-->>Repo: List of user-owned tasks
    Repo-->>Service: Sorted tasks
    Service->>Service: Apply status filter
    Service->>Service: Apply case-insensitive title or description search
    Service-->>Controller: Filtered tasks
    Controller->>View: Add tasks selectedStatus searchTerm taskForm username
    View-->>Browser: Render dashboard with preserved filters
  else Not authenticated
    Security-->>Browser: Redirect to /login
  end
```

## Deployment Diagram

```mermaid
---
title: Deployment View - Todo App Filter Search
config:
  theme: default
---
flowchart TB
  subgraph Client[Client Device]
    Browser[Web Browser]
  end

  subgraph Host[Application Host]
    App[Spring Boot Todo Application on port 8090]
    Static[Static CSS assets]
    Templates[Thymeleaf templates]
    Storage[(Local storage directory)]
  end

  Browser -->|HTTP GET /tasks with query params| App
  App --> Static
  App --> Templates
  App -->|Read/write JSON with file locks| Storage

  subgraph StorageFiles[File Layout]
    Users[(storage/users.json)]
    Tasks[(storage/tasks/username.json)]
  end

  Storage --> Users
  Storage --> Tasks
```

## Data Flow Diagram

```mermaid
---
title: Data Flow - Status Filter and Search
config:
  theme: default
---
flowchart LR
  QP[/Query Params: status and search/] --> Controller[TaskController]
  Auth[/Authentication Principal/] --> Controller
  Controller --> Params[Validated Dashboard Criteria]
  Params --> Service[DefaultTaskService]
  Service --> Normalize[Normalize username and trim search]
  Normalize --> Repo[TaskRepository.findByUsername]
  Repo --> UserFile[(User Task File)]
  UserFile --> AllTasks[All tasks for authenticated user]
  AllTasks --> StatusDecision{Status is ALL?}
  StatusDecision -->|Yes| SearchFilter[Search filter]
  StatusDecision -->|No| StatusFilter[Keep tasks with matching status]
  StatusFilter --> SearchFilter
  SearchFilter --> SearchDecision{Search is blank?}
  SearchDecision -->|Yes| Result[Filtered task list]
  SearchDecision -->|No| TextMatch[Case-insensitive title or description substring match]
  TextMatch --> Result
  Result --> Model[Model attributes]
  Model --> View[tasks.html]
```

## Architecture Diagram

```mermaid
architecture-beta
  group client(internet)[Client]
  service browser(internet)[Browser] in client

  group app(server)[Todo Application Host]
  service spring(server)[Spring Boot MVC App] in app
  service security(server)[Spring Security] in app
  service thymeleaf(server)[Thymeleaf Renderer] in app
  service files(database)[JSON File Storage] in app

  browser:R --> L:security
  security:R --> L:spring
  spring:R --> L:thymeleaf
  spring:B --> T:files
```

## Wireframe and User Flow

```mermaid
---
title: Tasks Dashboard Wireframe
config:
  theme: default
---
flowchart TB
  Page["Tasks Dashboard"] --> Header["Header: Username and Logout"]
  Page --> CreateCard["Create New Task Card"]
  CreateCard --> Fields["Title, Description, Task Date, Planned Finish Date, Add Task"]
  Page --> FilterBar["Filter Bar"]
  FilterBar --> StatusSelect["Status dropdown: All, Open, Completed"]
  FilterBar --> SearchInput["Search input: keyword"]
  FilterBar --> ApplyButton["Apply"]
  FilterBar --> ClearLink["Clear"]
  Page --> List["My Task List"]
  List --> TaskCard["Task card: title, description, dates, status badge, Mark Completed action"]
  List --> Empty["Empty state when no task matches"]
```

## Error Handling and Edge Cases

```mermaid
---
title: Error Handling and Edge Cases
config:
  theme: default
---
flowchart TB
  Request([GET /tasks]) --> AuthCheck{Authenticated?}
  AuthCheck -->|No| Login[Redirect to login]
  AuthCheck -->|Yes| StatusCheck{status valid?}
  StatusCheck -->|No| DefaultAll[Fallback to ALL]
  StatusCheck -->|Yes| Criteria[Use requested status]
  DefaultAll --> SearchCheck
  Criteria --> SearchCheck{search blank?}
  SearchCheck -->|Yes| NoSearch[Skip text filtering]
  SearchCheck -->|No| TextSearch[Trim and lower-case search]
  NoSearch --> ReadTasks[Read user-owned task file]
  TextSearch --> ReadTasks
  ReadTasks --> FileExists{Task file exists?}
  FileExists -->|No| EmptyList[Return empty list]
  FileExists -->|Yes| ApplyFilters[Apply filters]
  ApplyFilters --> Render[Render tasks page]
```
