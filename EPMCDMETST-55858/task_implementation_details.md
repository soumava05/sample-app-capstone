## Planning Summary
This plan translates the design artifacts for Jira **EPMCDMETST-55858** (Todo app: add Open/Completed filter and sort by planned finish date) to an execution-ready, dependency-ordered task list.

Scope per design:
- Add optional query parameters to `GET /tasks` to filter by status (`ALL|OPEN|COMPLETED`) and sort by `plannedFinishDate` (ascending/descending).
- Implement filter/sort in the **service layer** while preserving user scoping and existing create/complete flows.
- Update Thymeleaf UI to show controls and the active selections.
- Add/extend unit tests for service and controller behavior.

## Inputs Used
Design and repo artifacts used to derive tasks:
- `EPMMCDMETST-55858/sample_todo_app_details.md`
  - Requirements (functional + non-functional), defaults, risks, test strategy
- `EPMCDMETST1-55858/solution-diagrams.md`
  - High-level and low-level design, API/view contract, error handling, r