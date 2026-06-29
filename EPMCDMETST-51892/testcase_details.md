# Test Cases for EPMCDMETST-51892

## Feature Overview
**Feature:** Friendly UI error handling for Thymeleaf endpoints (UI only)

**Goal:** Ensure that when errors occur while using Thymeleaf UI endpoints, the user sees a friendly HTML outcome (error page or redirect with message) and never receives raw JSON/stack traces.

**In Scope (UI only):**
- Friendly HTML error view or redirect
- UI validation messages
- UI not-found experience
- UI storage/internal error experience
- No leakage of JSON error envelopes or stack traces

**Out of Scope:**
- API JSON error envelope contract validation
- Correlation-ID header propagation for API clients

---

## Test Cases (Gherkin)

Feature: Friendly UI error handling for Thymeleaf endpoints

  @TestCaseID: TC_EPMCDMETST-51892_001
  @Title: UI route error renders friendly HTML error page (not JSON)
  @Preconditions:
    - Thymeleaf UI is accessible
    - Global exception handling is enabled

  Scenario: User opens a UI route that triggers an unhandled exception
    Given the user navigates to a UI route known to trigger an internal error
    When the page loads
    Then the response should render an HTML error view (content contains `<html` or a known error template marker)
    And the UI should not show raw JSON (no `{`...`"code"`... patterns)
    And the UI should not expose a stacktrace (page does not contain "Exception", "Stacktrace", "at com.")


  @TestCaseID: TC_EPMCDMETST-51892_002
  @Title: UI “not found” renders friendly HTML page/redirect (no raw JSON)
  @Preconditions:
    - A UI page exists that accepts an ID in the route (e.g., entity details)

  Scenario: User opens a non-existing entity details page from UI
    Given the user navigates to a UI details route with a non-existing ID
    When the page loads
    Then the user should see a friendly “not found” UI outcome (error page or redirected page with message)
    And the UI should not show raw JSON
    And the UI should not expose technical exception details


  @TestCaseID: TC_EPMCDMETST-51892_003
  @Title: UI form validation error shows user-friendly validation messages (HTML)
  @Preconditions:
    - A UI form exists (create/edit entity) with at least one required field validation

  Scenario: User submits form with missing required fields
    Given the user opens a UI create/edit form page
    When the user submits the form with required fields empty or invalid
    Then the page should remain in HTML context (rendered form or redirected with UI message)
    And validation messages should be visible to the user (field-level or summary)
    And the UI should not display a JSON error envelope


  @TestCaseID: TC_EPMCDMETST-51892_004
  @Title: UI storage failure shows friendly error outcome (no sensitive leakage)
  @Preconditions:
    - There is a UI action that can trigger a storage failure in test environment (e.g., file upload/save)

  Scenario: User triggers a storage failure via UI action
    Given the user opens a UI page that performs a storage operation
    When the user performs an action that triggers a storage failure
    Then the UI should show a friendly error page or redirect with message
    And the UI should not show raw exception messages (e.g., "IOException", filesystem paths)
    And the UI should not show raw JSON


  @TestCaseID: TC_EPMCDMETST-51892_005
  @Title: UI error page remains consistent across browsers/viewports
  @Preconditions:
    - Error view exists (dedicated error template or redirect target)

  Scenario: Error view layout is usable on standard viewport
    Given the user triggers a UI error page
    When the error page is displayed
    Then key UI elements are visible (title/message, navigation/home link if applicable)
    And there are no broken layouts (no overlapping critical elements)

---

## Coverage Summary
- Friendly UX for UI exceptions: Covered by TC_001, TC_004
- Not-found UI behavior: Covered by TC_002
- UI validation messaging: Covered by TC_003
- No JSON/stacktrace exposure in UI: Covered by TC_001–TC_004
- Basic layout sanity for error view: Covered by TC_005
