## Implementation Summary
Implemented **Todo Dashboard task list filtering (All/Open/Completed)** and **case-insensitive keyword search** (matching task title OR description) for Jira **EPMCDMETST-52763** in the existing Spring Boot 3.3.2 + Thymeleaf MVC application.

Following the plan exactly, filtering and searching are performed **in-memory in the Service layer** after loading the already-sorted task list from the file-based repository (no persistence-layer changes). The `GET /tasks` endpoint now accepts optional `filter` (`ALL`/`OPEN`/`COMPLETED`, default `ALL`) and `keyword` query parameters. UI state is preserved through URL query parameters and echoed back into the Thymeleaf model so filter selection and search text survive navigation. The existing task ordering (`taskDate` then `createdAt`, produced by `FileTaskRepository.findByUsername`) is preserved because filtering is applied on top of the already-sorted list.

Key behaviors:
- `filter` is bound as a `String`, normalized (trim + uppercase), and mapped to `TaskStatus`. `ALL` (and any blank/invalid value) maps to a `null` status filter meaning "no status restriction" — invalid values default gracefully to `ALL` to avoid breaking the existing flow (design left this as an open question; graceful defaulting chosen consistent with Spring MVC `defaultValue` conventions).
- `keyword` is trimmed; blank/whitespace-only keyword disables search. Matching is case-insensitive across title and description, null-safe for null descriptions.

During implementation a **pre-existing base-branch compilation break** was found: `FileTaskRepository` and `FileUserRepository` referenced `com.capstone.todo.storage.FileStorageManager`, which did not exist on the `EPMCDMETST-52763` base branch (nor on `main`). This blocked all compilation. A **minimal, self-contained** `FileStorageManager` (matching the exact `readList(Path, Class<T>)` / `writeList(Path, List<T>)` API and `(ObjectMapper)` constructor the repositories require, using `java.io.UncheckedIOException` to avoid pulling in unrelated ticket dependencies) was added strictly to unblock the build. A too-broad `.gitignore` rule (`storage/`) that also ignored the new source package was anchored to the repo root (`/storage/`) so runtime storage remains ignored while the source package is tracked.

## Reference Documents Used
- Primary:
  - `EPMCDMETST-52763/task_implementation_details.md` (implementation plan / source of truth)
- Secondary (design):
  - `EPMCDMETST-52763/sample_todo_app_details.md`
  - `EPMCDMETST-52763/solution-diagrams.md` (HLD/LLD/Component/Sequence/Data-Flow/Deployment)
- Guide:
  - `README.md`
- Reference:
  - Full codebase under `src/main/java` and `src/test/java`

## Files Added
Count: 2
- source: `src/main/java/com/capstone/todo/storage/FileStorageManager.java` (minimal base-branch fix to unblock compilation)
- docs: `EPMCDMETST-52763/dev_implementation-details.md`

## Files Modified
Count: 8
- source: `src/main/java/com/capstone/todo/service/TaskService.java` (added `getFilteredTasks(username, filter, keyword)`)
- source: `src/main/java/com/capstone/todo/service/impl/DefaultTaskService.java` (in-memory filter + keyword search implementation)
- source: `src/main/java/com/capstone/todo/web/TaskController.java` (added `filter`/`keyword` request params, normalization, model attributes)
- template: `src/main/resources/templates/tasks.html` (filter buttons + search form, state preserved via query params)
- style: `src/main/resources/static/css/styles.css` (filter bar, active filter button, search input styling)
- test: `src/test/java/com/capstone/todo/service/impl/DefaultTaskServiceTest.java` (filter/search unit tests)
- test: `src/test/java/com/capstone/todo/web/TaskControllerTest.java` (updated for new signature + query-param/model tests)
- config: `.gitignore` (anchored `storage/` -> `/storage/` so source package is tracked)

## Compilation Performed
- Main sources: `mvn -q -DskipTests compile` => SUCCESS
- Test sources: `mvn -q -DskipTests test-compile` => SUCCESS

## Tests Performed
### Unit
- Command: `mvn test`
- Result: **BUILD SUCCESS** — `Tests run: 55, Failures: 0, Errors: 0, Skipped: 0`
- New/updated coverage:
  - `DefaultTaskServiceTest`: username normalization for filtered retrieval; null-filter + blank-keyword returns all; OPEN-only; COMPLETED-only; case-insensitive keyword match in title; case-insensitive keyword match in description; combined filter + keyword; null-description safety; no-match returns empty.
  - `TaskControllerTest`: default (`ALL` + blank keyword) model population; `OPEN` filter with trimmed keyword delegation; `COMPLETED` (lowercase input normalized); invalid filter defaults to `ALL`; existing create/complete flows retained.

### Integration
- Not applicable for this slice (no new integration test scope defined in the plan). All existing tests continue to pass under the full `mvn test` run.

## Requirement Traceability
| Requirement (from plan / design) | Implementation | Verified by |
|---|---|---|
| Status filter All/Open/Completed on `GET /tasks` | `TaskController.taskDashboard` `filter` param -> `TaskStatus` mapping; `DefaultTaskService.getFilteredTasks` status filter | `TaskControllerTest` (ALL/OPEN/COMPLETED/invalid), `DefaultTaskServiceTest` (OPEN/COMPLETED/all) |
| Case-insensitive keyword search over title + description | `DefaultTaskService.matchesKeyword` (lowercased, null-safe, title OR description) | `DefaultTaskServiceTest` title/description/no-match/null-description |
| Blank keyword disables search | keyword trimmed; empty => no search filter | `DefaultTaskServiceTest` blank keyword returns all |
| Default filter = ALL | `@RequestParam(defaultValue="ALL")` + `null` status => all | `TaskControllerTest` default test |
| Preserve UI state via query params | `tasks.html` filter links carry `filter`/`keyword`; hidden `filter` in search form; model echoes `filter`/`keyword` | `TaskControllerTest` model attribute assertions |
| Persistence unchanged; filter on sorted list | Service filters list returned by `FileTaskRepository.findByUsername` (sorted) | Code review; repository untouched |
| Service-layer interface extension | `TaskService.getFilteredTasks(...)` | Compilation + `DefaultTaskServiceTest` |

## Outstanding Notes
- **Invalid `filter` value handling** was left as an open question in the design. Implemented as graceful defaulting to `ALL` (consistent with Spring MVC `defaultValue` semantics and non-breaking for existing users). Revisit if product prefers a 400/validation response.
- A **pre-existing base-branch compilation defect** (missing `FileStorageManager`) was fixed minimally to allow the build/tests to run. The added class intentionally uses standard `UncheckedIOException` to avoid coupling to unrelated ticket (EPMCDMETST-51892) error-handling work; if that ticket merges first, consider reconciling to its `StorageException`-based version.
- `.gitignore` was adjusted to anchor the runtime `storage/` ignore rule to the repo root so the source package is tracked. Runtime persistence directory remains ignored.
