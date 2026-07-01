# Sample Todo App -- Codebase Analysis
## JIRA Ticket: EPMCDMETST-52763
### Feature: Todo Dashboard -- Task List Filters (All/Open/Completed) & Keyword Search

---

## 1. Application Overview

| Attribute         | Details                                           |
|-------------------|---------------------------------------------------|
| **App Type**      | Server-Rendered MVC Web Application               |
| **Language**      | Java 21                                           |
| **Framework**     | Spring Boot 3.3.2                                 |
| **Frontend**      | Thymeleaf (server-side rendering)                 |
| **Persistence**   | File-based JSON (no database)                     |
| **Auth**          | Spring Security (BCrypt, form login, remember-me) |
| **Build Tool**    | Maven                                             |
| **Test Framework**| TestNG + Mockito                                  |

---

## 2. Tech Stack

- **Java 21** -- Core language
- **Spring Boot 3.3.2** -- Application framework
- **Spring MVC** -- Web layer (controllers)
- **Spring Security** -- Authentication & authorization
- **Spring Validation (Jakarta)** -- DTO validation
- **Thymeleaf** -- HTML templating engine
- **Jackson / jackson-datatype-jsr310** -- JSON serialization
- **TestNG 7.10.2** -- Test framework
- **Mockito 5.12.0** -- Mocking framework

---

## 3. Architecture Pattern

**Clean Layered Architecture (MVC + Repository Pattern)**

```
Browser (HTML/CSS)
      |
      v
[Web Layer]        -- TaskController, AuthController
      |
      v
[Service Layer]    -- TaskService (interface), DefaultTaskService (impl)
      |
      v
[Repository Layer] -- TaskRepository (interface), FileTaskRepository (impl)
      |
      v
[Storage Layer]    -- FileStorageManager (file I/O with locking)
      |
      v
[File System]      -- storage/tasks/<username>.json
```

---

## 4. Domain Entities

### TodoTask
| Field              | Type           | Description                          |
|--------------------|----------------|--------------------------------------|
| id                 | String (UUID)  | Unique task identifier               |
| username           | String         | Owner (normalized to lowercase)      |
| title              | String         | Task title (3-120 chars)             |
| description        | String         | Task description (max 500 chars)     |
| taskDate           | LocalDate      | Task start date                      |
| plannedFinishDate  | LocalDate      | Planned completion date              |
| status             | TaskStatus     | OPEN or COMPLETED                    |
| createdAt          | LocalDateTime  | Creation timestamp                   |

### TaskStatus Enum
- `OPEN` -- Default on task creation
- `COMPLETED` -- Set via markCompleted()

### User
| Field        | Type          | Description               |
|--------------|---------------|---------------------------|
| username     | String        | Unique (case-insensitive) |
| fullName     | String        | Display name              |
| passwordHash | String        | BCrypt hashed             |
| createdAt    | LocalDateTime | Registration timestamp    |

---

## 5. Existing API Endpoints

| Method | Path                      | Description                        |
|--------|---------------------------|------------------------------------|
| GET    | /tasks                    | Show task dashboard                |
| POST   | /tasks                    | Create new task                    |
| POST   | /tasks/{taskId}/complete  | Mark task as completed             |
| GET    | /login                    | Show login page                    |
| POST   | /login                    | Authenticate                       |
| GET    | /register                 | Show registration page             |
| POST   | /register                 | Register new user                  |

---

## 6. Current Gaps (What EPMCDMETST-52763 Addresses)

| Area              | Current State                      | Required State                                    |
|-------------------|------------------------------------|---------------------------------------------------|
| TaskRepository    | findByUsername(String) only        | In-memory filtering after load                    |
| TaskService       | getUserTasks(username) only        | Add getFilteredTasks(username, status, keyword)   |
| TaskController    | No query params on GET /tasks      | Accept filter and keyword query params            |
| tasks.html        | No filter/search UI                | Add filter buttons (All/Open/Completed) + search  |
| Persistence       | Sorted list only                   | In-memory filtering after load                    |

---

## 7. Key Business Rules (from JIRA)

1. Filter values: ALL (default), OPEN, COMPLETED
2. Keyword matches against title AND description (case-insensitive)
3. Filter and search apply only to the authenticated user's tasks
4. Selected filter/search values preserved via URL query parameters
5. Results sorted by taskDate then createdAt (existing behavior retained)
6. Authenticated access required for all task operations

---

## 8. Architecture Approach for Feature

### Design Decision: Service-Layer Filtering
- **Rationale**: The repository uses file-based JSON persistence. Filtering is performed in-memory on the sorted task list -- consistent with current sort-in-memory pattern.
- **Alternative**: Add filter params to repository interface. More cohesive but over-engineered for file-based storage.
- **Chosen**: Extend TaskService with new getFilteredTasks() method; filter in service after repo load.

### URL Parameter Strategy
- GET /tasks?filter=OPEN&keyword=meeting
- Thymeleaf template reads th:value from model to preserve state
- Spring MVC @RequestParam with defaults handles missing params gracefully

---

## 9. Files to Modify

| File                            | Change Type | Description                            |
|---------------------------------|-------------|----------------------------------------|
| TaskService.java                | Modify      | Add getFilteredTasks() method          |
| DefaultTaskService.java         | Modify      | Implement filtering/search logic       |
| TaskController.java             | Modify      | Add @RequestParam filter/keyword       |
| tasks.html                      | Modify      | Add filter buttons + search input      |
| styles.css                      | Modify      | Add filter bar styles                  |
| DefaultTaskServiceTest.java     | Modify      | Add tests for filtered tasks           |
| TaskControllerTest.java         | Modify      | Add tests for filter/keyword params    |
