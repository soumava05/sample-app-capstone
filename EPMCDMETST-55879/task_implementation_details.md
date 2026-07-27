## Planning Summary
This plan covers implementing **server-side search and filtering** for the authenticated user’s Todo Dashboard (`GET /tasks`) as specified in Jira **EPMCDMETST-55879**. The design explicitly preserves the existing Spring Boot MVC + Thymeleaf architecture, Spring Security session authentication, and file-based JSON persistence.

The implementation approach is to:
- Introduce a **TaskFilter DTO** to capture query parameters (keyword/status/date range).
- Extend the **TaskService** API with a filter-aware retrieval method.
- Implement filter validation and predicate-based filtering in **DefaultTaskService** (service layer), operating only on already user-scoped tasks retrieved via the repository.
- Update **TaskController** to bind query params, handle invalid date ranges without crashing, and return a user-visible validation message.
- Update **tasks.html** to render the filter UI, preserve values, and provide a clear-filters action.
- Add/extend **unit/controller tests** to verify filtering behavior, AND-combination semantics, inclusive date ranges, and strict user isolation.

## Inputs Used
- Repo: https://github.com/soumava05/sample-app-capstone (branch: `EPMCDMETST-55879`)
- Design docs (folder `EPMCDMETST-55879/`):
  - `sample_todo_app_details.md`
  - `solution-diagrams.md` (C4 context/component, sequence, deployment, data flow, domain model, rollout plan)
- Confluence design link provided by user:
  - https://epam-team-js1kicba.atlassian.net/wiki/spaces/EliteA/pages/25755649/Design+Diagram+-+EPMCDMETST-55879

## Priority-Ordered Task List
### Prerequisites
**T01 — Confirm current controller/service/template contracts for `/tasks`**
- **Purpose:** Establish baseline of existing dashboard flow (model attributes, current service call, view name) so changes remain non-breaking for create/complete flows.
- **Dependencies:** None.
- **Verification intent:** Identify current `TaskController.taskDashboard` signature and model attributes; ensure new filter binding won’t break existing test expectations.

**T02 — Define filter contract and parameter naming for `GET /tasks`**
- **Purpose:** Translate the design into an explicit contract for query parameters: `keyword`, `status`, `dateFrom`, `dateTo`, including allowed values and defaults.
- **Dependencies:** T01.
- **Verification intent:** Ensure controller binding and Thymeleaf form fields align exactly with query parameter names described in `solution-diagrams.md` and `sample_todo_app_details.md`.

### Feature Work
**T03 — Add `TaskFilter` DTO (query/form model)**
- **Purpose:** Introduce a DTO that carries `keyword`, `status`, `dateFrom`, `dateTo` for the dashboard.
- **Dependencies:** T02.
- **Verification intent:** DTO compiles, supports Spring MVC binding for optional query params (including empty values), and can be added to the MVC model for value preservation.

**T04 — Extend `TaskService` interface with a filter-aware retrieval method**
- **Purpose:** Add `getUserTasks(String username, TaskFilter filter)` and keep `getUserTasks(String username)` as an overload delegating to “empty filter” behavior.
- **Dependencies:** T03.
- **Verification intent:** All call sites compile; existing flows can continue using the old method without behavior changes.

**T05 — Implement filter validation + filtering logic in `DefaultTaskService`**
- **Purpose:** Apply server-side filtering (case-insensitive keyword matching title/description, status filter with ALL/OPEN/COMPLETED, inclusive date range on `taskDate`) while preserving user scoping.
- **Dependencies:** T04.
- **Verification intent:** Filtering uses AND semantics across criteria; blank/empty keyword behaves as no keyword filter; invalid date range triggers a clear validation outcome consistent with the design.

**T06 — Implement invalid date-range handling strategy in service/controller boundary**
- **Purpose:** Ensure “dateFrom after dateTo” does not crash and produces a user-visible validation message.
- **Dependencies:** T05.
- **Verification intent:** For invalid range, `/tasks` returns HTTP 200 with `tasks` view and an error banner/message, while still returning only the authenticated user’s tasks.

**T07 — Update `TaskController` to bind query parameters and call the new service method**
- **Purpose:** Add binding of `TaskFilter` for `GET /tasks`, derive username strictly from `Authentication.getName()`, and populate model attributes required by `tasks.html`.
- **Dependencies:** T04, T06.
- **Verification intent:** Controller never accepts username from query params; model contains tasks + filter + error (when applicable) + existing `taskForm` attribute used by create flow.

**T08 — Update Thymeleaf `tasks.html` to add filter UI + preserve values + clear filters**
- **Purpose:** Implement filter panel (keyword input, status select with ALL/OPEN/COMPLETED, dateFrom/dateTo inputs), “Apply filters” submission via GET, and “Clear filters” action linking to `/tasks`.
- **Dependencies:** T07.
- **Verification intent:** Values persist after applying filters; error message is displayed on invalid date ranges; create and complete flows remain unchanged.

### Hardening / Cleanup
**T09 — Add/extend `DefaultTaskService` tests for filtering behavior and user scoping**
- **Purpose:** Validate predicate logic independently of web layer.
- **Dependencies:** T05.
- **Verification intent:** Tests cover:
  - keyword matches title case-insensitively
  - keyword matches description case-insensitively
  - status filters OPEN and COMPLETED
  - status ALL returns all tasks
  - combined filters apply AND semantics
  - inclusive `dateFrom`/`dateTo` behavior
  - invalid date range produces defined validation outcome
  - repository called only with normalized authenticated username

**T10 — Add/extend `TaskController` tests for query binding, model population, and invalid range UX**
- **Purpose:** Ensure MVC contract and user-visible behavior are correct.
- **Dependencies:** T07, T08.
- **Verification intent:** Tests cover:
  - `GET /tasks` without params returns full user task list
  - `GET /tasks` with params calls service with bound filter
  - invalid date range returns `tasks` view with error in model (HTTP 200)
  - create (`POST /tasks`) and complete (`POST /tasks/{id}/complete`) flows still redirect as before

**T11 — Verify end-to-end behavior locally via `mvn test` and basic manual UI check**
- **Purpose:** Confirm overall feature correctness against requirements and no regressions.
- **Dependencies:** T09, T10.
- **Verification intent:** All tests pass; manual check confirms filter panel and clear filters behavior.

## Dependency Ordering
- **Sequential prerequisites:** T01 → T02
- **Core feature chain:** T02 → T03 → T04 → T05 → T06 → T07 → T08
- **Testing/hardening:**
  - T09 depends on T05
  - T10 depends on T07 + T08
  - T11 depends on T09 + T10

**Parallelization opportunities (after prerequisites):**
- After T04: service-layer filtering (T05/T06) and controller update prep (T07 scaffolding) can proceed in parallel if interfaces are finalized.
- After T07: template work (T08) can proceed in parallel with tests (T09/T10) once model contract is stable.

## Blocked Tasks
None identified from provided design docs.

## Verification Notes
- **Security constraint (must hold):** Filtering must never return another user’s tasks. Verification relies on ensuring the username is taken from `Authentication.getName()` and repository access remains `findByUsername(normalizedUsername)` before any filtering.
- **Date-range validation UX:** Design recommends HTTP 200 returning `tasks` view with inline message; tests should assert the view and model attributes rather than expecting redirects.
- **Regression protection:** Ensure existing create-task and mark-completed flows remain unchanged (controller mappings and redirects should be asserted by tests).

## Risks and Execution Notes
- **Binding edge cases:** Query params for `status` may come as blank or missing; the design recommends supporting both blank and `ALL`. This should be explicitly verified in controller binding tests.
- **Template-model coupling:** `tasks.html` may currently depend on specific model attribute names; controller changes must preserve them to avoid runtime rendering errors.
- **Date parsing:** `dateFrom`/`dateTo` must bind as ISO dates; invalid formats are not specified in the design and should not be assumed—avoid adding new behavior beyond the documented invalid range requirement.

## Assumptions and Open Questions
- **Open question (from design):** Should invalid date range be handled as HTTP 200 with inline message or redirect with flash error?
  - The design recommends inline message with HTTP 200; implementation should follow that unless repository/existing patterns dictate otherwise.
- **Open question:** Should `status` filter accept literal string `ALL` or treat empty/missing as ALL?
  - The design recommends supporting both; confirm what current UI conventions are in `tasks.html` before finalizing.

## Requirement Traceability
- **Server-side search + filtering on `/tasks`:** T02–T08
- **Keyword matches title/description case-insensitively:** T05, T09
- **Status filter supports ALL/OPEN/COMPLETED:** T02, T05, T08, T09
- **Optional `from`/`to` inclusive range on `taskDate`:** T02, T05, T08, T09
- **Filters combinable with AND semantics:** T05, T09
- **Clear filters returns full user task list:** T08, T10
- **Invalid date ranges show validation message and no crash:** T06, T07, T08, T10
- **Never return another user's tasks:** T05, T07, T09, T10
- **Create task / mark completed unchanged:** T01, T07, T10
- **Preserve MVC/Thymeleaf + file JSON persistence + layered boundaries:** T01–T08 (design-constrained), validated indirectly by limiting changes to DTO/service/controller/template and not altering persistence technology
