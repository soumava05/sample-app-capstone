## Implementation Summary
- Added standardized JSON error envelope (`ApiErrorResponse`) and a global exception handling layer (`GlobalExceptionHandler`) that maps validation, not-found, storage, and generic failures to consistent HTTP responses.
- Implemented `CorrelationIdFilter` that reads `X-Correlation-ID` or generates a UUID and stores it in SLF4J MDC under `correlationId`, clearing MDC after request completion.
- Introduced domain-specific exception types (`TaskNotFoundException`, `UserNotFoundException`, `StorageException`, `ValidationException`) and updated service/storage layers to throw them.
- Added MVC test coverage for validation error envelope and correlation-id behavior.
- Updated documentation and test dependencies required for Spring MVC testing.

## Reference Documents Used
- Primary:
  - `EPMCDMETST-51892/implementation_details.md`
- Secondary:
  - `EPMCDMETST-51892/diagrams/01_component_architecture.md`
  - `EPMCDMETST-51892/diagrams/05_deployment_layered_architecture.md`
- Guide:
  - `README.md`
- Reference:
  - Full codebase under `src/main/java` and `src/test/java`

## Files Added
Count: 7
- source: `src/main/java/com/capstone/todo/web/api/ApiErrorResponse.java`
- source: `src/main/java/com/capstone/todo/web/api/GlobalExceptionHandler.java`
- source: `src/main/java/com/capstone/todo/web/filter/CorrelationIdFilter.java`
- source: `src/main/java/com/capstone/todo/exception/TaskNotFoundException.java`
- source: `src/main/java/com/capstone/todo/exception/UserNotFoundException.java`
- source: `src/main/java/com/capstone/todo/exception/StorageException.java`
- source: `src/main/java/com/capstone/todo/exception/ValidationException.java`
- test: `src/test/java/com/capstone/todo/web/api/GlobalExceptionHandlerTest.java`

## Files Modified
Count: 5
- source: `src/main/java/com/capstone/todo/service/impl/DefaultTaskService.java`
- source: `src/main/java/com/capstone/todo/service/impl/DefaultUserService.java`
- source: `src/main/java/com/capstone/todo/storage/FileStorageManager.java`
- config: `pom.xml`
- template/docs: `README.md`

## Compilation Performed
- Slice (model + filter + exceptions + handler):
  - `mvn -q -DskipTests compile` => success
- Slice (tests compilation):
  - `mvn -q -DskipTests test-compile` => success

## Tests Performed
### Unit
- Command: `mvn -q test`
- Result: success

### Integration
- MVC slice:
  - File: `src/test/java/com/capstone/todo/web/api/GlobalExceptionHandlerTest.java`
  - Command: `mvn -q test -Dtest=com.capstone.todo.web.api.GlobalExceptionHandlerTest`
  - Result: success

## Requirement Traceability
- REQ-001 Standard error envelope returned for API failures
  - `src/main/java/com/capstone/todo/web/api/ApiErrorResponse.java`
  - `src/main/java/com/capstone/todo/web/api/GlobalExceptionHandler.java`
- REQ-002 Correlation ID propagation via `X-Correlation-ID` and MDC key `correlationId`
  - `src/main/java/com/capstone/todo/web/filter/CorrelationIdFilter.java`
  - `src/main/java/com/capstone/todo/web/api/GlobalExceptionHandler.java`
- REQ-003 Validation errors return HTTP 400 with details array
  - `src/main/java/com/capstone/todo/web/api/GlobalExceptionHandler.java`
  - `src/test/java/com/capstone/todo/web/api/GlobalExceptionHandlerTest.java`
- REQ-004 Not-found errors return HTTP 404 with standardized envelope
  - `src/main/java/com/capstone/todo/exception/TaskNotFoundException.java`
  - `src/main/java/com/capstone/todo/exception/UserNotFoundException.java`
  - `src/main/java/com/capstone/todo/web/api/GlobalExceptionHandler.java`
  - `src/main/java/com/capstone/todo/service/impl/DefaultTaskService.java`
- REQ-005 Storage failures return HTTP 500 with standardized envelope
  - `src/main/java/com/capstone/todo/exception/StorageException.java`
  - `src/main/java/com/capstone/todo/storage/FileStorageManager.java`
  - `src/main/java/com/capstone/todo/web/api/GlobalExceptionHandler.java`

## Outstanding Notes
- The app is primarily Thymeleaf/MVC; the global handler is `@RestControllerAdvice` and is intended for JSON/API endpoints. HTML form flows (e.g., `/tasks` binding errors) still render templates as before.
- `TaskController#createTask` still catches `IllegalArgumentException` for UI binding; service-layer now throws `ValidationException`/`TaskNotFoundException` in some paths. If the UI should display these messages, we may need a follow-up change (not included in the plan).
- The implementation uses error codes: `VALIDATION_ERROR`, `TASK_NOT_FOUND`, `USER_NOT_FOUND`, `STORAGE_ERROR`, `INTERNAL_ERROR`.
