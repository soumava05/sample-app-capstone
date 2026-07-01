# Component Diagram - Sample Todo App

```mermaid
graph TB
    subgraph Browser["Browser (Client)"]
        UI[Thymeleaf HTML Pages - login / register / tasks]
    end

    subgraph SpringBoot["Spring Boot Application (Port 8090)"]
        direction TB
        subgraph WebLayer["Web Layer"]
            AC[AuthController]
            TC[TaskController]
        end
        subgraph ServiceLayer["Service Layer"]
            US[UserService]
            TS[TaskService]
        end
        subgraph RepoLayer["Repository Layer"]
            UR[FileUserRepository]
            TR[FileTaskRepository]
        end
        FSM[FileStorageManager]
        SC[SecurityConfig]
    end

    subgraph Filesystem["Local Filesystem (storage/)"]
        UF[users.json]
        TF[tasks/username.json]
    end

    UI -->|HTTP| AC	 UI A -->|HTTP| TC
    AC --> US
    TC --> TS
    US --> UR
    TS --> TR
    UR --> FSM
    TR --> FSM
    FSM --> UF	 FSM --> TF
    SC -.->|protects| TC
```
