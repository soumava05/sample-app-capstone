### Final Test Cases for EDMCDMETST-55979

### Understanding Summary
- **Feature**: Task dashboard filtering on `/tasks` for authenticated users.
- **Scope**: Server-side filtering by status (`ALL`, `OPEN`, `COMPLETED`) and inclusive date range (`fromDate`, `toDate`), value preservation after apply, reset action, and invalid date-range handling.
- **Key Flows**:
  - View all tasks by default.
  - Filter by status only.
  - Filter by date range only.
  - Filter by combined status and date range.
  - Reset filters to return to full task list.
  - Show validation feedback when `fromDate > toDate`.
- **Edge Cases**:
  - Blank status should behave as `ALL`.
  - Only `fromDate` supplied.
  - Only `toDate` supplied.
  - No matching tasks.
  - Invalid range should preserve entered values and show error.
  - Filtering must stay scoped to authenticated user only.
- **Assumptions**:
  - Test data exists or can be created through UI for the logged-in user.
  - Date filtering is based on displayed task date.
  - Empty state text reflects filter results.

### Proposed Coverage
- **Functional**:
  - Default dashboard load.
  - Status filter application.
  - Date filter application.
  - Combined filter application.
  - Reset action.
- **Validation**:
  - Invalid date range error.
  - Filter values preserved after submit.
- **Negative**:
  - No tasks matching filters.
  - Invalid range fallback behavior.
- **Edge Cases**:
  - Boundary inclusion on from/to date.
  - Blank status treated as ALL.
- **Integration**:
  - UI-to-controller query parameter flow.
  - Controller-to-service filtered response reflected in UI.

### Sample Gherkin
```gherkin
Feature: Task dashboard filtering

  Scenario: Filter tasks by status and date range
    Given the authenticated user is on the tasks dashboard
    And the user has tasks across different statuses and dates
    When the user selects status as "OPEN"
    And enters a valid from and to date range
    And clicks "Apply Filters"
    Then only matching open tasks within the inclusive date range are displayed
    And the selected status and dates remain populated
```

Feature: Task Dashboard Filtering

  @TestCaseID: TC_EDMCDMETST_55979_001
  @Title: Verify tasks dashboard shows all tasks by default with reset control visible
  @Preconditions:
    - User is authenticated successfully
    - User has at least one open task and one completed task

  Scenario: Default dashboard load without filters
    Given the authenticated user navigates to the tasks dashboard
    When the dashboard loads without any filter query parameters
    Then all tasks belonging to the authenticated user are displayed
    And the status filter is set to "ALL"
    And the from date field is empty
    And the to date field is empty
    And the reset action is visible

  @TestCaseID: TC_EDMCDMETST_55979_002
  @Title: Verify user can filter tasks by OPEN status
  @Preconditions:
    - User is authenticated successfully
    - User has both OPEN and COMPLETED tasks

  Scenario: Apply OPEN status filter
    Given the authenticated user is on the tasks dashboard
    When the user selects status as "OPEN"
    And clicks "Apply Filters"
    Then only tasks with status "OPEN" are displayed
    And no task with status "COMPLETED" is displayed
    And the selected status remains "OPEN"

  @TestCaseID: TC_EDMCDMETST_55979_003
  @Title: Verify user can filter tasks by COMPLETED status
  @Preconditions:
    - User is authenticated successfully
    - User has both OPEN and COMPLETED tasks

  Scenario: Apply COMPLETED status filter
    Given the authenticated user is on the tasks dashboard
    When the user selects status as "COMPLETED"
    And clicks "Apply Filters"
    Then only tasks with status "COMPLETED" are displayed
    And no task with status "OPEN" is displayed
    And the selected status remains "COMPLETED"

  @TestCaseID: TC_EDMCDMETST_55979_004
  @Title: Verify user can filter tasks using only from date inclusively
  @Preconditions:
    - User is authenticated successfully
    - User has tasks on multiple dates including the boundary date

  Scenario: Apply from date filter only
    Given the authenticated user is on the tasks dashboard
    When the user enters a from date
    And leaves the to date empty
    And clicks "Apply Filters"
    Then only tasks with task date on or after the selected from date are displayed
    And tasks before the selected from date are not displayed
    And the from date remains populated

  @TestCaseID: TC_EDMCDMETST_55979_005
  @Title: Verify user can filter tasks using only to date inclusively
  @Preconditions:
    - User is authenticated successfully
    - User has tasks on multiple dates including the boundary date

  Scenario: Apply to date filter only
    Given the authenticated user is on the tasks dashboard
    When the user enters a to date
    And leaves the from date empty
    And clicks "Apply Filters"
    Then only tasks with task date on or before the selected to date are displayed
    And tasks after the selected to date are not displayed
    And the to date remains populated

  @TestCaseID: TC_EDMCDMETST_55979_006
  @Title: Verify user can filter tasks using inclusive from and to date range
  @Preconditions:
    - User is authenticated successfully
    - User has tasks before, on, within, and after the selected date range

  Scenario: Apply valid inclusive date range filter
    Given the authenticated user is on the tasks dashboard
    When the user enters valid from and to dates
    And clicks "Apply Filters"
    Then only tasks with task date between the selected dates inclusive are displayed
    And tasks on the exact boundary dates are displayed
    And both date fields remain populated

  @TestCaseID: TC_EDMCDMETST_55979_007
  @Title: Verify user can filter tasks using combined status and date range
  @Preconditions:
    - User is authenticated successfully
    - User has tasks across multiple dates and statuses

  Scenario: Apply combined OPEN status and date range filters
    Given the authenticated user is on the tasks dashboard
    When the user selects status as "OPEN"
    And enters valid from and to dates
    And clicks "Apply Filters"
    Then only OPEN tasks within the selected inclusive date range are displayed
    And tasks outside the date range are not displayed
    And COMPLETED tasks are not displayed
    And the selected status and dates remain populated

  @TestCaseID: TC_EDMCDMETST_55979_008
  @Title: Verify reset action clears applied filters and restores full task list
  @Preconditions:
    - User is authenticated successfully
    - Filters are currently applied on the tasks dashboard

  Scenario: Reset applied filters
    Given the authenticated user has applied task filters on the dashboard
    When the user clicks the reset action
    Then the user is returned to the tasks dashboard without filter query parameters
    And all tasks belonging to the authenticated user are displayed
    And the status filter is set to "ALL"
    And the from date field is empty
    And the to date field is empty

  @TestCaseID: TC_EDMCDMETST_55979_009
  @Title: Verify invalid date range shows validation error and preserves entered values
  @Preconditions:
    - User is authenticated successfully
    - User is on the tasks dashboard

  Scenario: Apply invalid date range where from date is after to date
    Given the authenticated user is on the tasks dashboard
    When the user selects status as "OPEN"
    And enters a from date later than the to date
    And clicks "Apply Filters"
    Then an error message "From date cannot be after to date" is displayed
    And the selected status remains "OPEN"
    And the entered from date remains populated
    And the entered to date remains populated
    And the dashboard remains accessible

  @TestCaseID: TC_EDMCDMETST_55979_010
  @Title: Verify empty-state message is shown when no tasks match applied filters
  @Preconditions:
    - User is authenticated successfully
    - User has tasks, but none match the chosen filter combination

  Scenario: Apply filters with no matching tasks
    Given the authenticated user is on the tasks dashboard
    When the user applies filter criteria that match no tasks
    Then no task cards are displayed
    And the empty-state message "No tasks match the selected filters." is shown
    And the applied filter values remain populated

Proceed to automation
