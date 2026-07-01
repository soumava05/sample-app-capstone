# Test Cases for EPMCDMETST-52763
## Todo Dashboard: Task List Filters and Keyword Search

---

## Feature Overview
**Jira Ticket:** EPMCDMETST-52763  
**Feature:** Todo Dashboard - Add task list filters (All/Open/Completed) and keyword search  
**Application:** Todo Dashboard (Spring Boot + Thymeleaf)  
**URL:** `/tasks`

---

## Test Cases

```gherkin
Feature: Todo Dashboard Task List Filters and Keyword Search
  As an authenticated user
  I want to filter my task list by status and search by keyword
  So that I can quickly find relevant tasks when the list grows

  Background:
    Given the user is logged into the Todo application
    And the user navigates to the "/tasks" page

  # ============================================================
  # FILTER FUNCTIONALITY - STATUS FILTERS
  # ============================================================

  @TestCaseID: TC_EPMCDMETST-52763_001
  @Title: Verify default filter shows all tasks
  @Priority: High
  @Category: Functional - Filter
  @Preconditions:
    - User is authenticated
    - User has at least one OPEN task and one COMPLETED task

  Scenario: Default view displays all tasks regardless of status
    Given the user has tasks with both "OPEN" and "COMPLETED" statuses
    When the user opens the tasks page without any filter parameters
    Then all tasks belonging to the user should be displayed
    And the "All" filter option should be selected by default
    And tasks should be sorted by taskDate then createdAt

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_002
  @Title: Filter tasks by Open status
  @Priority: High
  @Category: Functional - Filter
  @Preconditions:
    - User is authenticated
    - User has at least one OPEN task and one COMPLETED task

  Scenario: User filters tasks to show only Open tasks
    Given the user has tasks with both "OPEN" and "COMPLETED" statuses
    When the user selects the "Open" filter option
    Then only tasks with "OPEN" status should be displayed
    And tasks with "COMPLETED" status should not be visible
    And the URL should contain "filter=Open" query parameter

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_003
  @Title: Filter tasks by Completed status
  @Priority: High
  @Category: Functional - Filter
  @Preconditions:
    - User is authenticated
    - User has at least one OPEN task and one COMPLETED task

  Scenario: User filters tasks to show only Completed tasks
    Given the user has tasks with both "OPEN" and "COMPLETED" statuses
    When the user selects the "Completed" filter option
    Then only tasks with "COMPLETED" status should be displayed
    And tasks with "OPEN" status should not be visible
    And the URL should contain "filter=Completed" query parameter

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_004
  @Title: Switch from filtered view back to All tasks
  @Priority: High
  @Category: Functional - Filter
  @Preconditions:
    - User is authenticated
    - User has tasks with mixed statuses

  Scenario: User switches from Open filter back to All filter
    Given the user has the "Open" filter currently selected
    When the user selects the "All" filter option
    Then all tasks should be displayed regardless of status
    And the URL should reflect the "All" filter or have no filter parameter

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_005
  @Title: Filter shows empty state when no tasks match
  @Priority: Medium
  @Category: Negative - Filter
  @Preconditions:
    - User is authenticated
    - User has only OPEN tasks (no COMPLETED tasks)

  Scenario: Filter shows empty state when no Completed tasks exist
    Given the user has only tasks with "OPEN" status
    When the user selects the "Completed" filter option
    Then an empty state message should be displayed
    And the message should indicate no tasks match the filter

  # ============================================================
  # SEARCH FUNCTIONALITY - KEYWORD SEARCH
  # ============================================================

  @TestCaseID: TC_EPMCDMETST-52763_006
  @Title: Search tasks by keyword matching title
  @Priority: High
  @Category: Functional - Search
  @Preconditions:
    - User is authenticated
    - User has tasks with various titles

  Scenario: User searches and finds tasks by title keyword
    Given the user has a task with title "Team meeting notes"
    And the user has a task with title "Grocery shopping list"
    When the user enters "meeting" in the search field
    And the search is triggered
    Then only tasks containing "meeting" in the title should be displayed
    And the task "Team meeting notes" should be visible
    And the task "Grocery shopping list" should not be visible
    And the URL should contain "keyword=meeting" query parameter

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_007
  @Title: Search tasks by keyword matching description
  @Priority: High
  @Category: Functional - Search
  @Preconditions:
    - User is authenticated
    - User has tasks with various descriptions

  Scenario: User searches and finds tasks by description keyword
    Given the user has a task with title "Project Alpha" and description "Discuss budget allocation"
    And the user has a task with title "Project Beta" and description "Technical review"
    When the user enters "budget" in the search field
    And the search is triggered
    Then only tasks containing "budget" in the description should be displayed
    And the task "Project Alpha" should be visible
    And the task "Project Beta" should not be visible

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_008
  @Title: Search is case-insensitive
  @Priority: High
  @Category: Functional - Search
  @Preconditions:
    - User is authenticated
    - User has tasks with mixed case text

  Scenario: Search matches regardless of case
    Given the user has a task with title "URGENT Meeting"
    When the user enters "urgent" in the search field (lowercase)
    And the search is triggered
    Then the task "URGENT Meeting" should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_009
  @Title: Search with partial keyword match
  @Priority: High
  @Category: Functional - Search
  @Preconditions:
    - User is authenticated
    - User has tasks with searchable content

  Scenario: Search matches partial keywords
    Given the user has a task with title "Development sprint planning"
    When the user enters "sprint" in the search field
    And the search is triggered
    Then the task "Development sprint planning" should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_010
  @Title: Clear search returns to full list
  @Priority: High
  @Category: Functional - Search
  @Preconditions:
    - User is authenticated
    - User has search keyword currently applied

  Scenario: User clears search to see all tasks
    Given the user has a search keyword "meeting" applied
    And only matching tasks are displayed
    When the user clears the search field
    And the search is triggered or cleared
    Then all tasks should be displayed
    And the URL should not contain the keyword parameter

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_011
  @Title: Search shows empty state when no tasks match
  @Priority: Medium
  @Category: Negative - Search
  @Preconditions:
    - User is authenticated
    - User has tasks that do not match the search term

  Scenario: Search with no matching results shows empty state
    Given the user has tasks that do not contain "xyznonexistent"
    When the user enters "xyznonexistent" in the search field
    And the search is triggered
    Then an empty state message should be displayed
    And the message should indicate no tasks match the search

  # ============================================================
  # COMBINED FILTER AND SEARCH
  # ============================================================

  @TestCaseID: TC_EPMCDMETST-52763_012
  @Title: Apply both filter and search together
  @Priority: High
  @Category: Functional - Combined
  @Preconditions:
    - User is authenticated
    - User has multiple tasks with different statuses and titles

  Scenario: User applies both Open filter and keyword search
    Given the user has an OPEN task with title "Urgent code review"
    And the user has a COMPLETED task with title "Urgent deployment"
    And the user has an OPEN task with title "Weekly standup"
    When the user selects the "Open" filter option
    And the user enters "urgent" in the search field
    And the search is triggered
    Then only OPEN tasks containing "urgent" should be displayed
    And the task "Urgent code review" should be visible
    And the task "Urgent deployment" should not be visible
    And the task "Weekly standup" should not be visible
    And the URL should contain both "filter=Open" and "keyword=urgent"

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_013
  @Title: Clear search while filter remains active
  @Priority: Medium
  @Category: Functional - Combined
  @Preconditions:
    - User is authenticated
    - User has filter and search both applied

  Scenario: Clearing search keeps filter active
    Given the user has "Open" filter and "urgent" search applied
    When the user clears the search field
    And the search is triggered
    Then all OPEN tasks should be displayed
    And the "Open" filter should remain selected
    And COMPLETED tasks should still be hidden

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_014
  @Title: Change filter while search remains active
  @Priority: Medium
  @Category: Functional - Combined
  @Preconditions:
    - User is authenticated
    - User has filter and search both applied

  Scenario: Changing filter keeps search active
    Given the user has "Open" filter and "review" search applied
    When the user changes the filter to "Completed"
    Then only COMPLETED tasks containing "review" should be displayed
    And the search keyword should remain in the search field

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_015
  @Title: Combined filter and search with no results
  @Priority: Medium
  @Category: Negative - Combined
  @Preconditions:
    - User is authenticated
    - User has tasks but none match both filter and search

  Scenario: No results when filter and search combination has no matches
    Given the user has OPEN tasks but none containing "deployment"
    When the user selects the "Open" filter option
    And the user enters "deployment" in the search field
    And the search is triggered
    Then an empty state message should be displayed

  # ============================================================
  # URL PERSISTENCE AND REFRESH
  # ============================================================

  @TestCaseID: TC_EPMCDMETST-52763_016
  @Title: Filter selection persists after page refresh
  @Priority: High
  @Category: Functional - Persistence
  @Preconditions:
    - User is authenticated
    - User has tasks in the list

  Scenario: Selected filter is preserved after page refresh
    Given the user selects the "Open" filter option
    And the URL contains "filter=Open"
    When the user refreshes the page
    Then the "Open" filter should still be selected
    And only OPEN tasks should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_017
  @Title: Search keyword persists after page refresh
  @Priority: High
  @Category: Functional - Persistence
  @Preconditions:
    - User is authenticated
    - User has tasks in the list

  Scenario: Search keyword is preserved after page refresh
    Given the user enters "meeting" in the search field
    And the search is triggered
    And the URL contains "keyword=meeting"
    When the user refreshes the page
    Then the search field should contain "meeting"
    And only tasks matching "meeting" should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_018
  @Title: Direct URL navigation with filter parameter
  @Priority: High
  @Category: Functional - Persistence
  @Preconditions:
    - User is authenticated

  Scenario: User navigates directly to filtered URL
    When the user navigates directly to "/tasks?filter=Completed"
    Then the "Completed" filter should be selected
    And only COMPLETED tasks should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_019
  @Title: Direct URL navigation with search parameter
  @Priority: High
  @Category: Functional - Persistence
  @Preconditions:
    - User is authenticated

  Scenario: User navigates directly to URL with search keyword
    When the user navigates directly to "/tasks?keyword=project"
    Then the search field should contain "project"
    And only tasks matching "project" should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_020
  @Title: Direct URL navigation with both filter and search
  @Priority: High
  @Category: Functional - Persistence
  @Preconditions:
    - User is authenticated

  Scenario: User navigates directly to URL with filter and search
    When the user navigates directly to "/tasks?filter=Open&keyword=urgent"
    Then the "Open" filter should be selected
    And the search field should contain "urgent"
    And only OPEN tasks matching "urgent" should be displayed

  # ============================================================
  # EDGE CASES AND VALIDATION
  # ============================================================

  @TestCaseID: TC_EPMCDMETST-52763_021
  @Title: Search with empty keyword shows all tasks
  @Priority: Medium
  @Category: Edge Case - Search
  @Preconditions:
    - User is authenticated
    - User has tasks in the list

  Scenario: Empty search field shows all tasks
    Given the user has a search keyword applied
    When the user clears the search field completely
    And the search is triggered
    Then all tasks should be displayed (respecting any active filter)

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_022
  @Title: Search with whitespace only is treated as empty
  @Priority: Medium
  @Category: Edge Case - Search
  @Preconditions:
    - User is authenticated

  Scenario: Whitespace-only search is treated as empty search
    When the user enters "   " (spaces only) in the search field
    And the search is triggered
    Then all tasks should be displayed
    And the search should be treated as empty

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_023
  @Title: Search with leading and trailing whitespace
  @Priority: Medium
  @Category: Edge Case - Search
  @Preconditions:
    - User is authenticated
    - User has a task with title "Meeting notes"

  Scenario: Search trims leading and trailing whitespace
    When the user enters "  meeting  " in the search field
    And the search is triggered
    Then tasks containing "meeting" should be displayed
    And the search should work correctly despite whitespace

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_024
  @Title: Search with special characters
  @Priority: Medium
  @Category: Edge Case - Search
  @Preconditions:
    - User is authenticated
    - User has a task with special characters in title

  Scenario: Search handles special characters gracefully
    Given the user has a task with title "Bug fix - issue #123"
    When the user enters "#123" in the search field
    And the search is triggered
    Then the task "Bug fix - issue #123" should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_025
  @Title: Invalid filter parameter in URL defaults to All
  @Priority: Medium
  @Category: Edge Case - Validation
  @Preconditions:
    - User is authenticated

  Scenario: Invalid filter value in URL defaults to All
    When the user navigates directly to "/tasks?filter=InvalidValue"
    Then the "All" filter should be selected (default)
    And all tasks should be displayed

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_026
  @Title: Filter and search apply only to authenticated user's tasks
  @Priority: High
  @Category: Security - Data Isolation
  @Preconditions:
    - Multiple users exist in the system
    - Each user has their own tasks

  Scenario: Filter and search only affect current user's tasks
    Given User A is logged in
    And User A has tasks with "Project X" in title
    And User B has tasks with "Project X" in title
    When User A searches for "Project X"
    Then only User A's tasks with "Project X" should be displayed
    And User B's tasks should never be visible to User A

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_027
  @Title: Sorting is preserved with filter applied
  @Priority: Medium
  @Category: Functional - Sorting
  @Preconditions:
    - User is authenticated
    - User has multiple OPEN tasks with different dates

  Scenario: Tasks remain sorted by taskDate then createdAt when filtered
    Given the user has multiple OPEN tasks with different task dates
    When the user selects the "Open" filter option
    Then tasks should be sorted by taskDate
    And tasks with same taskDate should be sorted by createdAt

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_028
  @Title: Sorting is preserved with search applied
  @Priority: Medium
  @Category: Functional - Sorting
  @Preconditions:
    - User is authenticated
    - User has multiple matching tasks with different dates

  Scenario: Tasks remain sorted by taskDate then createdAt when searched
    Given the user has multiple tasks containing "meeting" with different dates
    When the user searches for "meeting"
    Then matching tasks should be sorted by taskDate
    And tasks with same taskDate should be sorted by createdAt

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_029
  @Title: Filter UI elements are visible
  @Priority: High
  @Category: UI - Elements
  @Preconditions:
    - User is authenticated

  Scenario: Filter options and search field are visible on tasks page
    When the user opens the tasks page
    Then the filter options "All", "Open", "Completed" should be visible
    And a search input field should be visible
    And the filter/search controls should be easily accessible

  # ------------------------------------------------------------

  @TestCaseID: TC_EPMCDMETST-52763_030
  @Title: User with no tasks sees filters but empty list
  @Priority: Medium
  @Category: Edge Case - Empty State
  @Preconditions:
    - User is authenticated
    - User has zero tasks

  Scenario: New user sees filters with empty task list
    Given the user has no tasks created
    When the user opens the tasks page
    Then the filter options should be visible
    And the search field should be visible
    And an empty state message should be displayed
    And selecting any filter should still show empty state
```

---

## Test Case Summary

| Category | Count | Test Case IDs |
|----------|-------|---------------|
| Functional - Filter | 5 | TC_001 - TC_005 |
| Functional - Search | 6 | TC_006 - TC_011 |
| Functional - Combined | 4 | TC_012 - TC_015 |
| Functional - Persistence | 5 | TC_016 - TC_020 |
| Edge Cases & Validation | 6 | TC_021 - TC_026 |
| Functional - Sorting | 2 | TC_027 - TC_028 |
| UI Elements & Empty State | 2 | TC_029 - TC_030 |
| **Total** | **30** | |

### Priority Distribution
| Priority | Count |
|----------|-------|
| High | 18 |
| Medium | 12 |

---

## Acceptance Criteria Coverage

| Acceptance Criteria | Test Cases Covering |
|---------------------|---------------------|
| Filter by status: All, Open, Completed | TC_001, TC_002, TC_003, TC_004, TC_005 |
| Search by keyword (title/description) | TC_006, TC_007, TC_008, TC_009, TC_010, TC_011 |
| Filter/search within authenticated user's tasks only | TC_026 |
| Preserve filter/search via query parameters | TC_016, TC_017, TC_018, TC_019, TC_020 |
| Results sorted by taskDate then createdAt | TC_027, TC_028 |

---

*Generated for EPMCDMETST-52763 | Todo Dashboard Filters and Search*
