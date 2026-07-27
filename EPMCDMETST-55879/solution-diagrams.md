# EPMCDMETST-55879 - Solution Diagrams

## High Level Design - System Context and Architecture

```mermaid
C4Context
  title EPMCDMETST-55879 Todo Dashboard Filtering - System Context
  Person(user, "Authenticated User", "Manages personal tasks through browser")
  System(todoApp, "Todo App", "Spring Boot MVC and Thymeleaf application providing user-specific task management, search, and filters")
  SystemDb(fileStore, "Local JSON File Storage", "users.json and per-user task JSON files under storage directory")
  System_Ext(browser, "Web Browser", "Renders server-side Thymeleaf pages and submits forms")

  Rel(user, browser, "Uses", "HTTPS or localhost HTTP")
  Rel(browser, todoApp, "GET /tasks with keyword, status, date range query params", "HTTP")
  Rel(browser, todoApp, "POST /tasks and POST /tasks/{taskId}/complete", "HTTP form + CSRF")
  Rel(todoApp, fileStore, "Reads and writes user-scoped JSON task files", "Jackson + file locks")

  UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

## Low Level Design - Component Diagram

```mermaid
C4Component
  title EPMCDMETST-55879 Todo Dashboard Filtering - Component Design
  Person(user, "Authenticated User")
  Container_Boundary(app, "Spring Boot Todo Application") {
    Component(security, "SecurityFilterChain", "Spring Security", "Authenticates users and protects /tasks")
    Component(taskController, "TaskController", "Spring MVC Controller", "Binds query parameters, handles dashboard, create, and complete actions")
    Component(taskFilter, "TaskFilter DTO", "Form/Query DTO", "Carries keyword, status, dateFrom, and dateTo filter criteria")
    Component(taskService, "TaskService", "Service Interface", "Exposes user-scoped task retrieval and mutations")
    Component(defaultTaskService, "DefaultTaskService", "Business Service", "Normalizes username, validates filters, applies search/status/date predicates")
    Component(taskRepository, "TaskRepository", "Repository Interface", "Provides user-scoped task persistence operations")
    Component(fileTaskRepository, "FileTaskRepository", "File Repository", "Loads tasks from storage/tasks/{username}.json sorted by taskDate and createdAt")
    Component(tasksTemplate, "tasks.html", "Thymeleaf Template", "Displays filter form, validation messages, create form, and filtered task list")
  }
  ContainerDb(fileStore, "Local JSON Storage", "File System", "Per-user JSON task files")

  Rel(user, security, "Authenticated session")
  Rel(security, taskController, "Allows authenticated /tasks access")
  Rel(taskController, taskFilter, "Binds GET query params")
  Rel(taskController, taskService, "Calls getUserTasks(username, filter)")
  Rel(taskService, defaultTaskService, "Implemented by")
  Rel(defaultTaskService, taskRepository, "Reads user tasks before filtering")
  Rel(taskRepository, fileTaskRepository, "Implemented by")
  Rel(fileTaskRepository, fileStore, "Read/write JSON")
  Rel(taskController, tasksTemplate, "Returns model and view")
```

## Sequence Diagram - Filtered Dashboard Request

```mermaid
sequenceDiagram
  actor User
  participant Browser
  participant Security as Spring Security
  participant Controller as TaskController
  participant Service as DefaultTaskService
  participant Repo as FileTaskRepository
  participant Storage as Local JSON Storage
  participant View as tasks.html

  User->>Browser: Submit filters on /tasks
  Browser->>Security: GET /tasks?keyword=plan&status=OPEN&dateFrom=2026-06-01&dateTo=2026-06-30
  Security->>Security: Verify authenticated session
  Security->>Controller: Forward request with Authentication
  Controller->>Controller: Bind TaskFilter from query params
  Controller->>Service: getUserTasks(authentication.name, filter)
  Service->>Service: Normalize username and validate date range
  alt Valid filter range
    Service->>Repo: findByUsername(normalizedUsername)
    Repo->>Storage: Read storage/tasks/{username}.json
    Storage-->>Repo: User-scoped task list
    Repo-->>Service: Sorted user tasks
    Service->>Service: Apply keyword AND status AND inclusive date predicates
    Service-->>Controller: Filtered tasks
    Controller->>View: Add tasks, filter values, username, taskForm
    View-->>Browser: Render dashboard with filtered results
  else dateFrom is after dateTo
    Service-->>Controller: IllegalArgumentException with validation message
    Controller->>Repo: findByUsername(normalizedUsername)
    Repo->>Storage: Read storage/tasks/{username}.json
    Storage-->>Repo: User-scoped task list
    Repo-->>Controller: Full user task list for safe redisplay
    Controller->>View: Add error message and preserve filter values
    View-->>Browser: Render dashboard without crash
  end
```

## Deployment Diagram

```mermaid
C4Deployment
  title EPMCDMETST-55879 Todo App Deployment View
  Deployment_Node(client, "User Device", "Desktop or Mobile") {
    Container(browser, "Browser", "HTML/CSS", "Renders Thymeleaf-generated pages")
  }
  Deployment_Node(host, "Application Host", "Local VM, developer machine, or simple server") {
    Container(jvm, "Todo App JVM", "Java 21 + Spring Boot 3.3.2", "Runs MVC, Security, Services, Repositories")
    Deployment_Node(storageDir, "Storage Directory", "Local File System") {
      ContainerDb(usersJson, "users.json", "JSON", "Registered users and password hashes")
      ContainerDb(tasksJson, "tasks/{username}.json", "JSON", "Per-user task lists")
    }
  }

  Rel(browser, jvm, "HTTP GET /tasks with filters and POST task actions", "HTTP")
  Rel(jvm, usersJson, "Read/write users", "Jackson")
  Rel(jvm, tasksJson, "Read/write user-scoped tasks", "Jackson")
```

## Data Flow Diagram

```mermaid
flowchart LR
  User(["Authenticated User"])
  Browser["Browser Filter Form"]
  Controller["TaskController GET /tasks"]
  Auth["Authentication Principal"]
  Filter["TaskFilter keyword status dateFrom dateTo"]
  Service["DefaultTaskService"]
  Validation{"Date range valid?"}
  Repo["TaskRepository.findByUsername"]
  Store[("storage/tasks/{username}.json")]
  Predicates["Apply AND predicates: keyword, status, inclusive taskDate range"]
  Model["MVC Model: tasks, filter, errors, username, taskForm"]
  View["tasks.html"]
  Error["User-visible validation message"]

  User --> Browser
  Browser --> Controller
  Auth --> Controller
  Controller --> Filter
  Controller --> Service
  Service --> Validation
  Validation -- "Yes" --> Repo
  Repo --> Store
  Store --> Repo
  Repo --> Predicates
  Filter --> Predicates
  Predicates --> Model
  Validation -- "No" --> Error
  Error --> Model
  Model --> View
  View --> Browser
```

## Architecture Diagram

```mermaid
architecture-beta
  group client(internet)[Client Layer]
  service browser(internet)[Web Browser] in client

  group app(server)[Spring Boot Todo Application]
  service security(server)[Spring Security] in app
  service mvc(server)[Spring MVC Controllers] in app
  service serviceLayer(server)[Task Service Filtering Logic] in app
  service repository(server)[File Repository] in app
  service thymeleaf(server)[Thymeleaf Views] in app

  group data(disk)[Local File Persistence]
  service users(disk)[users json] in data
  service tasks(disk)[per user task json] in data

  browser:R --> L:security
  security:R --> L:mvc
  mvc:R --> L:serviceLayer
  serviceLayer:R --> L:repository
  repository:R --> L:tasks
  repository:B --> T:users
  mvc:B --> T:thymeleaf
  thymeleaf:L --> R:browser
```

## Domain Model Diagram

```mermaid
classDiagram
  direction LR
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
  class TaskFilter {
    +String keyword
    +TaskStatus status
    +LocalDate dateFrom
    +LocalDate dateTo
  }

  User "1" --> "0..*" TodoTask : owns by username
  TodoTask --> TaskStatus : has
  TaskFilter ..> TodoTask : filters
```

## Wireframe and User Flow

```mermaid
flowchart TB
  Login["Login Page"] --> Tasks["Todo Dashboard"]
  Tasks --> FilterForm["Search and Filter Panel\nKeyword input\nStatus select: ALL OPEN COMPLETED\nTask Date From\nTask Date To\nApply Filters button\nClear Filters link"]
  FilterForm --> Results{"Valid date range?"}
  Results -- "Yes" --> FilteredList["Filtered My Task List\nOnly matching user-owned tasks"]
  Results -- "No" --> ErrorBanner["Inline validation message\nDate From cannot be after Date To"]
  ErrorBanner --> FilterForm
  Tasks --> CreateForm["Create New Task Form"]
  CreateForm --> Tasks
  FilteredList --> CompleteAction["Mark Completed"]
  CompleteAction --> Tasks
```

## API and Contract Design

```mermaid
flowchart TB
  GETTasks["GET /tasks"] --> Params["Query Parameters\nkeyword optional string\nstatus optional ALL OPEN COMPLETED\ndateFrom optional ISO date\ndateTo optional ISO date"]
  Params --> Response["200 OK tasks view with filtered model"]
  Params --> Invalid["Invalid date range"]
  Invalid --> ValidationResponse["200 OK tasks view with filterError"]
  POSTCreate["POST /tasks"] --> ExistingCreate["Existing create-task flow unchanged"]
  POSTComplete["POST /tasks/{taskId}/complete"] --> ExistingComplete["Existing mark-completed flow unchanged"]
```

## Rollout Plan

```mermaid
gantt
  title EPMCDMETST-55879 Rollout Plan
  dateFormat YYYY-MM-DD
  section Design
    Architecture and diagrams :done, design, 2026-07-27, 1d
  section Implementation
    Add TaskFilter DTO and service API :impl1, after design, 1d
    Update controller and Thymeleaf template :impl2, after impl1, 1d
    Add service and controller tests :impl3, after impl2, 1d
  section Validation
    Run mvn test :test, after impl3, 1d
    Review and merge :review, after test, 1d
```

## Mermaid Validation Notes

All diagrams use Mermaid-supported diagram types: C4Context, C4Component, sequenceDiagram, C4Deployment, flowchart, architecture-beta, classDiagram, and gantt. Node labels containing special characters are quoted where needed. C4 diagrams include titles. Flowcharts avoid reserved lowercase `end` labels.
