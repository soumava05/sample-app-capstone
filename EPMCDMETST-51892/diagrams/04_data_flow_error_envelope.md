# Data Flow Diagram - Error Envelope Construction

```mermaid
flowchart LR
    REQ([Incoming HTTP Request]) --> CIF

    subgraph "Filter Chain"
        CIF{X-Correlation-ID Header Present?}
        CIF -- Yes --> USE[Use Client Correlation ID]
        CIF -- No --> GEN[Generate UUID]
        USE --> MDC[Set MDC correlationId]
        GEN --> MDC
    end

    MDC --> CTRL[Controller]

    subgraph "Exception Sources"
        CTRL |MethodArgumentNotValid| H400[400 Handler]
        CTRL |Task/UserNotFoundException| H404[404 Handler]
        CTRL |StorageException| H500[500 Handler]
        CTRL |Exception| HGEN[Generic 500 Handler]
    end

    subgraph "Envelope Assembly"
        H400 --> ENV[Build ApiErrorResponse]
        H404 --> ENV
        H500 --> ENV
        HGEN --> ENV
        MDCR[Read correlationId from MDC] --> ENV
    end

    ENV --> RESP([JSON Error Response])
    MDC --> MDCR
    RESP --> CLR[MDC.clear]
    CLR --> CLIENT([Return to Client])
```
