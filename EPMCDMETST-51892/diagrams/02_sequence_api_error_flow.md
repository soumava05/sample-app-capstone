# Sequence Diagram — API Error Flow (Correlation ID) — EPMCDMETST-4892

```mermaid
sequenceDiagram
    participant C as API Client
    participant F as CorrelationIdFilter
    participant TC as TaskController
    participant SVC as DefaultTaskService
    participant GEH as GlobalExceptionHandler
    participant LOG as SLF4J Logger (MDC)

    C->>F: POST /tasks/{id}/complete\n[X-Correlation-ID: client-uuid]
    activate F
    F->>LOG: MDC.put("correlationId", "client-uuid")
    F->>TC: forward request
    activate TC
    TC->>SVC: markCompleted(username, taskId)
    activate SVC
    SVC-->>TC: throw TaskNotFoundException("Task not found")
    deactivate SVC
    TC-->>GEH: exception propagates
    deactivate TC
    activate GEH
    GEH-\n>>LOG: log.error("[client-uuid] Task not found - taskId={}", ...)
    GEH->>GEH: Read correlationId from MDC
    GEH->>GEH: Build ApiErrorResponse\n{ error: { code: TASK_NOT_FOUND,\n message: "...", details: [],\n correlationId: "client-uuid" } }
    GEH-->>C: HTTP 404 JSON Response
    deactivate GEH
    F->>LOG: MDC.clear()
    deactivate F

    Note over C,LOG: If no X-Correlation-ID header sent,\nFilter generates UUID automatically
```
