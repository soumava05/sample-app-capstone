### Understanding Summary
- Feature: Server-side search and filtering for authenticated user Todo Dashboard (`GET /tasks`).
- Jira Ticket: `EPMCDMETST-55879`
- Scope: Validate keyword, status, date-range filtering, clear-filters behavior, invalid range handling, user scoping, and non-regression of create/complete flows.
- Key Flows:
  - Authenticated user opens `/tasks` and sees own task list.
  - User applies keyword/status/date filters and receives filtered results.
  - User clears filters and returns to full task list.
  - User enters invalid date range and sees inline validation without crash.
- Edge Cases:
  - Blank keyword.
  - Status blank / ALL.
  - Inclusive date boundaries.
  - No matching tasks.
  - User isolation.
- Assumptions:
  - Task data exists for the authenticated user or can be created during test setup.
  - Status values supported by UI are `OPEN`, `COMPLETED`, and blank=`ALL`.
  - Invalid date-range means `dateFrom > dateTo`.

### Proposed Coverage
- Functional
  - Render filter panel on dashboard.
  - Filter by keyword in title.
  - Filter by keyword in description.
  - Filter by status OPEN.
  - Filter by status COMPLETED.
  - Filter by dateFrom/dateTo inclusive.
  - Combine filters with AND semantics.
  - Clear filters restores full list.
- Validation
  - Invalid date range shows message.
  - Filter values persist after apply.
- Negative
  - No match returns empty-state message.
  - Blank/ALL status behaves as unfiltered.
- Edge Cases
  - Keyword case-insensitivity.
  - Boundary dates included.
- Integration
  - Dashboard filtering does not break create task and mark completed flows.
  - Filtering remains scoped to authenticated user only.

### Sample Gherkin
Feature: Todo dashboard search and filtering

  Scenario: Filter tasks by keyword and status
    Given an authenticated user has multiple tasks
    When the user applies keyword and status filters on the dashboard
    Then only tasks matching all selected criteria are displayed

  Scenario: Invalid date range shows validation
    Given an authenticated user is on the dashboard
    When the user sets Date From later than Date To and applies filters
    Then the dashboard remains open
    And an inline filter validation message is displayed

### Final Test Cases for EPMCDMETST-55879

Feature: Todo dashboard search and filtering

  @TestCaseID: TC_EPMCDMETST-55879_001
  @Title: Verify filter section is displayed on task dashboard
  @Preconditions:
    - User is authenticated
    - User has access to `/tasks`
  Scenario: Display search and filter controls
    Given the authenticated user opens the task dashboard
    When the dashboard page is loaded
    Then the user should see the keyword field
    And the user should see the status dropdown
    And the user should see dateFrom and dateTo fields
    And the user should see Apply Filters and Clear Filters actions

  @TestCaseID: TC_EPMCDMETST-55879_002
  @Title: Verify keyword filter matches task title case-insensitively
  @Preconditions:
    - User is authenticated
    - User has at least one task whose title contains the target keyword
  Scenario: Filter by title keyword
    Given the user is on the task dashboard
    When the user enters a keyword that matches a task title with different letter case
    And applies filters
    Then only tasks containing that keyword in title should be displayed

  @TestCaseID: TC_EPMCDMETST-55879_003
  @Title: Verify keyword filter matches task description case-insensitively
  @Preconditions:
    - User is authenticated
    - User has at least one task whose description contains the target keyword
  Scenario: Filter by description keyword
    Given the user is on the task dashboard
    When the user enters a keyword that matches a task description with different letter case
    And applies filters
    Then only tasks containing that keyword in description should be displayed

  @TestCaseID: TC_EPMCDMETST-55879_004
  @Title: Verify OPEN status filter returns only open tasks
  @Preconditions:
    - User is authenticated
    - User has open and completed tasks
  Scenario: Filter by OPEN status
    Given the user is on the task dashboard
    When the user selects OPEN in status filter
    And applies filters
    Then only tasks with OPEN status should be displayed

  @TestCaseID: TC_EPMCDMETST-55879_005
  @Title: Verify COMPLETED status filter returns only completed tasks
  @Preconditions:
    - User is authenticated
    - User has open and completed tasks
  Scenario: Filter by COMPLETED status
    Given the user is on the task dashboard
    When the user selects COMPLETED in status filter
    And applies filters
    Then only tasks with COMPLETED status should be displayed

  @TestCaseID: TC_EPMCDMETST-55879_006
  @Title: Verify date range filter includes boundary dates
  @Preconditions:
    - User is authenticated
    - User has tasks on boundary and outside-boundary dates
  Scenario: Filter by inclusive date range
    Given the user is on the task dashboard
    When the user enters Date From and Date To
    And applies filters
    Then tasks whose task date is equal to Date From should be displayed
    And tasks whose task date is equal to Date To should be displayed
    And tasks outside the range should not be displayed

  @TestCaseID: TC_EPMCDMETST-55879_007
  @Title: Verify combined filters use AND semantics
  @Preconditions:
    - User is authenticated
    - User has multiple tasks partially matching different criteria
  Scenario: Apply combined keyword, status, and date filters
    Given the user is on the task dashboard
    When the user enters a keyword
    And selects a status
    And enters a valid date range
    And applies filters
    Then only tasks matching all selected criteria should be displayed

  @TestCaseID: TC_EPMCDMETST-55879_008
  @Title: Verify clear filters restores full task list
  @Preconditions:
    - User is authenticated
    - User previously applied one or more filters
  Scenario: Clear applied filters
    Given filtered results are displayed on the dashboard
    When the user clicks Clear Filters
    Then the dashboard should reload without filter criteria
    And the full task list for the authenticated user should be displayed

  @TestCaseID: TC_EPMCDMETST-55879_009
  @Title: Verify invalid date range shows inline validation and does not crash
  @Preconditions:
    - User is authenticated
  Scenario: Apply invalid date range
    Given the user is on the task dashboard
    When the user enters Date From later than Date To
    And applies filters
    Then the task dashboard should remain displayed
    And an inline filter error message should be shown
    And the application should not crash

  @TestCaseID: TC_EPMCDMETST-55879_010
  @Title: Verify no-match filter result shows empty-state message
  @Preconditions:
    - User is authenticated
  Scenario: Apply filters with no matching tasks
    Given the user is on the task dashboard
    When the user applies filter criteria that match no tasks
    Then no task cards should be displayed
    And the empty-state message should be shown

  @TestCaseID: TC_EPMCDMETST-55879_011
  @Title: Verify blank status or ALL behaves as no status filter
  @Preconditions:
    - User is authenticated
    - User has multiple tasks with different statuses
  Scenario: Apply no status-specific filter
    Given the user is on the task dashboard
    When the user leaves status unselected or chooses ALL
    And applies filters
    Then tasks should not be restricted by status

  @TestCaseID: TC_EPMCDMETST-55879_012
  @Title: Verify filtering never shows another user’s tasks
  @Preconditions:
    - Two different users exist with different tasks
    - Test user is authenticated
  Scenario: Apply filters within authenticated user scope
    Given the authenticated user is on the task dashboard
    When the user applies any supported filters
    Then only tasks belonging to the authenticated user should be displayed

  @TestCaseID: TC_EPMCDMETST-55879_013
  @Title: Verify create task flow remains functional after filter enhancement
  @Preconditions:
    - User is authenticated
  Scenario: Create task from dashboard with filter section present
    Given the user is on the task dashboard
    When the user submits valid task details in the create task form
    Then the task should be created successfully
    And the user should be redirected to `/tasks`

  @TestCaseID: TC_EPMCDMETST-55879_014
  @Title: Verify mark completed flow remains functional after filter enhancement
  @Preconditions:
    - User is authenticated
    - User has at least one OPEN task
  Scenario: Mark task as completed from dashboard
    Given the user is on the task dashboard
    When the user clicks Mark Completed for an OPEN task
    Then the task status should be updated to COMPLETED
    And the user should be redirected to `/tasks`

  @TestCaseID: TC_EPMCDMETST-55879_015
  @Title: Verify filter values are preserved after applying filters
  @Preconditions:
    - User is authenticated
  Scenario: Preserve filter values on dashboard
    Given the user is on the task dashboard
    When the user enters keyword, status and date values
    And applies filters
    Then the same filter values should remain populated on the dashboard

Proceed to automation

### Diff Review Notes
- Implemented coverage observed in code:
  - DTO for filter binding: `TaskFilterForm`
  - Service overload for filtered task retrieval
  - Filtering by keyword/status/date with AND semantics
  - Inclusive date validation and invalid range exception path
  - Controller inline error handling with fallback unfiltered list
  - UI filter form and clear filters action
  - Unit tests for DTO/service/controller behavior
- Coverage gap found against requirement wording:
  - UI empty-state text was changed globally to `No tasks found for the selected filters.` This can be misleading for first-time/no-task dashboard load because it no longer distinguishes between truly empty dashboard and filtered no-result state.
- Additional diff note:
  - `FileStorageManager` was added in PR diff but appears unrelated to the stated filtering requirement and is not covered by new tests in this ticket scope.
