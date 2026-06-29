# Test Cases for EPMCDMETST-51892

## Feature Overview
This suite validates that **Thymeleaf/MVC UI endpoints remain user-friendly on errors** and **do not expose raw JSON error envelopes** to end users after introducing centralized exception handling.

- Scope: **UI-only (HTML/Thymeleaf)**
- Out of scope: API JSON error envelope structure, HTTP status mapping for APIs, correlation-id propagation in JSON responses
- Base URL: `http://localhost:8090`

## Test Cases (Gherkin)

Feature: UI Error Handling (Thymeleaf) does not expose JSON and shows friendly outcome

  @TestCaseID: TC_EPMCDMETST-51892_001
  @Title: UI validation failure shows user-friendly feedback (no JSON)
  @Preconditions:
    - Application running at http://localhost:8090
    - A UI form page exists that performs server-side validation

  Scenario: UI submit invalid form data results in friendly validation message
    Given user navigates to a Thymeleaf form page
    When user submits the form with invalid inputs
    Then the response content type contains "text/html"
    And the page shows validation messages near the invalid fields or as a summary
    And the response body does not contain a JSON error envelope

  @TestCaseID: TC_EPMCDMETST-51892_002
  @Title: UI missing entity shows friendly "not found" outcome
  @Preconditions:
    - A UI route exists that loads an entity by id (e.g., task/user details)

  Scenario: UI open details page for missing entity does not return JSON
    Given user navigates to a UI details page with a non-existing id
    When the page is requested
    Then the response content type contains "text/html"
    And the page shows a friendly "not found" message OR redirects to a safe page with a message
    And the response body does not contain a JSON error envelope

  @TestCaseID: TC_EPMCDMETST-51892_003
  @Title: UI internal server error renders friendly error page (no stack trace)
  @Preconditions:
    - There is a UI action that can trigger an internal error (e.g., service exception)

  Scenario: UI action triggers server error and user sees friendly error view
    Given user is on a UI page that triggers a server-side operation
    When the operation fails due to an internal error
    Then the response content type contains "text/html"
    And a friendly error page/message is displayed
    And the page does not display stack traces or Java exception class names

  @TestCaseID: TC_EPMCDMETST-51892_004
  @Title: UI error page does not leak JSON fields/correlation id as JSON
  @Preconditions:
    - Centralized exception handling is enabled

  Scenario: UI error response does not expose JSON error contract
    Given a UI request results in an error
    When the error page is rendered
    Then the response body does not contain JSON keys like "correlationId" or "errorCode" in JSON format
    And the response content type contains "text/html"

  @TestCaseID: TC_EPMCDMETST-51892_005
  @Title: UI preserves navigation safety (no blank page) after errors
  @Preconditions:
    - UI has navigation header/footer or home link

  Scenario: UI error view still allows user to continue
    Given user triggers an error via UI
    When the error page is shown
    Then the page contains a way to navigate back (e.g., Home link or Back button)
    And the page is rendered successfully (not empty/white screen)

## Coverage Summary
- Validates UI error outcomes are HTML-based and user-friendly.
- Validates no raw JSON envelope is exposed to UI users.
- Validates common error types: validation, not-found, internal error.
