## Planning Summary
This implementation plan derives tasks from the design artifacts for **EPMCDMETST-55645** (sample_todo_app). The system is a **server-rendered Spring Boot (Java 21) monolithic Todo app** using **Spring MVC + Thymeleaf**, **Spring Security form login**, **BCrypt** password hashing, and **local JSON file persistence** for users and per-user tasks.

The design documents describe the current intended architecture (layered packages: web/controllers, dto, service, repository, domain, config), key user flows (register → login → dashboard; create task; mark complete), and storage layout under `storage/`.

This plan focuses on converting the described architecture and flows into an execution-ready, dependency-ordered task list (prereqs → feature work → hardening/cleanup), with explicit verification notes and flagged gaps.

## Inputs Used
1. **Git repository**: https://github.com/soumava05/sample-app-capstone
   - Branch analyzed for ticket: `EPMCDMETST-55645`
2. **Repo design artifacts (folder `EPMCDMETST-55645/`)**:
   - `EPMCDMETST-55645/sample_todo_app_details.md`
   - `EPMCDMETST-55645/solution-diagrams.md` (pointer to Confluence)
3. **Confluence design page (canonical diagrams)**:
   - https://epam-team-js1kicba.atlassian.net/wiki/spaces/EliteA/pages/23363586/Design+Diagram+-+EPMCDMETST-55645
   - Extracted diagrams/sections: Architecture diagram, Component (C4) diagram, Sequence diagram (Create Task), Deployment diagram, Data Flow diagram, Wireframes/User flow.
4. **Codebase context (structure observed in repository tree)**:
   - `src/main/java/com/capstone/todo/{config,domain,dto,repository,service,web}`
   - `src/main/resources/{application.yml,templates/*.html,static/css/styles.css}`

## Priority-Ordered Task List
### Prerequisites
**T1. Confirm runtime and build prerequisites (Java/Maven/Spring Boot versions)**
- **Purpose**: Ensure the implementation matches the design’s stack (Java 21, Spring Boot 3.3.x, Maven) and can be built/tested consistently.
- **Dependencies**: None.
- **Verification intent**: `mvn -v` shows compatible Maven; project builds successfully via `mvn test`.

**T2. Establish/verify local storage directory contract (`storage/` layout and permissions)**
- **Purpose**: Align file persistence with the documented paths: `storage/users.json` and `storage/tasks/{username}.json`.
- **Dependencies**: T1.
- **Verification intent**: Application can start with clean workspace; storage directory is created or validated; read/write permissions confirmed.

### Feature Work
**T3. Implement user registration flow (MVC + DTO validation + service + repository write)**
- **Purpose**: Deliver “register” feature described in user flow and component diagrams.
- **Design-driven scope**:
  - `AuthController` renders `register.html` and accepts registration POST.
  - `RegistrationForm` DTO validation.
  - `DefaultUserService` hashes password (BCrypt) and persists user.
  - `FileUserRepository` writes to `storage/users.json`.
- **Dependencies**: T1, T2.
- **Verification intent**: Registering a new user persists in `storage/users.json`; duplicate username behavior matches documented constraints (see Open Questions if unspecified).

**T4. Implement form login authentication with Spring Security (public auth pages + protected tasks)**
- **Purpose**: Enforce authenticated access per design: auth pages permitted; `/tasks` requires login.
- **Design-driven scope**:
  - `SecurityConfig` provides filter chain, login page, password encoder, and user details lookup.
  - User data source is file-backed repository.
- **Dependencies**: T3 (needs persisted users to authenticate).
- **Verification intent**: Anonymous access to `/tasks` redirects to login; valid credentials allow access; invalid credentials rejected.

**T5. Implement tasks dashboard (list user-scoped tasks via TaskController → service → repository)**
- **Purpose**: Provide the main “Todo Dashboard” page for an authenticated user.
- **Design-driven scope**:
  - `TaskController` renders `tasks.html` with tasks for current authenticated username.
  - `DefaultTaskService` loads tasks.
  - `FileTaskRepository` reads from `storage/tasks/{username}.json`.
- **Dependencies**: T4, T2.
- **Verification intent**: Logged-in user sees only their own tasks; no cross-user leakage.

**T6. Implement create-task flow (POST /tasks) including business validation described in sequence diagram**
- **Purpose**: Implement the sequence diagram “Create task” flow: controller receives form, service normalizes username and validates dates, repository persists to per-user file.
- **Design-driven scope**:
  - `TaskForm` DTO validation.
  - Service-level validation for date rules (explicit in sequence diagram).
  - Persist new task in OPEN state.
- **Dependencies**: T5.
- **Verification intent**:
  - Valid task creates/updates `storage/tasks/{username}.json`.
  - Invalid dates lead to an error response rendering tasks with an error (as shown in sequence diagram alt path).

**T7. Implement mark-task-completed flow (user marks tasks completed on dashboard)**
- **Purpose**: Deliver “mark tasks completed” feature from key findings and user flow diagram.
- **Design-driven scope**:
  - Controller endpoint to complete a task.
  - Service updates status to COMPLETED.
  - Repository updates the per-user JSON.
- **Dependencies**: T5 (dashboard exists), T6 (task creation exists).
- **Verification intent**: Completing a task changes its status and persists; dashboard reflects the updated status.

**T8. Implement root routing behavior (`/` redirects to `/tasks`)**
- **Purpose**: Match user flow: landing at `/` redirects to `/tasks`.
- **Dependencies**: T4.
- **Verification intent**: Visiting `/` results in redirect chain to login when unauthenticated, or dashboard when authenticated.

### Hardening / Cleanup
**T9. Add/confirm consistent JSON serialization format (Jackson configuration)**
- **Purpose**: Ensure file persistence is stable across runs per design’s inclusion of `JacksonConfig`.
- **Dependencies**: T3, T6, T7.
- **Verification intent**: Users and tasks JSON files are written in expected format; reading back yields correct domain objects; no serialization regressions.

**T10. Add defensive file I/O and concurrency handling for local JSON repositories**
- **Purpose**: Reduce risk of corrupted JSON from partial writes and concurrent access (single-node, but multiple requests can overlap).
- **Dependencies**: T3, T6, T7.
- **Verification intent**: Repository writes are atomic (e.g., temp file + move) or otherwise protected; concurrent create/complete requests do not corrupt JSON.

**T11. Add automated tests aligned to the design-critical flows**
- **Purpose**: Validate the designed behavior (auth gating, per-user scoping, create/complete task flows).
- **Dependencies**: T3–T8.
- **Verification intent**:
  - Unit tests for `DefaultUserService` (hashing, uniqueness constraints as specified).
  - Unit tests for `DefaultTaskService` (date validation).
  - Repository tests for file read/write behavior.
  - Web/security integration tests (redirects and access control).

**T12. Document operational constraints and rollout expectations (single-node file storage)**
- **Purpose**: Make explicit the design’s limitation: local JSON persistence is suitable for sample/single-node use.
- **Dependencies**: T2, T10.
- **Verification intent**: README or ticket documentation includes storage layout, backup/cleanup notes, and scaling limitation.

## Dependency Ordering
- **Phase 0 (Prereqs)**: T1 → T2
- **Phase 1 (Auth foundation)**: T3 → T4
- **Phase 2 (Tasks features)**: T5 → T6 → T7
- **Phase 3 (Routing polish)**: T8 (after T4)
- **Phase 4 (Hardening/testing/docs)**:
  - T9 (after T3/T6/T7)
  - T10 (after T3/T6/T7)
  - T11 (after T3–T8)
  - T12 (after T2/T10)

**Parallelization opportunities (after prerequisites)**
- After **T4**, **T8** can be done in parallel with **T5**.
- After **T6**, **T9** and **T10** can begin in parallel (both depend on repositories/services being present).
- **T12** can start once storage contract is clear (T2), but should be finalized after hardening decisions (T10).

## Blocked Tasks
**B1. Define the exact validation rules for task “date validation”**
- **Why blocked**: The sequence diagram states “validate dates” but does not specify rules (e.g., dueDate required?, must be >= today?, start/end relationship?).
- **Impact**: T6 and related tests (T11) cannot be fully specified without exact criteria.

**B2. Define user uniqueness and error UX for duplicate usernames / weak passwords**
- **Why blocked**: Design mentions registration and BCrypt, but does not define constraints (min length, complexity, username normalization, duplicate handling, error rendering).
- **Impact**: T3 and T11 acceptance criteria are ambiguous.

**B3. Define task identity for “mark completed” operation**
- **Why blocked**: Design does not define whether tasks are identified by ID, index, title+date, etc., and how completion is addressed safely.
- **Impact**: T7 endpoint contract and repository update logic cannot be pinned down without the identifier strategy.

## Verification Notes
- **Security verification** should include:
  - Unauthenticated request to `/tasks` redirects to `/login`.
  - Auth pages (login/register) remain accessible to anonymous users.
  - Session-based authentication behaves as expected in browser.
- **Data correctness verification** should include:
  - No cross-user access: one user cannot read/write another user’s tasks file.
  - JSON persistence remains valid across restarts.
- **Flow verification** should align to Confluence sequence diagram:
  - For invalid inputs: controller renders tasks page with error state (not a raw stack trace).
  - For valid inputs: redirects back to `/tasks`.

## Risks and Execution Notes
- **Local file persistence risk**: concurrent writes can corrupt JSON unless atomic-write strategy is used (addressed in T10).
- **Path traversal / unsafe usernames**: using `{username}.json` requires sanitization/normalization to prevent creating unintended paths.
- **Environment-dependent storage**: relative `storage/` location and runtime permissions may differ in container/CI.
- **Auth coupling**: Spring Security `UserDetailsService` backed by file repository can become a bottleneck; acceptable for sample app.

## Assumptions and Open Questions
(These are not treated as facts; answers are required to unblock items.)
1. What are the exact **task date** fields and validation rules (required/optional, allowed ranges)?
2. How are tasks uniquely identified for completion (UUID, incremental ID, array index, composite key)?
3. What are the **registration constraints** (username normalization, password policy, duplicate handling UX)?
4. Should completed tasks be hidden, shown, or filtered on the dashboard?
5. Are there any NFR expectations (e.g., max tasks per user, file size limits) for this capstone?

## Requirement Traceability
Source-to-task mapping based on provided design artifacts:
- **Register** (Key Findings + user flow + component diagram): T3
- **Login/auth + protected routes** (High level design + architecture diagram): T4
- **View user-scoped tasks** (Key Findings + data flow diagram): T5
- **Create tasks** (Key Findings + sequence diagram): T6
- **Mark completed** (Key Findings + user flow): T7
- **Landing redirect `/` → `/tasks`** (wireframes/user flow): T8
- **Local JSON persistence** (high level design + architecture diagram + deployment diagram): T2, T9, T10
- **Testing approach referenced in stack** (details doc mentions TestNG/Mockito): T11
