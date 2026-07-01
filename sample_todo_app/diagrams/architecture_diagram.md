# Architecture Diagram – Sample Todo App

```mermaid
graph TB
    subgraph Presentation["📵 Presentation Layer (Thymeleaf MVC)"]
        direction LR
        L[login.html]
        R[register.html]
        T[tasks.html]
    end

    subgraph Web["🌎 Web Layer (Spring MVC Controllers)"]
        direction LR
        AC["AuthController\n/login \u00b7 /register"]
        TC["TaskController\n/tasks \u00b7 /tasks/{id}/complete"]
    end

    subgraph Security["🔂 Security Layer (Spring Security)"]
        SC["SecurityConfig\nBCrypt \u00b7 FormLogin \u00b7 RememberMe"]
    end

    subgraph Service["⚢  Service Layer (Business Logic)"]
        direction LR
        US["UserService\nDefaultUserService"]
        TS["TaskService\nDefaultTaskService"]
    end

    subgraph Repository["🦢 Repository Layer (Data Access)"]
        direction LR
        UR["UserRepository\nFileUserRepository"]
        TR["TaskRepository\nFileTaskRepository"]
    end

    subgraph Infrastructure["🔇 Infrastructure / Config"]
        direction LR
        FSM["FileStorageManager\nJackson ObjectMapper"]
        ASP["AppStorageProperties\nroot-path=storage/"]
        JC["JacksonConfig\nLocalDate/DateTime"]
    end

    subgraph Filesystem["💾 Filesystem Storage"]
        direction LR
        UJ[("users.json")]
        TJ[("tasks/{username}.json")]
    end

    Presentation -->|renders| Web
    Web --> Security
    Security --> Service
    AC --> US
    TC --> TS
    US --> UR
    TS --> TR
    UR --> FSM`
  TH R --> FSM
    FSM -->|reads/writes| UJ
    FSM -->|reads/writes| TJ
```
