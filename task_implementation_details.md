## Planning Summary
Implement **Todo Dashboard task list filtering (All/Open/Completed)** and **keyword search (title+description, case-insensitive)** for Jira **EPMCDMETST-52763** in the existing Spring Boot + Thymeleaf MVC app. Approach is explicitly **service-layer in-memory filtering** after loading the already-sorted task list from the file-based repository; add **query parameters** to `GET /tasks`, update the Thymeleaf UI to preserve state via URL params, and add/extend unit and controller tests.

## Inputs Used
- Repo: https://github.com/soumava05/sample-app-capstone
- Branch analyzed (matches Jira): `EPMCDMETST-52763`
- Design docs read under repo folder `EPMCDMETST-52763/`:
  - `EPMCDMETST-52763/sample_todo_app_details.md`
  - `EPMCDMETST-52763/solution-diagrams.md` (Mermaid diagrams: HLD, LLD, Component, Sequence, Data Flow, Deployment)
- Root `README.md` (architecture overview and storage layout)

## Priority-Ordered Task List

### Prerequisites
1) **Locate and inventory current task dashboard flow**
- **Purpose:** Confirm current `GET /tasks` controller method signature, model attributes, and how tasks are fetched/sorted today.
- **Dependencies:** None.