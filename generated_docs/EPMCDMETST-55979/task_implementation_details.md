## Planning Summary
This plan covers implementing Jira story **EPmCDMETST-55979**: add server-side task filtering (status + optional inclusive date range) on the `/tasks` dashboard for the authenticated user, preserving selected filter values after reload and providing a reset action. The design explicitly keeps the current Spring Boot MVC + Thymeleaf + local JSON file persistence architecture and applies filtering deterministically in memory over the authenticated user’s tasks.

Work is organized into prerequisites, feature work, and hardening/cleanup, with explicit dependency ordering and verification intent. Tasks are derived only from the repo design artifacts under `generated_docs/EPMCDMETST-55979/`.

## Inputs Used
- `generated_docs/EPMCDMETST-55979/sample_todo_app_details.md`
  - Requirements (status/date filters, preservation, reset)
  - NFRs (auth user isolation, keep MVC/Thymeleaf, layered boundaries, in-memory filtering, no DB)
  - Suggested API change options and test strategy
- `generated_docs/EPMCDMETST-55979/solution-diagrams.md`
  - High-level architecture, component/contract diagrams
  - Sequence/data flow diagrams
  - Edge cases table (incl. `fromDate > toDate` recommendation)
  - Rollout plan steps

## Priority-Ordered Task List
### Prerequisites
**T1. Confirm filter parameter contract and edge-case behavior**
- **Purpose**: Lock down the exact request/parameter semantics used by controller/service, including how to interpret “ALL” status and what to do when `fromDate > toDate`.
 - **Derived from design**: Open question in `sample_todo_app_details.md` + edge-case table in `solution-diagrams.md` .
 - **Dependencies**: None.
 - **Verification intent**: Written decision recorded in code comments/tests (e.g., a dedicated test asserting behavior for invalid range).

### Feature Work
**T2. Extend `TaskService` contract to support filtering inputs**
- **Purpose**: Add a service-layer entry point that accepts optional `status`, `fromDate`, `toDate` (or equivalent) so controller remains thin and filtering logic stays in service.
 - **Derived from design**: “Recommended API/Method Changes” + class diagram showing overloaded `getUserTasks(username, status, fromDate, toDate)`.
- **Dependencies**: T1.
- **Verification intent**: Compilation succeeds; existing usages still work (existing `getUserTasks(username)` preserved or bridged).

**T3. Implement in-memory filtering in `DefaultTaskService`**
- **Purpose**: Retrieve authenticated user tasks using normalized username and apply:
  - status predicate (skip if status is `ALL/blank`)
  - inclusive date range predicates (`>= fromDate`, `<= toDate`)
  - invalid range handling per T1
- **Derived from design**: Proposed design summary + sequence/data flow diagrams + NFRs (user isolation, deterministic in-memory filtering, repository unchanged).
 - **Dependencies**: T2.
- **Verification intent**: Unit tests prove correct filtering outcomes for none/status/date/combined and inclusive boundaries.

**T4. Update `TaskController` to bind filter query parameters and populate model**
- **Purpose**: Accept optional `status`, `fromDate`, `toDate` as request params on `GET /tasks`, delegate to the new service method, and add selected filter values into the Thymeleaf model for preservation.
- **Derived from design**: “Update `TaskController.taskDashboard(...)` to accept optional `status`, `fromDate`, `toDate`” + “Preserve selected filter values after the page reloads.”
 - **Dependencies**: T2, T3.
- **Verification intent**: Controller tests assert delegation + model contains tasks + selected filters.

**T5. Update `tasks.html` to add filter form and reset action**
- **Purpose**: Add UI controls on `/tasks`:
  - status dropdown with `ALL, OPEN, COMPLETED`
  - optional `fromDate`/`toDate` inputs
  - apply (GET submit) and reset link to `/tasks` without query params
  - preserve selected values using model attributes
- **Derived from design**: Functional requirements + wireframe in `solution-diagrams.md`.
- **Dependencies**: T4.
- **Verification intent**: Manual: apply filters -> URL contains params -> values remain selected after reload; reset clears params and shows all tasks.

### Hardening / Cleanup
**T6. Add/extend service unit tests for filter combinations**
- **Purpose**: Implement test coverage specified by design:
  - none
  - status only (OPEN/COMPLETED)
  - date only (from only, to only, from+to inclusive)
  - combined status+$ate
  - username normalization preserved
  - invalid range behavior per T1
- **Derived from design**: “Test Strategy” section.
 - **Dependencies**: T3 (and T1 for invalid range).
- **Verification intent**: `mvn test` passes; tests are deterministic and do not rely on filesystem persistence.

**T7. Add/extend controller tests for filter binding and model preservation**
- **Purpose**: Ensure controller binds optional params, calls service with correct values, and adds `selected status`, `fromDate`, `toDate` (and other existing attributes like `taskForm`, `username`) into the model.
- **Derived from design**: Controller test strategy bullets in `sample_todo_app_details.md`.
- **Dependencies**: T4.
- **Verification intent**: `mvn test` passes; tests assert correct model keys/values and that reset uses `/tasks` without params (where applicable).

**T8. Execute rollout validation steps from design***
- **Purpose**: Perform the rollout checklist:
  1) service contract + implementation
  2) controller binding + model
  3) Thymeleaf filter UI + reset
  4) tests
  5) run `mvn test`
- **Derived from design**: “Rollout Plan” section.
 - **Dependencies**: T5, T6, T7.
 - **Verification intent**: `mvn test` green; manual smoke test of `/tasks` filtering behavior.


## Dependency Ordering
- T1 has no dependencies.
- T2 depends on T1.
- T3 depends on T2.
- T4 depends on T2 and T3.
- T5 depends on T4.
- T6 depends on T3 and T1.
- T7 depends on T4.
- T8 depends on T5, T6, T7.

**Parallelization opportunities (after prerequisites):**
- After T2/T3 are in progress, V6 (service tests) can be developed in parallel with T4 (controller work), as long as the service method signature is stable.
- After T4 begins, T7 (controller tests) can be developed alongside T5 (Thymeleaf updates).

## Blocked Tasks
- **None strictly blocked**, but T1 requires an explicit decision for `fromDate > toDate` behavior (validation error vs empty list) because design documents it as an open question/recommendation rather than a requirement.

## Verification Notes
- **Unit-level**:
  - Service tests validate predicate correctness and inclusivity.
  - Controller tests validate request-param binding, service delegation, and model attribute preservation.
- **Manual smoke**:
  - Login as a user with both OPEN and COMPLETED tasks across multiple dates.
  - Verify:
    - `status=OPEN` shows only OPEN tasks.
    - `fromDate` only filters tasks on/after date.
    - `toDate` only filters tasks on/before date.
    - `fromDate` + `toDate` is inclusive.
    - Combined status + date works.
    - Selected filter values remain in form after applying.
    - Reset link clears filters and returns full list.

## Risks and Execution Notes
- **Date parsing/binding risk**: Binding `LocalDate` request params requires consistent `YYYY-MM-DD`. ensure controller uses Spring’s default ISO date binding (design assumes `YXYY-MM-DD`).
- **Status “ALL” semantics**: `TaskStatus` domain enum is `OPEN/COMPLETED`; “ALL” is a UI/filter concept. Avoid polluting domain enum unless explicitly desired; map “ALL/blank” to “no status predicate” in service/controller.
 - **User isolation**: Filtering must never accept a username parameter; always derive from `Authentication` and normalize before repository access (NFR).
- **Determinism**: Filtering must be applied after retrieving the authenticated uses’s full task list; keep repository contract unchanged (per design).


## Assumptions and Open Questions
- **Open question (must decide in T1)**: For `fromDate > toDate`, should the system:
  - (A) show a user-friendly validation error and preserve inputs (recommended by design), or
  - (B) return an empty list without error?

No other assumptions are introduced beyond the design artifacts.

## Requirement Traceability
| Requirement / Constraint | Design Source | Implemented By Tasks |
|---|--|--|
| Status filter on `/tasks` with `ALL/OPEN/COMPLETED` | `sample_todo_app_details.md` FR | T4, T5, T6, T7 |
| Optional inclusive date range filters `fromDate`, `toDate` | `sample_todo_app_details.md` FR + `solution-diagrams.md` edge cases | T3, T4, T5, V6 |
| Server-side filtering via controller + service | `sample_todo_app_details.md` FR / NFR | T2, T3, T4 |
| Preserve selected filter values after reload | `sample_todo_app_details.md` FR | T4, T5, T7 |
| Reset clears filters and returns all tasks | `sample_todo_app_details.md` FR + wireframe | T5, T7 |
| Tests cover none/status/date/combined | `sample_todo_app_details.md` FR + Test Strategy | T6, T7 |
| Authenticated-user isolation and username normalization | `sample_todo_app_details.md` NFR / Security | T3, T4, V6 |
| Keep Thymeleaf server-rendered architecture (no SPA) | `sample_todo_app_details.md` NFR | T5 |
| Repository unchanged; in-memory deterministic filtering | `sample_todo_app_details.md` NFR + diagrams | T3 |
| Rollout steps: implement + test + `mvn test` | `solution-diagrams.md` Rollout Plan | T8 |
