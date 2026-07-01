# Component Diagram --- Sample Todo App

```mermaid
graph TB
    subgraph Browser["Browser (Client)"]
        UI[Thymeleaf HTML Pages]
    end

    subgraph SpringBoot["Spring Boot Application (Port 8090)"]
        subgraph Web["Web Layer"]
            AC[AuthController /login /register]
            TC[TaskController /tasks /tasks/id/complete]
        end
        subgraph Security["Security Layer"]
            SC[SecurityConfig Spring Security Filter Chain]
            UDS[UserDetailsService BCrypt Auth]
        end
        subgraph Service["Service Layer"]
            US[UserService DefaultUserService]
            TS[TaskService DefaultTaskService]
        end
        subgraph Repository["Repository Layer"]
            UR[UserRepository FileUserRepository]
            TR[TaskRepository FileTaskRepository]
        end
        subgraph Storage["Storage Layer"]
            FSM[FileStorageManager Jackson JSON]
        end
    end
    subgraph FileSystem["Local Filesystem"]
        UF[users.json]
        TF[tasks/username.json]
    end
    UI --> AC
    UI --> TC
    SC --> ACSC --> TC
    SC --> UDS
    UDS --> US
    AC --> US
    TC --> TS
    US --> UR
    TS --> TR
    UR --> FSM
    TR --> FSM
    FSM --> UFPSLM --> TF	`FM --> TF	`FCm --> AetFCM --> EF	bFSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF	`FSM --> TF
```
