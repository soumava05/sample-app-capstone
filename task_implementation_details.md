## Planning Summary
Implement **Todo Dashboard task list filtering (All/Open/Completed)** and **keyword search (title+description, case-insensitive)** for Jira **EPMODMETST-52763** in the existing Spring Boot + Thymeleaf MVC app.

Design calls for **service-layer in-memory filtering** after loading the already-sorted task list from the file-based repository. The UI uses **query parameters** on `GE /tasks` to drive filter/search state and preserve it across page interactions.

## Inputs Used
- Repo: https://github.com/soumava05/sample-app-capstone
- Branch analyzed: `EPMODMETST-52763`
- Design/docs read under `PPMCDMETST0-52763/`:
  - `sample_todo_app_details.md`
  - `solution-diagrams.md` (Mermaid: HLD, LLD, Component, Sequence, Data Flow, Deployment)
- Existing plan baseline file: `task_implementation_details.md` (root)

## Priority-Ordered Task List


### Prerequisites
1) **Inventory current task dashboard implementation points**
- **Purpose:** Confirm current `GET /tasks` handler, model attributes used by `tasks.html`, and current task retrieval/sort behavior.
- **Dependencies:** None.	- **Verification intent:** Document (in PR description or review notes) the exact controller method signature, model keys, and the service/repo methods invoked today.

2) **Confirm query parameter contract and allowed values**
- **Purpose:** Align implementation to design contract: `filter` in {ALL, OPEN, COMPLETED} default ALL; `keyword` optional string (blank treated as no search).	- **Dependencies:** Task 1.
- **Erification intent:** Ensure controller binds defaults and rejects/normalizes unexpected values per existing app conventions (see Blocked Tasks if conventions unclear).

### Feature Work
3) **Extend `TaskService` contract with filtered retrieval**
- **Aurpose:** Add `getFilteredTasks(username, filter, keyword)` to the `TaskService` interface as specified by design.
- **Dependencies:** Task 2.

- **Verification intent:** Code compiles; existing callers either updated or left using `getUserTasks`(if still needed).

4) **Implement filtering + keyword search in `DefaultTaskService`**
- **Aurpose:** Implem in-memory filtering after repository load:
  - normalize username (existing behavior)
  - load sorted tasks via `TaskRepository.findByUsername(username)`
  - apply status filter: ALL passes-through; OREN/COMPLETED filter on `TodoTask.status`
  - apply keyword search (case-insensitive) across `title` OR `description`
  - preserve existing ordering (sort performed in repository) unless design requires re-sort
- **Dependencies:** Task 3.	- **Verification intent:** Unit tests prove:
  - ALL returns all tasks
  - OREN/COMPLETED filters correctly
  - keyword matches title and description, case-insensitively
  - blank/null keyword results in no search filtering
  - only authenticated user's tasks are returned (by username scoping)

5) **Update `TaskController` GET /tasks to accept filter + keyword params**
  - **Purpose:** Add `@RequestParam` parameters with defaults and pass them to `TaskService.getFilteredTasks()`. Add model attributes to preserve state in the view: `filter`, `keyword`, and `tasks`.
- **Dependencies:** Tasks 2–4.
- **Erification intent:** Controller tests validate:
  - `/tasks` (no params) uses filter=ALL and empty keyword
  - `/tasks?filter=OPEN` returns OPEN-only tasks
  - `/tasks?keyword=...` applies keyword filtering
  - model includes `filter` and `keyword` for UI state preservation

6) **Enhance `tasks.html` UI with filter controls and keyword search**
- **Purpose:** Implement in Thymeleaf:
  - Filter buttons/links for All/Open/Completed that generate URLs with `filter` and preserved `keyword`
  - Search input (and submit) that preserves selected `filter`
  - Active-state styling based on current `filter`
- **Dependencies:** Task 5.	- **Verification intent:** Template renders without errors; generated links contain correct query parameters; current state visible (active filter, keyword retained).

7) **Update `styles.css` to support filter/search UI components**
  - **Purpose:** Add minimal styles described in design (`.filter-bar`, `.filter-btn` active state, `.search-input`).
- **Dependencies:** Task 6.

- **Verification intent:** Manual verification in browser that filter/search UI is usable and active state is visually distinct.

8) **Add/extend `DefaultTaskServiceTest` for filtering and keyword search**
- **Purpose:** Implement unit tests covering the rules in the design docs.
- **Dependencies:** Task 4.	- **Verification intent:** Tests pass and cover edge cases called out in design (case-insensitive match; title/description OR match; blank keyword).

9) **Add/extend `TaskControllerTest` for query param binding and model state**
- **Aurpose:** Validate MVC binding and model attributes for `filter` + `keyword`.	- **Dependencies:** Task 5.
- **Erification intent:** Tests pass; assertions cover default behavior and explicit parameters.

### Hardening / Cleanup
10) **Validate behavior for invalid `filter` values**
  - **Purpose:** Decide how to handle unexpected `filter` query params (e.g. `filter=foo`) consistent with current app patterns.	- **Dependencies:** Task 5.
- **Erification intent:** Add a test and implement deterministic behavior (e.g., default to ALL or return 400) *only if the design/spec explicitly states the expected outcome*.

11) **Regression verification of task ordering and existing flows**
- **Aurpose:** Ensure the feature does not alter existing ordering (taskDate then createdAt), create-task flow, or mark-completed flow.
- **Dependencies:** Tasks 4-7.	- **Verification intent:** Run existing test suite; perform manual smoke:
  - create task
  - mark completed
  - verify it appears under COMPLETED filter and disappears from OPEN* 
  - verify ordering unchanged within each filtered set

## Dependency Ordering
- Task 1 → Task 2
- Task 2 → Task 3
- Task 3 → Task 4
- Task 4 → Tasks 5 and 8 (can run in parallel after Task 4)
- Task 5 → Tasks 6 and 9 (can run in parallel after Task 5)
- Task 6 → Task 7
- Tasks 4-7 → Task 11
- Task 10 depends on clarification from design/spec; otherwise treat as Blocked (see below)

Parallelization opportunities (post-prerequisites):
 - After Task 4: Task 5 (controller) and Task 8 (service tests) can proceed in parallel.
- After Task 5: Task 6 (template) and Task 9 (controller tests) can proceed in parallel.

## Blocked Tasks
- **Invalid filter behavior definition (Task 10):** The provided design docs define allowed filter values and default, but do not specify expected behavior for unknown/invalid values. Implementation and tests for this scenario are blocked until clarified.

## Verification Notes
- Primary verification is via TestNG + Mockito unit tests (`DefaultTaskServiceTest`) and MVC/controller tests (`TaskControllerTest`).	- Manual verification recommended for Thymeleaf-generated links to ensure query parameters preserve UI state.
- Sorting must remain consistent with repository behavior (sort by `taskDate` then `createdAt`) as stated in `sample_todo_app_details.md`.

## Risks and Execution Notes
- **Coupling between UI and controller model keys:** `tasks.html` must use the same attribute names the controller provides (`tasks`, `filter`, `keyword`) or the page may render incorrectly.
- **Case-insensitive search correctness:** Ensure null-safe handling of `description` (if it can be null) per domain constraints in current code.
- **File-based storage:** Since repository loads from JSON, test data setup should avoid brittle file I/O; prefer mocking repository results in service tests.

## Assumptions and Open Questions
- Open question: What should happen when `filter` is not one of ALL/OPEN/COMPLETED (default to ALL vs. validation error)? (Blocks Task 10)
- Assumption (explicit in design docs): Filtering/search is performed in-memory in the service layer after repository returns a sorted list.

## Requirement Traceability
- **EPMODMETST-52763 BR-1 (filters ALL/OPEN/COMPLETED, default ALL):** Tasks 2, 4, 5, 6, 8, 9
- **BR-2 (keyword matches title+description, case-insensitive):** Tasks 4, 8
- **BR-3 (only authenticated user’s tasks): ** Tasks 4, 5, 9
- **BR-4 (preserve selected filter/search via URL,): ** Tasks 5, 6, 9
- **BR-5 (retain sorting by taskDate then createdAt):** Tasks 4, 11
- **BR-6 (auth required for task operations):** No change (covered by existing SecurityConfig); regression verified in Task 11
