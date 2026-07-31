# Sequence Diagram — Validation Error Flow (HTTP 400)

```mermaid
sequenceDiagram
    participant C as API Client
    participant F as CorrelationIdFilter
    participant TC as TaskController
    participant GEH as GlobalExceptionHandler
    participant LOG as SLF4J Logger (MDC)

    C->>F: POST /tasks [No X-Correlation-ID]
    activate F
    F->>F: Generate UUID: gen-uuid-123
    F->>LOG: MDC.put("correlationId", "gen-uuid-123")
    F->>TC: forward request
    activate TC
    TC->>TC: @Valid TaskForm validation fails
    TC-->>GEH: MethodArgumentNotValidException
    deactivate TC
    activate GEH
    GEH-随流GEH: Extract field errors
    GEH->>GEH: Build ApiErrorResponse with VALIDATION_ERROR
    GEH-->>C: HTTP 400 JSON Response
    deactivate GEH
    F->>LOG: MDC.clear()
    deactivate F
```
