# Solution Diagrams — EPMCDMETST-55591
## Todo Dashboard: Add Task Filtering (Status/Date) and Sorting

---

## 1. Architecture Diagram (C4 Context)

```mermaid
C4Context
  title System Context — Todo App (EPMCDMETST-55591)

  Person(user, "Authenticated User", "Registered user managing personal tasks")

  System_Boundary(b0, "Todo Application") {
    System(todoApp, "Todo Web App", "Spring Boot + Thymeleaf MVC application providing task management with filtering and sorting")
  }

  System_Ext(browser, "Web Browser", "User's browser rendering server-side HTML pages")
  System_Ext(fs, "Local Filesystem", "JSON file-based persistence for users and tasks")

  Rel(user, browser, "Interacts via", "HTTPS")
  Rel(browser, todoApp, "HTTP GET/POST", "Form submissions & page loads")
  Rel(todoApp, fs, "Read/Write JSON", "tasks/<username>.json, users.json")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

---

## 2. Component Diagram

```mermaid
C4Component
  title Component Diagram — Task Filtering and Sorting Feature

  Person(user, "Authenticated User")
  ContainerDb(fs, "Filesystem", "JSON Files", "storage/tasks/<username>.json")

  Container_Boundary(app, "Todo Web Application") {
    Component(taskCtrl, "TaskController", "Spring MVC Controller", "Handles GET /tasks with filter/sort params")
    Component(filterForm, "TaskFilterForm", "DTO", "Holds status, from, to, sortBy, sortDir params")
    Component(taskSvc, "TaskService", "Interface", "Contract for getUserTasks(username, filterForm)")
    Component(defaultTaskSvc, "DefaultTaskService", "Service Impl", "Applies status filter, date range filter, and dynamic sorting")
    Component(taskRepo, "TaskRepository", "Interface", "Provides findByUsername(username)")
    Component(fileTaskRepo, "FileTaskRepository", "Repository Impl", "Reads/writes tasks from JSON files")
    Component(storageManager, "FileStorageManager", "Storage Utility", "JSON serialization with file locking")
  }

  Rel(user, taskCtrl, "HTTP GET /tasks?status=OPEN'sortBy=taskDate", "Browser")
  Rel(taskCtrl, filterForm, "Binds query params")
  Rel(taskCtrl, taskSvc, "getUserTasks(username, filterForm)")
  Rel(taskSvc, defaultTaskSvc, "implements")
  Rel(defaultTaskSvc, taskRepo, "findByUsername(username)")
  Rel(taskRepo, fileTaskRepo, "implements")
  Rel(fileTaskRepo, storageManager, "readList / writeList")
  Rel(storageManager, fs, "File I/O")
```

---

## 3. Sequence Diagram — Filter and Sort Flow

```mermaid
sequenceDiagram
  actor User as Authenticated User
  participant Browser
  participant TaskController
  participant TaskFilterForm
  participant DefaultTaskService
  participant FileTaskRepository
  participant FileStorageManager
  participant Filesystem

  User->>Browser: Opens /tasks with optional filter params
  Browser->>+TaskController: GET /tasks?status=OPEN&from=2025-06-01&sortBy=taskDate&sortDir=asc

  TaskController->>TaskFilterForm: Bind query params
  TaskFilterForm-->>TaskController: Populated filterForm

  TaskController->>+DefaultTaskService: getUserTasks(username, filterForm)

  DefaultTaskService->>+FileTaskRepository: findByUsername(username)
  FileTaskRepository->>+FileStorageManager: readList(tasks/alice.json)
  FileStorageManager->>+Filesystem: Read JSON file
  Filesystem-->>-FileStorageManager: Raw JSON bytes
  FileStorageManager-->>-FileTaskRepository: List of TodoTask
  FileTaskRepository-->>-DefaultTaskService: Base sorted task list

  Note over DefaultTaskService: Apply filters in-memory
  DefaultTaskService->>DefaultTaskService: filter by status
  DefaultTaskService->>DefaultTaskService: filter by taskDate range
  DefaultTaskService->>DefaultTaskService: apply dynamic sort

  DefaultTaskService-->>-TaskController: Filtered and sorted task list
  TaskController-->>-Browser: Render tasks.html with filter form
  Browser-->>User: Page with preserved filters and sorted list
```

---

## 4. Data Flow Diagram

```mermaid
flowchart TD
  subgraph Browser["Browser (Client)"]
    direction TB
    FilterForm["Filter Form\n(status, from, to, sortBy, sortDir)"]
    TaskList["Rendered Task List\n(filtered + sorted)"]
  end

  subgraph Controller["TaskController"]
    BindParams["Bind Query Params\nto TaskFilterForm"]
    AddModel["Add to Model"]
    RenderView["Return 'tasks' view"]
  end

  subgraph Service["DefaultTaskService"]
    FetchAll["Fetch ALL user tasks"]
    FilterStatus["Apply Status Filter"]
    FilterFrom["Apply Date From Filter"]
    FilterTo["Apply Date To Filter"]
    Sort["Apply Dynamic Sort"]
  end

  subgraph Storage["Filesystem"]
    JsonFile[("tasks/username.json")]
  end

  FilterForm -->|GET /tasks?params| BindParams
  BindParams --> FetchAll
  FetchAll --> JsonFile
  JsonFile --> FilterStatus
  FilterStatus --> FilterFrom
  FilterFrom --> FilterTo
  FilterTo --> Sort
  Sort --> AddModel
  AddModel --> RenderView
  RenderView --> TaskList
  TaskList -->|Preserved filter values| FilterForm
```

---

## 5. Class Diagram

```mermaid
classDiagram
  direction LR

  class TaskController {
    <<Controller>>
    -TaskService taskService
    +taskDashboard(Authentication, TaskFilterForm, Model) String
    +createTask(Authentication, TaskForm, BindingResult, Model) String
    @markTaskCompleted(Authentication, taskId) String
  }

  class TaskFilterForm {
    <<DTO>>
    -TaskStatus status
    -LocalDate from
    -LocalDate to
    -String sortBy
    -String sortDir
  }

  class TaskService {
    <<interface>>
    +createTask(username, TaskForm) TodoTask
    +getUserTasks(username, TaskFilterForm) List~TodoTask~
    @markCompleted(username, taskId) void
  }

  class DefaultTaskService {
    <<Service>>
    -TaskRepository taskRepository
    +getUserTasks(username, TaskFilterForm) List~TodoTask~
    -applyFilters(tasks, filterForm) List~TodoTask~
    -applySort(tasks, sortBy, sortDir) List~TodoTask~
  }

  class TaskStatus {
    <<enumeration>>
    OPEN
    COMPLETED
  }

  TaskController --> TaskService : uses
  TaskController ..> TaskFilterForm : binds
  TaskService <|.. DefaultTaskService : implements
  DefaultTaskService ..> TaskFilterForm : uses
  TaskFilterForm ..> TaskStatus : references
```

---

## 6. Deployment Diagram

```mermaid
C4Deployment
  title Deployment Diagram — Todo App (Single-Server)

  Deployment_Node(userDevice, "User Device", "Desktop / Mobile Browser") {
    Container(browser, "Web Browser", "Chrome / Firefox", "Renders Thymeleaf HTML pages")
  }

  Deployment_Node(server, "Application Server", "JVM / Java 21") {
    Deployment_Node(springBoot, "Spring Boot 3.3.x Process") {
      Container(app, "Todo Web Application", "JAR", "Spring MVC + Security + Thymeleaf")
    }
    Deployment_Node(localFs, "Local Filesystem") {
      ContainerDb(tasksDir, "storage/tasks/", "JSON Files", "Per-user task files")
      ContainerDb(usersFile, "storage/users.json", "JSON File", "Registered users")
    }
  }

  Rel(browser, app, "HTTP GET/POST /tasks", "TCP 8080")
  Rel(app, tasksDir, "Read/Write", "File I/O")
  Rel(app, usersFile, "Read/Write", "File I/O")
```
