# Solution Diagrams - EPMCDMETST-5623 Todo Application

## High Level Design

The solution uses a layered Spring Boot MVC architecture for user-specific task management. Current persistence is JSON filesystem storage; production evolution should use PostgreSQL, HTTPS ingress, externalized sessions or JWT, and observability.

```mermaid
C4Context
  title Todo Application System Context
  Person(user, "Registered User", "Creates, views, and completes personal todo tasks")
  Person(admin, "Administrator", "Future role for oversight")
  System(todoApp, "Todo Web Application", "Spring Boot MVC and Thymeleaf application")
  System_Ext(browser, "Web Browser", "HTML form UI over HTTPS")
  SystemDb(fileStore, "Local JSON Storage", "users.json and per-user task JSON files")
  Rel(user, browser, "Uses")
  Rel(browser, todoApp, "Submits forms and receives HTML", "HTTPS")
  Rel(todoApp, fileStore, "Reads and writes", "Jackson JSON")
  Rel(admin, todoApp, "Future admin features", "HTTPS")
```

## Low Level Design

### Component Diagram

```mermaid
C4Component
  title Todo Application Component Diagram
  Person(user, "User", "Authenticated application user")
  Container(browser, "Browser", "HTML/CSS", "Renders Thymeleaf pages")
  ContainerDb(storage, "JSON File Storage", "Filesystem", "users.json and tasks per username")
  Container_Boundary(app, "Spring Boot Todo Application") {
    Component(security, "SecurityFilterChain", "Spring Security", "Protects routes")
    Component(authController, "AuthController", "Spring MVC", "Login and registration")
    Component(taskController, "TaskController", "Spring MVC", "Dashboard and tasks")
    Component(userService, "DefaultUserService", "Service", "Registration and password hashing")
    Component(taskService, "DefaultTaskService", "Service", "Task lifecycle")
    Component(userRepo, "UserRepository", "Interface", "User persistence contract")
    Component(taskRepo, "TaskRepository", "Interface", "Task persistence contract")
    Component(storageManager, "FileStorageManager", "Utility", "JSON read/write")
  }
  Rel(user, browser, "Uses")
  Rel(browser, security, "HTTP")
  Rel(security, authController, "Routes public auth")
  Rel(security, taskController, "Routes authenticated tasks")
  Rel(authController, userService, "Registers")
  Rel(taskController, taskService, "Manages")
  Rel(userService, userRepo, "Uses")
  Rel(taskService, taskRepo, "Uses")
  Rel(userRepo, storageManager, "Persists")
  Rel(taskRepo, storageManager, "Persists")
  Rel(storageManager, storage, "JSON I/O")
```

### Sequence Diagram

```mermaid
sequenceDiagram
  actor User
  participant Browser
  participant Security as Spring Security
  participant Controller as MVC Controller
  participant Service as Business Service
  participant Repo as Repository
  participant Storage as JSON Storage
  User->>Browser: Submit form
  Browser->>Security: HTTP request
  Security->>Controller: Route request
  Controller->>Controller: Validate DTO
  alt Validation fails
    Controller-->>Browser: Render page with errors
  else Valid input
    Controller->>Service: Execute use case
    Service->>Service: Apply business rules
    Service->>Repo: Read/write entity
    Repo->>Storage: JSON file I/O
    Storage-->>Repo: Persisted data
    Repo-->>Service: Result
    Service-->>Controller: Result
    Controller-->>Browser: Redirect or render view
  end
```

### Deployment Diagram

```mermaid
C4Deployment
  title Todo Application Deployment
  Deployment_Node(client, "Client Device", "Desktop or Mobile") {
    Container(browser, "Web Browser", "HTML/CSS")
  }
  Deployment_Node(runtime, "Application Host", "VM or container") {
    Container(app, "todo-app", "Spring Boot Java 21", "Port 8090")
    ContainerDb(files, "storage directory", "Local filesystem", "users.json and tasks/*.json")
  }
  Deployment_Node(target, "Recommended Production Target", "Kubernetes") {
    Container(ingress, "HTTPS Ingress", "TLS routing")
    Container(appPods, "todo-app pods", "Spring Boot replicas")
    ContainerDb(postgres, "PostgreSQL", "Managed database")
  }
  Rel(browser, app, "Current HTTP/HTTPS")
  Rel(app, files, "Current JSON read/write")
  Rel(browser, ingress, "Target HTTPS")
  Rel(ingress, appPods, "Routes")
  Rel(appPods, postgres, "Target JDBC/TLS")
```

### Data Flow Diagram

```mermaid
flowchart LR
  User([Authenticated User]) --> Browser["Browser Forms"]
  Browser --> Security["Spring Security"]
  Security --> Controllers["MVC Controllers"]
  Controllers --> DTOValidation{"DTO validation passed?"}
  DTOValidation -- "No" --> Views["Thymeleaf views with errors"]
  DTOValidation -- "Yes" --> Services["Business Services"]
  Services --> BusinessRules{"Business rules passed?"}
  BusinessRules -- "No" --> Views
  BusinessRules -- "Yes" --> Repositories["Repository Interfaces"]
  Repositories --> Storage["JSON files under storage/"]
  Storage --> Repositories
  Repositories --> Services
  Services --> Controllers
  Controllers --> Views
  Views --> Browser
```

### Architecture Diagram

```mermaid
architecture-beta
  group client(internet)[Client]
  service browser(internet)[Web Browser] in client
  group app(server)[Spring Boot Application]
  service security(server)[Security Filter Chain] in app
  service mvc(server)[MVC Controllers] in app
  service services(server)[Services] in app
  service repos(server)[Repositories] in app
  service templates(server)[Thymeleaf Templates] in app
  group data(database)[Data Layer]
  service json(database)[Local JSON Files] in data
  browser:R --> L:security
  security:R --> L:mvc
  mvc:R --> L:services
  services:R --> L:repos
  repos:R --> L:json
  mvc:B --> T:templates
  templates:L --> R:browser
```

### Wireframe User Flow

```mermaid
flowchart TD
  Start([Open Application]) --> Login["Login Screen"]
  Login -->|New user| Register["Register Screen"]
  Register -->|Success| Login
  Login -->|Authenticated| Dashboard["Todo Dashboard"]
  Dashboard --> Create["Create Task Form"]
  Create -->|Validation error| Dashboard
  Create -->|Task saved| Dashboard
  Dashboard --> Complete["Mark Completed"]
  Complete --> Dashboard
  Dashboard -->|Logout| Login
```

## Alternatives

| Option | Pros | Cons | Recommendation |
|---|---|---|---|
| JSON persistence | Simple and local | Single-node only | Demo only |
| PostgreSQL | Scalable and transactional | Requires DB migration | Production target |
| MVC form login | Simple | Not API-friendly | Keep current UI |
| REST with JWT | Stateless | Larger scope | Use if approved |
