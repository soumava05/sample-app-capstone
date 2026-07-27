## Planning Summary
Implement server-side task list filtering on the existing `/tasks` page of the Todo application. The design introduces an optional filter DTO (`TaskFilterForm`) carrying `status`, `fromDate`, and `toDate`, extends the service/repository contracts to support filtered retrieval, adds date-range validation (`toDate` must not be before `fromDate`) with an in-page error rendered in `tasks.html`, and updates the UI to include a filter bar with Apply/Clear behavior.

The plan below is derived strictly from the design artifacts in `generated_docs/EPMCDMETST-55863/sample_todo_app_details.md` and `generated_docs/EPMCDMETST-55863/solution-diagrams.md`.

## Inputs Used
- GitHub repository: https://github.com/soumava05/sample-app-capstone (branch: `EPMCDMETST-55863`)
- Design/requirements document: `generated_docs/EPMCDMETST-55863/sample_todo_app_details.md`
- Diagrams/spec document: `generated_docs/EPMCDMETST-55863/solution-diagrams.md`
- Confluence design page: https://epam-team-js1kicba.atlassian.net/wiki/spaces/EliteA/pages/25591826/Design+Diagram+-+EPMCDMETST-55863

## Priority-Ordered Task List
### Prerequisites
1. **Confirm filter contract (DTO shape + query param names)**
   - **Purpose**: Align on `TaskFilterForm` fields (`status`, `fromDate`, `toDate`) and their mapping from `/tasks` query parameters.
   - **Dependencies**: None.
   - **Verification intent**: DTO fields match design diagrams and are bindable from GET query string.

2. **Add `TaskFilterForm` DTO (web-layer binding model)**
   - **Purpose**: Provide a typed model to bind optional `status`, `fromDate`, `toDate` from GET query parameters.
   - **Dependencies**: Task 1.
   - **Verification intent**: Unit test DTO basic construction/validation annotations (if used) and successful Spring binding (covered later by MVC test).

### Feature Work
3. **Extend `TaskService` contract for filtered retrieval**
   - **Purpose**: Add `getUserTasks(String username, TaskFilterForm filter)` while preserving existing no-filter behavior.
   - **Dependencies**: Task 2.
   - **Verification intent**: Compilation + service unit tests can call the new overload.

4. **Implement date-range validation in service layer**
   - **Purpose**: Enforce FR-6/AC5: reject `toDate < fromDate` before repository read and return a validation outcome consumable by controller.
   - **Dependencies**: Task 3.
   - **Verification intent**: Service unit tests cover invalid range handling and ensure entered filter values are preserved for rendering.

5. **Extend `TaskRepository` contract to support filtered user-scoped reads**
   - **Purpose**: Implement preferred design: repository method that accepts username + filter (e.g., `findByUsername(username, filter)` or equivalent), keeping persistence abstraction ready for future optimization.
   - **Dependencies**: Task 2.
   - **Verification intent**: Compilation + repository unit tests can call filtered method.

6. **Implement filtering logic in `FileTaskRepository` (status + inclusive date predicates + sort)**
   - **Purpose**: Apply FR-2/FR-3/FR-4: filter tasks by optional status and inclusive `taskDate` range, then sort by `taskDate` then `createdAt` (per existing behavior).
   - **Dependencies**: Task 5.
   - **Verification intent**: Repository tests cover: only status, only fromDate, only toDate, both dates inclusive, combined filters, no filters, missing file returns empty.

7. **Update `DefaultTaskService` to orchestrate filtered retrieval and user normalization**
   - **Purpose**: Pass normalized authenticated username and filter to repository; keep existing path when filter is empty; integrate validation outcome for invalid ranges.
   - **Dependencies**: Tasks 4, 6.
   - **Verification intent**: Service tests prove username scoping is preserved and default behavior returns all tasks when filter not supplied.

8. **Update `TaskController` GET `/tasks` to accept filters via query parameters**
   - **Purpose**: Bind query params into `TaskFilterForm`, fetch authenticated principal name, call the new service method, and populate model for `tasks.html`.
   - **Dependencies**: Task 7.
   - **Verification intent**: MVC tests verify:
     - Filtered results are rendered for status/date params.
     - No params returns all tasks.
     - Invalid date range returns `tasks.html` with validation error.
     - Username is always derived from authentication, not request.

9. **Add filter bar UI to `tasks.html` (Apply + Clear; preserve selected values)**
   - **Purpose**: Implement FR-1/FR-8: provide status dropdown (All/OPEN/COMPLETED), from/to date inputs, Apply button (GET) and Clear link to `/tasks`.
   - **Dependencies**: Task 8.
   - **Verification intent**: Manual UI check or MVC HTML assertions confirm filter inputs render and selected values are echoed back.

10. **Add styling updates in `static/css/styles.css` for filter bar**
   - **Purpose**: Ensure filter bar is usable and visually integrated, including responsive layout (per design “Wireframe” and maintainability notes).
   - **Dependencies**: Task 9.
   - **Verification intent**: Manual UI inspection in browser.

### Hardening / Cleanup
11. **Add/extend tests for acceptance criteria coverage (controller/service/repository)**
   - **Purpose**: Satisfy NFR “Testability”: cover status, date range, combined filters, default behavior, invalid range, and ownership isolation.
   - **Dependencies**: Tasks 6–10.
   - **Verification intent**: Automated test suite passes; tests map to AC1–AC6.

12. **Update/verify API contract notes in docs (optional, if project conventions require)**
   - **Purpose**: Ensure endpoint contract (`GET /tasks` optional `status/fromDate/toDate`) remains discoverable.
   - **Dependencies**: Tasks 8–9.
   - **Verification intent**: Documentation reflects final parameter names and behavior; no functional code changes.

## Dependency Ordering
- Task 1 → Task 2
- Task 2 → Tasks 3 and 5 (parallelizable)
- Task 3 → Task 4
- Task 5 → Task 6
- Tasks 4 and 6 → Task 7
- Task 7 → Task 8
- Task 8 → Task 9
- Task 9 → Task 10
- Tasks 6–10 → Task 11
- Task 8 → Task 12

Parallel work after prerequisites:
- **Tasks 3 and 5** can be done in parallel once `TaskFilterForm` exists.

## Blocked Tasks
None explicitly blocked by the design documents.

## Verification Notes
- **Functional verification**
  - `GET /tasks` with no query params shows all tasks for authenticated user.
  - `GET /tasks?status=OPEN` shows only OPEN tasks.
  - `GET /tasks?fromDate=YYYY-MM-DD` includes tasks with `taskDate >= fromDate`.
  - `GET /tasks?toDate=YYYY-MM-DD` includes tasks with `taskDate <= toDate`.
  - `GET /tasks?status=COMPLETED&fromDate=...&toDate=...` applies all predicates (AND semantics).
  - `GET /tasks?fromDate=2026-06-30&toDate=2026-06-01` renders `tasks.html` with validation error and preserves entered values.
  - Clear filters by navigating to `/tasks` without query parameters (via Clear link).

- **Security verification**
  - Ensure controller uses `Authentication.getName()` (or equivalent) for username scoping, not a request parameter.

- **Persistence compatibility verification**
  - Existing JSON task files remain readable; no schema changes introduced.

## Risks and Execution Notes
- **Binding/format risk**: `LocalDate` query parameter parsing requires correct date format (typically ISO `YYYY-MM-DD`). If the application deviates from default conversion rules, binding may fail and needs explicit configuration; the design does not specify custom formats.
- **Validation UX risk**: The design states the validation error must be shown on `tasks.html`. Ensure the controller/service can return an error state without redirecting, otherwise the message will be lost.
- **Repository abstraction drift**: If filtering is implemented in service instead of repository, it diverges from the “Preferred: repository method” design note; keep implementation aligned to avoid future refactor.

## Assumptions and Open Questions
- **Open question**: Exact `TaskRepository` filtered method signature name (`findByUsernameAndFilter` vs `findByUsername(username, filter)`) is not fixed in the design; choose one and keep it consistent across service/controller.
- **Open question**: Where the date-range validation should live (controller vs service) is described as “service/controller boundary” and “validate before repository read”; the plan implements it in service to match diagrams, but confirmation may be needed.
- **Open question**: How unknown/invalid `status` values should be handled (binding error vs graceful fallback) is mentioned as an edge case but not specified; tests should reflect chosen behavior once clarified.

## Requirement Traceability
| Requirement / AC | Planned Tasks |
|---|---|
| FR-1 Show filter bar above task list | 9, 10 |
| FR-2 Filter by status OPEN/COMPLETED | 6, 8, 11 |
| FR-3 Filter by inclusive taskDate range | 6, 8, 11 |
| FR-4 Combine status + date filters | 6, 8, 11 |
| FR-5 Show all tasks when no filter | 7, 8, 11 |
| FR-6 Reject invalid date range and show validation error | 4, 7, 8, 9, 11 |
| FR-7 Always scope to authenticated username | 7, 8, 11 |
| FR-8 Clear filters by navigating to /tasks without params | 9 |
| NFR Security (maintain Spring Security + user scoping) | 7, 8, 11 |
| NFR Maintainability (preserve layering + interfaces) | 3, 5, 7 |
| NFR Scalability (repository-level optimization possible) | 5, 6 |
| NFR Observability (validation visible in UI) | 4, 8, 9 |
| NFR Compatibility (no migration) | 6 |
| NFR Testability (controller/service/repository tests) | 11 |
