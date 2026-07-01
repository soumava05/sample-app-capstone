# Data Flow Diagram - Sample Todo App

```mermaid
flowchart TD
    A([User Browser]) -->|Registration Form| B[AuthController]
    A -->|Login Credentials| C[Spring Security]
    A -->|Task Form| D[TaskController]
    A -->|Complete Request| E[TaskController - complete]

    B -->|RegistrationForm DTO| F{Bean Validation}
    F -->|Invalid| G([Validation Errors])
    F -->|Valid| H[DefaultUserService]

    H -->|Check uniqueness| I[FileUserRepository]
    I -->|Read| J[(users.json)]
    I -->|User exists| K([Username Taken Error])
    I -->|User not found| L[BCrypt Password Hash]
    L -->|Save user| M[FileUserRepository save]
    M -->|Write| J
    M -->|Success| N([Redirect /login])

    C -->|Credentials| O[UserDetailsService]
    O -->|Lookup| I
    O -->|Authenticated| P([Redirect /tasks])

    D -->|TaskForm DTO| Q{Bean Validation}
    Q -->|Invalid| R([Validation Errors])
    Q -->|Valid| S[DefaultTaskService]

    S -->|Date check| T{plannedFinishDate gte taskDate?}
    T -->|No| U([Date Error])
    T -->|Yes| V[Assign UUID - Status=OPEN]† U€ -->|Save task| W[FileTaskRepository save]
    W -->|Write| X[(tasks/username.json)]
    W -->|Success| Y([Redirect /tasks])

    E -->|username + taskId| Z[DefaultTaskService markCompleted]
    Z -->|Find task| AA[FileTaskRepository findById]
    AA -->|Read| X
    AA -->|Not found| AB([Task Not Found Error])
    AA -->|Found| AC[Set Status=COMPLETED]
    AC -->|Update| AD[FileTaskRepository update]
    AD -->|Write| X
    AD -->|Success| AE([Redirect /tasks])
```
