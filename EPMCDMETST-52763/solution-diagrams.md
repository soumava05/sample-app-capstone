# Solution Architecture Diagrams
## JIRA: EPMCDMETST-52763 -- Todo Dashboard: Task List Filters & Keyword Search

---

## 1. High Level Design

### Overview
The feature adds **status-based filtering** (All/Open/Completed) and **keyword search** to the Todo Dashboard.
The architecture follows the existing layered MVC pattern: query parameters flow from the browser through
the Controller -> Service -> (filtered in-memory after Repository load) -> Thymeleaf view.
No new persistence layer changes are required; filtering is performed in-memory on the sorted task list.

```mermaid
graph TD
    Browser["Browser - Thymeleaf + CSS"]
    Controller["TaskController - GET /tasks?filter=OPEN&keyword=meeting"]
    Service["DefaultTaskService - getFilteredTasks(username, filter, keyword)"]
    Repository["FileTaskRepository - findByUsername(username)"]
    Storage["FileStorageManager - storage/tasks/{username}.json"]

    Browser -->|"HTTP GET /tasks?filter=OPEN&keyword=..."| Controller
    Controller -->|"getFilteredTasks()"| Service
    Service -->|"findByUsername()"| Repository
    Repository -->|"readList()"| Storage
    Storage -->|"List of TodoTask"| Repository
    Repository -->|"Sorted List"| Service
    Service -->|"Filtered + Searched List"| Controller
    Controller -->|"Model: tasks, filter, keyword"| Browser
```

---

## 2. Low Level Design

### Detailed Component Interactions

```mermaid
graph LR
    subgraph Web["Web Layer"]
        TC["TaskController\n@GetMapping('/tasks')\n@RequestParam filter=ALL\n@RequestParam keyword=empty"]
    end

    subgraph Service["Service Layer"]
        TSI["TaskService interface\n+getFilteredTasks(username,filter,keyword)"]
        DTS["DefaultTaskService\n1. Normalize username\n2. Load all user tasks\n3. Filter by TaskStatus\n4. Search title+description\n5. Return sorted result"]
    end

    subgraph Repository["Repository Layer"]
        TRI["TaskRepository interface\n+findByUsername(username)"]
        FTR["FileTaskRepository\nReads JSON, sorts by taskDate + createdAt"]
    end

    subgraph Storage["Storage Layer"]
        FSM["FileStorageManager\nFile locking, Jackson read/write JSON"]
    end

    subgraph Domain["Domain"]
        TT["TodoTask\n- id, username\n- title, description\n- taskDate, plannedFinishDate\n- status: TaskStatus\n- createdAt"]
        TS["TaskStatus\nOPEN or COMPLETED"]
    end

    subgraph View["View Layer"]
        HTML["tasks.html Thymeleaf\n- Filter buttons: All/Open/Completed\n- Search input with keyword\n- Preserves ?filter and ?keyword\n- Task list filtered results"]
        CSS["styles.css\n- .filter-bar styles\n- .filter-btn active state\n- .search-input styles"]
    end

    TC -->|"calls"| TSI
    TSI -->|"implemented by"| DTS
    DTS -->|"calls"| TRI
    TRI -->|"implemented by"| FTR
    FTR -->|"uses"| FSM
    FSM -->|"reads"| TT
    TT -->|"has"| TS
    TC -->|"renders"| HTML
    HTML -->|"styled by"| CSS
```

---

## 3. Component Diagram

```mermaid
graph TB
    subgraph App["Spring Boot Application - Todo App"]
        subgraph Cfg["Config"]
            SC["SecurityConfig - BCrypt, FormLogin, RememberMe"]
            JC["JacksonConfig - LocalDate/Time support"]
            ASP["AppStorageProperties - root-path: storage/"]
        end

        subgraph Web2["Web Controllers"]
            TC2["TaskController - /tasks GET/POST, /tasks/{id}/complete"]
            AC["AuthController - /login, /register"]
        end

        subgraph Svc["Services"]
            TSvc["TaskService interface"]
            DTS2["DefaultTaskService impl"]
            USvc["UserService interface"]
            DUS["DefaultUserService impl"]
        end

        subgraph Repo["Repositories"]
            TR["TaskRepository interface"]
            FTR2["FileTaskRepository impl"]
            UR["UserRepository interface"]
            FUR["FileUserRepository impl"]
        end

        subgraph Stor["Storage"]
            FSM2["FileStorageManager - File I/O + Locking"]
        end

        subgraph Dom["Domain"]
            TT2["TodoTask"]
            U["User"]
            TS2["TaskStatus OPEN or COMPLETED"]
        end
    end

    subgraph FS["File System"]
        UF["storage/users.json"]
        TF["storage/tasks/{username}.json"]
    end

    TC2 --> TSvc
    TSvc --> DTS2
    DTS2 --> TR
    TR --> FTR2
    FTR2 --> FSM2
    FSM2 --> TF
    AC --> USvc
    USvc --> DUS
    DUS --> UR
    UR --> FUR
    FUR --> FSM2
    FSM2 --> UF
    SC --> USvc
```

---

## 4. Sequence Diagram -- Filtered Task Search

```mermaid
sequenceDiagram
    actor User as User
    participant Browser as Browser
    participant TC as TaskController
    participant DTS as DefaultTaskService
    participant FTR as FileTaskRepository
    participant FSM as FileStorageManager
    participant FS as File System

    User->>Browser: Click Open filter or type keyword
    Browser->>TC: GET /tasks?filter=OPEN&keyword=meeting
    TC->>TC: Extract username from Authentication
    TC->>DTS: getFilteredTasks(alice, OPEN, meeting)
    DTS->>DTS: normalizeUsername(alice)
    DTS->>FTR: findByUsername(alice)
    FTR->>FSM: readList(storage/tasks/alice.json)
    FSM->>FS: Read alice.json
    FS-->>FSM: JSON byte stream
    FSM-->>FTR: List of TodoTask raw
    FTR->>FTR: Sort by taskDate then createdAt
    FTR-->>DTS: List of TodoTask sorted all tasks
    DTS->>DTS: Filter by status == OPEN
    DTS->>DTS: Search title/desc contains meeting case-insensitive
    DTS-->>TC: List of TodoTask filtered and searched
    TC->>TC: model tasks, filter, keyword
    TC-->>Browser: Render tasks.html with filtered list
    Browser-->>User: Show filtered task list with preserved filter and keyword
```

---

## 5. Data Flow Diagram

```mermaid
flowchart TD
    A([User Browser]) -->|"HTTP GET /tasks?filter=OPEN&keyword=planning"| B[TaskController]
    B -->|"username from Authentication"| C{Validate Input}
    C -->|"filter: ALL/OPEN/COMPLETED, keyword: string"| D["DefaultTaskService getFilteredTasks"]
    D -->|"findByUsername normalized username"| E[FileTaskRepository]
    E -->|"readList path: storage/tasks/alice.json"| F[(JSON File Storage)]
    F -->|"Deserialize List of TodoTask"| E
    E -->|"Sort by taskDate then createdAt"| D
    D -->|"Stream filter status"| G{Apply Status Filter}
    G -->|"filter=ALL"| H[All Tasks]
    G -->|"filter=OPEN"| I[OPEN Tasks only]
    G -->|"filter=COMPLETED"| J[COMPLETED Tasks only]
    H --> K{Apply Keyword Search}
    I --> K
    J --> K
    K -->|"keyword is blank"| L[No search applied]
    K -->|"keyword not blank"| M["title contains keyword OR desc contains keyword case-insensitive"]
    L --> N[Final Task List]
    M --> N
    N -->|"Model: tasks, filter, keyword"| O["Thymeleaf tasks.html"]
    O -->|"HTML Response"| A
```

---

## 6. Deployment Diagram

```mermaid
graph TB
    subgraph Machine["Developer Machine / Server"]
        subgraph JVM["JVM Process - Spring Boot App port 8090"]
            App2["Todo App - spring-boot:run - Java 21"]
            subgraph SpringCtx["Spring Context"]
                SC3["SecurityFilterChain"]
                TC3["TaskController"]
                DTS3["DefaultTaskService"]
                FTR3["FileTaskRepository"]
                FSM3["FileStorageManager"]
            end
        end
        subgraph FileSystem["File System"]
            StorageDir["storage/ - users.json, tasks/alice.json, tasks/bob.json"]
        end
        subgraph Build["Build"]
            Maven2["Maven 3.9+ - Java 21 - pom.xml"]
        end
    end
    subgraph Client["Client"]
        Browser3["Web Browser - HTTP GET /tasks?filter=OPEN&keyword=..."]
    end
    Browser3 -->|"HTTP :8090"| App2
    App2 --> StorageDir
    Maven2 -->|"mvn spring-boot:run"| App2
```
