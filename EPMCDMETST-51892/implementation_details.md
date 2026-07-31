## Planning Summary
Implement standardized API error responses for the Sample Todo App by introducing a **correlation-id filter** and a **global exception handling layer** that returns a consistent JSON error envelope across common failure modes (validation errors, not-found errors, storage errors, and generic server errors). The design targets a **layered monolith** Spring Boot app (Java 21, Spring Boot 3.3.x) running on **port 8090**, using SLF4J MDC for correlation ID propagation.

Approval requested: confirm this plan matches the intended scope for `EPMCDMETST-51892` before I write it to `EPMCDMETST-51892/implementation_details.md` and commit.

## Inputs Used
From branch `EPMCDMETST-51892` (markdown files only, per instructions):
- `EPMCDMETST-51892/sample_todo_app_details.md` — scope statement + stack + list of required new components (CorrelationIdFilter, GlobalExceptionHandler, ApiErrorResponse, exception types).
- `EPMCDMETST-51892/diagrams/01_component_architecture.md` — component/layer diagram (Client/Web/Service/Repository/Storage) and where new pieces sit.
- `EPMCDMETST-51892/diagrams/02_sequence_api_error_flow.md` — sequence for API error with correlation ID (header → MDC → exception → handler → envelope → response → MDC clear).
- `EPMCDMETST-51892/diagrams/03_sequence_validation_error_flow.md` — sequence for validation error (no header → generate UUID → MethodArgumentNotValidException → envelope).
- `EPMCDMETST-51892/diagrams/04_data_flow_error_envelope.md` — data-flow for envelope assembly and exception source mapping (400/404/500).
- `EPMCDMETST-51892/diagrams/05_deployment_layered_architecture.md` — deployment/layered architecture with servlet filter chain and Spring Security filter.

## Priority-Ordered Task List

### Prerequisites
1. **Inventory current error/exception handling behavior**
   - **Purpose:** Establish baseline responses and existing exception handling to ensure the new envelope replaces/extends it cleanly.
   - **Depends on:** None
   - **Verification intent:** Capture current behavior for (a) task not found, (b) user not found, (c) validation failure, (d) storage failure, (e) generic exception; note current HTTP codes and body formats.

2. **Confirm correlation ID contract (header name + response behavior)**
   - **Purpose:** Align implementation with the diagrams that reference `X-Correlation-ID` and MDC key `correlationId`.
   - **Depends on:** Task 1
   - **Verification intent:** Document expected behavior: accept client header if present, otherwise generate UUID; correlationId appears in error envelope; MDC cleared after request.

### Feature work
3. **Define the standard error envelope model: `ApiErrorResponse`**
   - **Purpose:** Implement the JSON structure shown in the sequence/data-flow diagrams (outer envelope with `error` containing `code`, `message`, `details`, `correlationId`).
   - **Depends on:** Task 2
   - **Verification intent:** Serialization shape matches design (including `details: []`); correlationId field populated from MDC.

4. **Implement `CorrelationIdFilter` in the servlet filter chain**
   - **Purpose:** Ensure every request has a correlationId in MDC, using incoming `X-Correlation-ID` or generating a UUID, and clearing MDC at end (per sequences).
   - **Depends on:** Task 2
   - **Verification intent:** 
     - With header present, MDC uses client-provided value.
     - Without header, UUID is generated and used.
     - MDC cleared after completion to avoid cross-request leakage.

5. **Create/introduce the exception types called out in the design**
   - **Purpose:** Provide explicit exceptions used for mapping to HTTP codes: `TaskNotFoundException` (404), `UserNotFoundException` (404), `StorageException` (500), `ValidationException` (400).
   - **Depends on:** Task 3
   - **Verification intent:** Exceptions exist and are thrown from the layers indicated in component diagram (service/repository boundaries) without changing business logic beyond the documented contract.

6. **Implement `GlobalExceptionHandler` (`@RestControllerAdvice`)**
   - **Purpose:** Centralize exception-to-response mapping and envelope assembly per the data-flow diagram (400/404/500 + generic 500).
   - **Depends on:** Tasks 3, 5
   - **Verification intent:** 
     - `MethodArgumentNotValidException` mapped to HTTP 400 with extracted field errors in `details`.
     - Not-found exceptions mapped to HTTP 404.
     - StorageException mapped to HTTP 500.
     - Fallback handler maps unknown exceptions to HTTP 500.
     - Every response includes `correlationId` from MDC.

7. **Wire controller/service/repository flows to throw the designed exceptions**
   - **Purpose:** Ensure `DefaultTaskService`, `DefaultUserService`, `FileTaskRepository`, and `FileUserRepository` propagate the correct exception types shown in the component diagram.
   - **Depends on:** Task 5
   - **Verification intent:** Trigger scenarios lead to the intended exception class, then are handled by `GlobalExceptionHandler` to produce correct HTTP code + envelope.

8. **Align logging with correlation ID usage (SLF4J MDC)**
   - **Purpose:** The sequence diagram shows `log.error("[client-uuid] ...")` behavior and MDC usage.
   - **Depends on:** Task 4
   - **Verification intent:** Logs include correlationId consistently for handled errors; no correlationId persists across requests (paired with MDC clear verification).

### Hardening/Cleanup
9. **Add/adjust automated tests around the new error envelope and correlation ID**
   - **Purpose:** Prevent regressions in envelope shape, status codes, and correlation id behavior.
   - **Depends on:** Tasks 3–7
   - **Verification intent:** Tests cover:
     - Validation error path (no header) returns 400 + generated correlationId + populated details.
     - API error path (header provided) returns 404/500 as appropriate + same correlationId echoed in envelope.
     - Storage error returns 500 + envelope.

10. **Documentation update for API clients**
   - **Purpose:** Ensure consumers know the `X-Correlation-ID` header behavior and standard error format.
   - **Depends on:** Tasks 3–7
   - **Verification intent:** Markdown doc updates reflect the final envelope fields and correlation-id behavior described in the diagrams.

## Dependency Ordering
- Must do first: **Task 1 → Task 2**
- Core build order:
  - **Task 3 (ApiErrorResponse)** and **Task 4 (CorrelationIdFilter)** can proceed in parallel after Task 2.
  - **Task 5 (Exception types)** depends on Task 3.
  - **Task 6 (GlobalExceptionHandler)** depends on Tasks 3 and 5.
  - **Task 7 (Throw/propgate exceptions in layers)** depends on Task 5 and should be validated with Task 6 in place.
  - **Task 8 (Logging alignment)** depends on Task 4.
- Hardening:
  - **Task 9 (Tests)** depends on Tasks 3–7 (and benefits from Task 4 for correlation cases).
  - **Task 10 (Docs)** depends on Tasks 3–7.

Parallelizable after prerequisites:
- (3) and (4) in parallel.
- (8) can be done alongside (6)/(7) once (4) exists.

## Blocked Tasks
- None explicitly indicated in the provided design markdown/diagrams (no design-review “blocking” notes found in the inputs).

## Verification Notes
Verification should follow the designed flows:

- **API error with provided correlation ID** (from `02_sequence_api_error_flow.md`)
  - Send request with `X-Correlation-ID: client-uuid`
  - Trigger `TaskNotFoundException`
  - Expect HTTP 404 JSON envelope with `correlationId = client-uuid`
  - Confirm logs include the correlationId context and MDC is cleared after request

- **Validation error with no correlation ID header** (from `03_sequence_validation_error_flow.md`)
  - Send invalid request (trigger `MethodArgumentNotValidException`)
  - Expect HTTP 400 JSON envelope with generated UUID correlationId
  - Ensure `details` contains extracted field errors

- **Storage + generic 500**
  - Trigger `StorageException` from repository/storage boundary and ensure HTTP 500 envelope
  - Trigger unexpected exception and ensure fallback 500 envelope still includes correlationId

## Risks and Execution Notes
- **Risk: Existing error handling may conflict** (e.g., existing `@ControllerAdvice` or Spring Boot default error response). Mitigation: Task 1 baseline inventory and then confirm ordering/override behavior.
- **Risk: MDC leakage across threads/requests** if filter doesn’t clear MDC in all exit paths. Mitigation: enforce `finally`-style cleanup behavior and verify with repeated requests (Task 4 + verification).
- **Risk: Envelope schema drift** if multiple handlers build responses differently. Mitigation: centralize envelope assembly in `GlobalExceptionHandler` and test shape (Task 6 + Task 9).

## Assumptions and Open Questions
Open questions (not answered explicitly in the markdown/diagrams):
1. Should successful (non-error) responses also echo `X-Correlation-ID` as a response header, or is correlationId only required inside **error** envelopes?
2. What are the canonical **error codes** enum/strings beyond the example `TASK_NOT_FOUND` and `VALIDATION_ERROR` (e.g., `USER_NOT_FOUND`, `STORAGE_ERROR`, `INTERNAL_ERROR`)—should these be standardized and documented?
3. What exact structure is expected for `details` entries for validation errors (e.g., `{field, message}` vs `{field, rejectedValue, message}`)?

## Requirement Traceability
No separate requirements document was provided in the repo beyond the design markdown; traceability below maps tasks to explicit design statements/diagrams:

- **Standardize API Error Responses with Custom Envelope + Correlation ID** (`sample_todo_app_details.md`)
  - Tasks: 2–7, 9–10
- **Correlation ID filter using `X-Correlation-ID` and MDC; generate UUID if missing; clear MDC** (`02_sequence_api_error_flow.md`, `03_sequence_validation_error_flow.md`, `04_data_flow_error_envelope.md`, `05_deployment_layered_architecture.md`)
  - Tasks: 2, 4, 8, 9
- **Global exception handling (`GlobalExceptionHandler`) building `ApiErrorResponse`** (`01_component_architecture.md`, `04_data_flow_error_envelope.md`)
  - Tasks: 3, 6, 9
- **Exception types required: `TaskNotFoundException`, `UserNotFoundException`, `StorageException`, `ValidationException`** (`sample_todo_app_details.md`, `01_component_architecture.md`, `04_data_flow_error_envelope.md`)
  - Tasks: 5, 7, 9
