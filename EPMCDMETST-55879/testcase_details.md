### Final Test Cases for EPMCDMETST-55879

Feature: Todo Dashboard search and filter

  @TestCaseID: TC_EPMCDMETST-55879_001
  @Title: Verify filter controls are visible on task dashboard
  @Preconditions:
    - User is authenticated
    - User is on /tasks

  Scenario: Display search and filter controls
    Given the authenticated user opens the tasks dashboard
    Then the keyword search input is visible
    And the status filter dropdown is visible
    And the Task Date From field is visible
    And the Task Date To field is visible
    And the Apply Filters button is visible
    And the Clear Filters action is visible

  @TestCaseID: TC_EPMCDMETST-55879_002
  @Title: Verify keyword filter searches title case-insensitively
  @Preconditions:
    - User is authenticated
    - User has at least one matching task and one non-matching task

  Scenario: Filter by keyword in title
    Given the user has tasks with distinct titles
    When the user enters a keyword matching part of a task title in different letter case
    And submits the filters
    Then only tasks whose title contains the keyword are displayed

  @TestCaseID: TC_EPMCDMETST-55879_003
  @Title: Verify keyword filter searches description case-insensitively
  @Preconditions:
    - User is authenticated
    - User has tasks with distinct descriptions

  Scenario: Filter by keyword in description
    Given the user has tasks with distinct descriptions
    When the user enters a keyword matching part of a task description in different letter case
    And submits the filters
    Then only tasks whose description contains the keyword are displayed

  @TestCaseID: TC_EPMCDMETST-55879_004
  @Title: Verify OPEN status filter returns only open tasks
  @Preconditions:
    - User is authenticated
    - User has OPEN and COMPLETED tasks

  Scenario: Filter by OPEN status
    Given the user has open and completed tasks
    When the user selects status OPEN
    And submits the filters
    Then only OPEN tasks are displayed

  @TestCaseID: TC_EPMCDMETST-55879_005
  @Title: Verify COMPLETED status filter returns only completed tasks
  @Preconditions:
    - User is authenticated
    - User has OPEN and COMPLETED tasks

  Scenario: Filter by COMPLETED status
    Given the user has open and completed tasks
    When the user selects status COMPLETED
    And submits the filters
    Then only COMPLETED tasks are displayed

  @TestCaseID: TC_EPMCDMETST-55879_006
  @Title: Verify ALL status behaves as no status filter
  @Preconditions:
    - User is authenticated
    - User has multiple tasks with mixed statuses

  Scenario: Filter by ALL status
    Given the user has multiple tasks
    When the user selects status ALL
    And submits the filters
    Then tasks are not reduced by status selection

  @TestCaseID: TC_EPMCDMETST-55879_007
  @Title: Verify combined keyword and status filters use AND semantics
  @Preconditions:
    - User is authenticated
    - User has tasks with overlapping and non-overlapping attributes

  Scenario: Combine keyword and status filters
    Given the user has tasks with different titles, descriptions, and statuses
    When the user enters a valid keyword
    And selects a status
    And submits the filters
    Then only tasks matching both conditions are displayed

  @TestCaseID: TC_EPMCDMETST-55879_008
  @Title: Verify inclusive date range filtering
  @Preconditions:
    - User is authenticated
    - User has tasks on boundary and out-of-range dates

  Scenario: Filter by inclusive date range
    Given the user has tasks across multiple task dates
    When the user enters Task Date From and Task Date To
    And submits the filters
    Then tasks on the from date are displayed
    And tasks on the to date are displayed
    And tasks outside the range are not displayed

  @TestCaseID: TC_EPMCDMETST-55879_009
  @Title: Verify invalid date range shows validation message
  @Preconditions:
    - User is authenticated
    - User is on /tasks

  Scenario: Date From later than Date To
    Given the user is on the tasks dashboard
    When the user enters a Task Date From value later than Task Date To
    And submits the filters
    Then a user-visible validation message is shown
    And the application remains responsive
    And the task list fallback is still shown

  @TestCaseID: TC_EPMCDMETST-55879_010
  @Title: Verify clear filters restores default task list
  @Preconditions:
    - User is authenticated
    - User previously applied one or more filters

  Scenario: Clear applied filters
    Given filters are currently applied on the tasks dashboard
    When the user clicks Clear Filters
    Then all filter fields are reset
    And the full task list for the user is displayed

  @TestCaseID: TC_EPMCDMETST-55879_011
  @Title: Verify filter values are preserved after submission
  @Preconditions:
    - User is authenticated
    - User applies one or more valid filters

  Scenario: Preserve entered filter values
    Given the user enters filter criteria
    When the user submits the filters
    Then the entered keyword remains populated
    And the selected status remains selected
    And the entered date values remain populated

  @TestCaseID: TC_EPMCDMETST-55879_012
  @Title: Verify create-task flow continues to work with filter feature present
  @Preconditions:
    - User is authenticated
    - User is on /tasks

  Scenario: Create task after filters are available
    Given the user is on the tasks dashboard
    When the user creates a valid task
    Then the task is created successfully
    And the dashboard remains functional

  @TestCaseID: TC_EPMCDMETST-55879_013
  @Title: Verify mark-completed flow continues to work with filter feature present
  @Preconditions:
    - User is authenticated
    - User has at least one OPEN task

  Scenario: Mark task completed and filter by completed
    Given the user has an open task
    When the user marks the task as completed
    And filters by COMPLETED status
    Then the completed task is displayed

  @TestCaseID: TC_EPMCDMETST-55879_014
  @Title: Verify no matching filters display empty state message
  @Preconditions:
    - User is authenticated
    - User has at least one task

  Scenario: Show empty state for no matching results
    Given the user has one or more tasks
    When the user applies filter criteria that match no tasks
    Then the empty state message is displayed
    And no task cards are shown

Proceed to automation
