## Planning Summary
Implement status-based task list filtering (All/Open/Completed) and keyword search (case-insensitive across title + description) for the Todo Dashboard (GET `/tasks`) as described in the design docs under `EPMCDMETST-52763/`. The design keeps persistence unchanged (file-based JSON). Filtering and searching are performed in-memory in the Service layer after loading the already-sorted list from the repository. UI state is preserved via query parameters (`filter`, `keyword`) and echoed back to the Thymeleaf model.

This plan is derived strictly from:
- `EPMCDMETST-52763/sample_todo_app_details.md` (feature + rules + files-to-modify)
- `EPMCDMETST-52763/solution-diagrams.md` (HLD/LLD/sequence/data-flow/deployment diagrams)

## Inputs Used
- Repository: https://github.com/soumava05/sample-app-capstone
- Branch analyzed: `EPMCDMETST-52763`
- Design artifacts read (folder `EPMCDMETST-52763/`):
  - `sample_todo_app_details.md`
  - `solution-diagrams.md`

## Priority-Ordered Task List
### Prerequisites
**T1. Baseline current `/tasks` dashboard behavior (controller → service → repo → view)**
- **Purpose:** Confirm current GET `/tasks` handler signature, model attributes used by `tasks.html`, and current retrieval/sorting path so that new params don’t break the existing flow.
- **Dependencies:** None.
- **Verification intent:** Document (in PR notes) the current controller method, model keys, and service/repo calls; confirm current task ordering is “taskDate then createdAt” (as stated in design).

**T2. Confirm query parameter contract and allowed values**
- **Purpose:** Align implementation with design contract: `filter ∈ {ALL, OPEN, COMPLETED}` default `ALL`; `keyword` optional string (blank = no search). Confirm how to treat null/blank/whitespace.
- **Dependencies:** T1.
- **Verification intent:** Identify exact controller binding approach (e.g., `@RequestParam(defaultValue=...)`) and document expected behavior for missing params.

### Feature Work
**T3. Extend `TaskService` interface with filtered retrieval method**
- **Purpose:** Add `getFilteredTasks(username, filter, keyword)` to `TaskService` as specified by design.
- **Dependencies:** T2.
- **Verification intent:** Project compiles with updated interface; all implementations/callers updated accordingly.

**T4. Implement in-memory filtering + keyword search in `DefaultTaskService`**
- **Purpose:** Implement `getFilteredTasks(...)`:
  - Normalize username (per existing behavior).
  - Load user tasks via `TaskRepository.findByUsername(username)`.
  - Apply status filter: `ALL` passes through; `OPEN`/`COMPLETED` filters on `TodoTask.status`.
  - Apply keyword search (case-insensitive) on `title` OR `description`.
  - Preserve ordering (repository returns sorted list) unless design requires re-sort.
- **Dependencies:** T3.
- **Verification intent:** Unit tests (see T8) cover ALL/OPEN/COMPLETED, keyword in title/description, case-insensitivity, and blank keyword behavior.

**T5. Update `TaskController` GET `/tasks` to accept `filter` + `keyword` query params**
- **Purpose:** Add `@RequestParam` bindings with defaults and wire to `TaskService.getFilteredTasks(...)`. Add model attributes for UI state preservation: `tasks`, `filter`, `keyword`.
- **Dependencies:** T4.
- **Verification intent:** MVC tests (see T9) confirm defaulting, parameter binding, correct delegation to service, and presence of model attributes.

**T6. Enhance `tasks.html` with filter controls and keyword search UI**
- **Purpose:** Implement in Thymeleaf:
  - Filter buttons/links for All/Open/Completed generating URLs with `filter` and preserved `keyword`.
  - Search input submit that preserves selected `filter`.
  - Active-state indication based on current `filter`.
- **Dependencies:** T5.
- **Verification intent:** Manual rendering check: correct links include expected query params; current state is visible (active filter + keyword retained).

**T7. Update `styles.css` to style filter/search controls**
- **Purpose:** Add minimal CSS for `.filter-bar`, `.filter-btn` active state, and `.search-input` per design.
- **Dependencies:** T6.
- **Verification intent:** Manual browser check that new controls are usable and active state is clearly indicated.

### Hardening / Cleanup
**T8. Add/extend `DefaultTaskServiceTest` for filter/search rules**
- **Purpose:** Implement unit tests for service logic:
  - `ALL` returns all tasks.
  - `OPEN` returns open-only; `COMPLETED` returns completed-only.
  - Keyword matches title or description, case-insensitive.
  - Blank keyword => no search filtering.
  - Ensures tasks are scoped to authenticated username input (service receives username).
- **Dependencies:** T4.
- **Verification intent:** TestNG suite passes; tests assert returned list contents and ordering is unchanged.

**T9. Add/extend `TaskControllerTest` for query params + model state**
- **Purpose:** Implement MVC/controller tests validating:
  - `/tasks` with no params uses `filter=ALL` and empty keyword.
  - `/tasks?filter=OPEN` binds correctly.
  - `/tasks?keyword=...` binds and passes through.
  - Model contains `filter` and `keyword` to preserve UI state.
- **Dependencies:** T5.
- **Verification intent:** TestNG MVC tests pass; assertions verify `model().attribute(...)` and service invocations.

**T10. Define behavior for invalid `filter` values (e.g., `filter=foo`)**
- **Purpose:** Decide and implement deterministic handling consistent with existing app conventions (e.g., default to `ALL` vs. return 400). **Design docs do not specify this behavior.**
- **Dependencies:** T5.
- **Verification intent:** Once clarified, implement and add a test covering invalid value behavior.

**T11. Regression verification of existing flows and ordering**
- **Purpose:** Ensure feature does not alter:
  - Create task flow (`POST /tasks`).
  - Mark complete flow (`POST /tasks/{id}/complete`).
  - Ordering rule (taskDate then createdAt) within each filtered result set.
  - Security expectations (“authenticated access required for all task operations”).
- **Dependencies:** T4–T7.
- **Verification intent:** Run full test suite; perform manual smoke checks: create → complete → verify it appears under Completed filter and not under Open; verify ordering remains consistent.

## Dependency Ordering
- T1 → T2
- T2 → T3 → T4 → T5
- After T4: T5 and T8 can proceed in parallel.
- After T5: T6 and T9 can proceed in parallel.
- T6 → T7
- T4–T7 → T11
- T10 depends on clarification (blocked); can be implemented after T5 once resolved.

## Blocked Tasks
- **T10 (invalid `filter` behavior)** is blocked because the design specifies allowed values and defaulting but does not define how to handle unknown/invalid values (e.g., `filter=foo`).

## Verification Notes
- Primary verification is via TestNG + Mockito unit tests:
  - `DefaultTaskServiceTest` for filtering/search logic.
  - `TaskControllerTest` for request param binding + model state.
- Manual verification is required for Thymeleaf UI correctness:
  - Filter button active-state and link/query-param preservation.
  - Search input retains keyword and does not reset filter unless intended.
- Regression checks must ensure ordering remains “taskDate then createdAt” (as described in `sample_todo_app_details.md`) and flows are unaffected.

## Risks and Execution Notes
- **UI/controller coupling risk:** `tasks.html` must use the same model attribute names provided by the controller (`tasks`, `filter`, `keyword`) or UI state preservation will fail.
- **Null-safety in keyword search:** Ensure `description` handling is null-safe if domain constraints allow it to be null; design requires matching against title and description.
- **File-based persistence constraints:** Prefer mocking repository results in service tests to avoid brittle file I/O and locks (consistent with design choice of service-layer filtering).

## Assumptions and Open Questions
- **Open question (blocks T10):** What should happen when `filter` is not one of `ALL/OPEN/COMPLETED`?
  - Option A: default to `ALL`.
  - Option B: reject with 400 / show validation error.
  - Design artifacts do not state the expected behavior.
- Assumption stated in design docs: filtering/search occurs in-memory in the service layer after repository load; persistence layer remains unchanged.

## Requirement Traceability
From `sample_todo_app_details.md` “Key Business Rules”:
- **BR-1 (filters ALL/OPEN/COMPLETED; default ALL):** T2, T4, T5, T6, T8, T9
- **BR-2 (keyword matches title AND description; case-insensitive):** T4, T8
- **BR-3 (applies only to authenticated user’s tasks):** T4, T5, T9
- **BR-4 (preserve filter/search via URL query params):** T5, T6, T9
- **BR-5 (results sorted by taskDate then createdAt; behavior retained):** T1, T4, T11
- **BR-6 (authenticated access required for task ops):** T11 (regression verification; security config unchanged by design)
