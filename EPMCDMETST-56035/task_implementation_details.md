## Planning Summary
This plan covers implementation work derived strictly from the available design artifacts for adding **task filtering (status, date range) and sorting** to the existing Spring Boot + Thymeleaf Todo application.

The design inputs describe a server-rendered MVC flow where the dashboard at `GET /tasks` accepts query parameters, binds them into a DTO/form object, and the service layer applies filtering and sorting over the authenticated user’s task list loaded from JSON file storage.

The work is organized into prerequisites, feature work, and hardening/cleanup tasks and ordered by dependencies.

## Inputs Used
1. **Repository**: `https://github.com/soumava05/sample-app-capstone` (default branch: `main`)
2. **Repo design doc (Markdown)**: `EPMCDMETST-55591/solution-diagrams.md`
   - Contains C4 Context/Component/Deployment diagrams, sequence, data-flow, class diagram for filtering+sorting via `TaskFilterForm` and `TaskService.getUserTasks(username, filterForm)`.
3. **Confluence design page**: *Design Diagram* (id `25853989`)
   - Focuses on status filter on `/tasks` with preservation across `POST /tasks` and `POST /tasks/{taskId}/complete` via query parameter redirect.
4. **Repo README**: `README.md`
   - Confirms layering (controller/service/repository/storage), file-based persistence under `storage/`.

## Priority-Ordered Task List
### Prerequisites
1. **Create planning folder for ticket and add plan file**
   - **Purpose**: Ensure the planning artifact exists under the ticket folder as required by the process.
   - **Dependencies**: None.
   - **Verification intent**: `EPMCDMETST-56035/task_implementation_details.md` exists in repo and renders as Markdown.

2. **Align endpoint behavior with design contracts (status/date/sort query params)**
   - **Purpose**: Confirm the feature’s API contract on `GET /tasks` and redirect preservation rules for `POST /tasks` and `POST /tasks/{taskId}/complete`.
   - **Dependencies**: Task 1.
   - **Verification intent**: A documented checklist (in PR description or notes) mapping required query params and redirect rules to implementation touchpoints.

### Feature Work
3. **Introduce/extend filter DTO to carry status/date/sort parameters**
   - **Purpose**: Implement the designed parameter carrier (e.g., `TaskFilterForm`) holding:
     - `status` (OPEN/COMPLETED or ALL semantics)
     - `from` (start date)
     - `to` (end date)
     - `sortBy` and `sortDir`
   - **Dependencies**: Task 2.
   - **Verification intent**: Unit tests validating default values and binding-friendly shape (e.g., null/blank inputs map to safe defaults).

4. **Controller: bind filter/sort params on `GET /tasks` and render them in model**
   - **Purpose**: Implement controller behavior described in diagrams:
     - Bind query parameters into the filter DTO
     - Call `TaskService.getUserTasks(username, filterForm)`
     - Populate the model with task list + current filter values
   - **Dependencies**: Task 3.
   - **Verification intent**: MVC tests verifying:
     - `GET /tasks` returns 200
     - model contains tasks and filter DTO
     - selected filter is preserved in rendered view model.

5. **Service: implement filtering (status + date range) and sorting over user tasks**
   - **Purpose**: Implement `DefaultTaskService.getUserTasks(username, filterForm)` behavior:
     - Load all tasks for user from repository
     - Apply status filter (including ALL fallback)
     - Apply date range filtering (`from`/`to`) over `taskDate` (per diagram intent)
     - Apply dynamic sorting based on `sortBy`/`sortDir`
   - **Dependencies**: Task 3.
   - **Verification intent**: Unit tests covering:
     - status filter values and fallback
     - from-only, to-only, from+to
     - sorting by supported fields and direction.

6. **Persist filter selection across POST redirects**
   - **Purpose**: Implement redirect rules from design:
     - `POST /tasks` redirects to `/tasks?...` preserving current filter/sort
     - `POST /tasks/{taskId}/complete` redirects to `/tasks?...` preserving current filter/sort
   - **Dependencies**: Task 4.
   - **Verification intent**: MVC tests asserting 3xx redirect locations include expected query parameters.

7. **Thymeleaf view: add/update filter and sort controls on tasks dashboard**
   - **Purpose**: Render UI elements described in design so that the user can select:
     - status filter
     - date range
     - sorting field + direction
     - and see preserved selection on reload.
   - **Dependencies**: Task 4.
   - **Verification intent**: Template-level checks (MVC test content assertions or snapshot-like assertions) that the form includes expected controls and that selected values are echoed back.

### Hardening / Cleanup
8. **Validation and safe defaults for unsupported filter/sort values**
   - **Purpose**: Enforce the design’s “invalid → ALL” behavior for status and prevent invalid sort fields/directions from breaking the page.
   - **Dependencies**: Tasks 3, 5.
   - **Verification intent**: Unit tests:
     - invalid status maps to ALL
     - invalid sortBy/sortDir maps to known default.

9. **Document supported filter/sort query parameters**
   - **Purpose**: Provide a concise reference of `GET /tasks` params and examples to match design contracts.
   - **Dependencies**: Tasks 2, 7.
   - **Verification intent**: README or ticket-folder doc updated with parameter table and example URLs.

10. **End-to-end smoke test scenario (manual checklist)**
   - **Purpose**: Verify the full flow described in sequence diagrams:
     - open tasks page, apply filters/sort
     - create task while filter active
     - mark completed while filter active
     - ensure redirect returns to same filtered view.
   - **Dependencies**: Tasks 5–7.
   - **Verification intent**: Recorded manual test steps + expected results in PR description or ticket doc.

## Dependency Ordering
- Task 1 → Task 2
- Task 2 → Task 3
- Task 3 → Tasks 4 and 5 (can be parallel)
- Task 4 → Tasks 6 and 7 (can be parallel)
- Task 5 → Task 8
- Tasks 2 and 7 → Task 9
- Tasks 5, 6, 7 → Task 10

Parallelizable after prerequisites:
- Tasks 4 and 5 can proceed in parallel once Task 3 is done.
- Tasks 6 and 7 can proceed in parallel once Task 4 is done.

## Blocked Tasks
- **None identified from the provided design artifacts.**

## Verification Notes
- Prefer **service unit tests** for filtering/sorting rules (status fallback, date range boundaries, ordering).
- Prefer **Spring MVC tests** for:
  - binding of query params into DTO
  - model attributes presence
  - redirect URL correctness (preserving params)
  - basic rendered HTML assertions for selected values.
- Date filtering needs explicit boundary expectations (inclusive vs exclusive); add tests to lock the intended behavior.

## Risks and Execution Notes
- **Design discrepancy**: Confluence design focuses on status filtering (ALL/OPEN/COMPLETED), while repo `solution-diagrams.md` expands scope to include date range and sorting. Implementation should ensure consistency with the intended ticket scope.
- **Sorting safety**: Dynamic sort-by requires an allowlist of sortable fields to avoid runtime failures.
- **File-based storage**: Filtering/sorting is in-memory post-load; for large task files this could become slower (acceptable unless NFRs say otherwise; none provided).

## Assumptions and Open Questions
- **Open question**: Which task date field is the canonical filter target—`taskDate`, `plannedFinishDate`, or both? (Repo diagram suggests `taskDate`; domain model contains both.)
- **Open question**: Exact list of supported `sortBy` fields (e.g., `taskDate`, `plannedFinishDate`, `createdAt`, `status`).
- **Open question**: Should status filtering accept `ALL` as an explicit value or treat missing/blank/invalid as ALL only?
- **Open question**: Should date range filtering be inclusive on boundaries?

## Requirement Traceability
- **GET /tasks?status=...** filtering contract → Tasks 2, 4, 5, 8
- **Preserve filter selection across POST redirects** → Task 6
- **UI control for filter selection** → Task 7
- **Date range filter and sorting controls** (as per repo solution diagrams) → Tasks 3, 5, 7, 8, 9
