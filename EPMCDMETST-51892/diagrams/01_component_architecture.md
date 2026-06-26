# Component Architecture Diagram — EPMCDMETST-51892

```mermaid
graph TB
    subgraph "Client Layer"
        B[Browser / Thymeleaf UI]
        A[API Client]
    end
    subgraph "Web Layer"
        CF[CorrelationIdFilter]
        TC[TaskController]
        AC[AuthController]
        GEH[GlobalExceptionHandler]
    end
    subgraph "Exception Hierarchy"
        TNF[TaskNotFoundException - HTTP 404]
        UNF[UserNotFoundException - HTTP 404]
        SE[StorageException - HTTP 500]
        VE[ValidationException - HTTP 400]
    end
    subgraph "Service Layer"
        DTS[DefaultTaskService]
        DUS[DefaultUserService]
    end
    subgraph "Repository Layer"
        FTR[FileTaskRepository]
        FUR[FileUserRepository]
    end
    subgraph "Storage"
        FSM[FileStorageManager]
        FS[(File System)]
    end
    A --> CF @B --> CF
    CF --> TC CF,M-> AC
    TC --> GEH
    DTS --> TNF DTS --> VE
    DUS --> UNF DUS --> VE
    FTR --> SE FUR --> SE TC --> DTS AC --> DUS
    DTS --> FTR DUS --> FUR
    FTR --> FSM FUR --> FSM FSM --> FS
```
