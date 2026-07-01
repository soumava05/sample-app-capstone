package com.capstone.todo.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Object for Tasks Page (Dashboard).
 * Handles task creation, filtering, and searching functionality.
 */
public class TasksPage {

    private final Page page;
    private final String baseUrl;

    // Locators - Header
    private static final String PAGE_TITLE = "h1:has-text('Todo Dashboard')";
    private static final String USERNAME_DISPLAY = ".subtitle strong";
    private static final String LOGOUT_BUTTON = "button:has-text('Logout')";

    // Locators - Task Creation Form
    private static final String TASK_TITLE_INPUT = "#title";
    private static final String TASK_DESCRIPTION_INPUT = "#description";
    private static final String TASK_DATE_INPUT = "#taskDate";
    private static final String PLANNED_FINISH_DATE_INPUT = "#plannedFinishDate";
    private static final String ADD_TASK_BUTTON = "button:has-text('Add Task')";

    // Locators - Filter and Search
    private static final String FILTER_ALL = "[data-filter='All'], button:has-text('All'), a:has-text('All')";
    private static final String FILTER_OPEN = "[data-filter='Open'], button:has-text('Open'), a:has-text('Open')";
    private static final String FILTER_COMPLETED = "[data-filter='Completed'], button:has-text('Completed'), a:has-text('Completed')";
    private static final String SEARCH_INPUT = "#keyword, input[name='keyword'], input[placeholder*='Search'], input[type='search']";
    private static final String SEARCH_BUTTON = "button:has-text('Search'), button[type='submit']:near(input[name='keyword'])";

    // Locators - Task List
    private static final String TASK_LIST_CONTAINER = ".task-list, .card:has(h2:has-text('My Task List'))";
    private static final String TASK_ITEM = ".task-item, .task-row, tr:has(td)";
    private static final String TASK_TITLE_IN_LIST = ".task-title, td:first-child, strong";
    private static final String TASK_STATUS_BADGE = ".status-badge, .status, span:has-text('OPEN'), span:has-text('COMPLETED')";
    private static final String COMPLETE_BUTTON = "button:has-text('Complete'), button:has-text('Mark Complete')";
    private static final String EMPTY_STATE = ".empty-state, p:has-text('No tasks')";

    // Locators - Filter Buttons (alternative selectors)
    private static final String FILTER_CONTAINER = ".filter-container, .filters, form:has(input[name='filter'])";

    public TasksPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    /**
     * Navigate to the tasks page.
     */
    public TasksPage navigate() {
        page.navigate(baseUrl + "/tasks");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Navigate to tasks page with query parameters.
     */
    public TasksPage navigateWithParams(String params) {
        page.navigate(baseUrl + "/tasks?" + params);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Check if the tasks page is displayed.
     */
    public boolean isDisplayed() {
        return page.isVisible(PAGE_TITLE) || page.url().contains("/tasks");
    }

    /**
     * Get the displayed username.
     */
    public String getDisplayedUsername() {
        if (page.isVisible(USERNAME_DISPLAY)) {
            return page.textContent(USERNAME_DISPLAY);
        }
        return "";
    }

    // ==================== TASK CREATION ====================

    /**
     * Create a new task with all fields.
     */
    public TasksPage createTask(String title, String description, LocalDate taskDate, LocalDate plannedFinishDate) {
        page.fill(TASK_TITLE_INPUT, title);
        page.fill(TASK_DESCRIPTION_INPUT, description);
        page.fill(TASK_DATE_INPUT, taskDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        page.fill(PLANNED_FINISH_DATE_INPUT, plannedFinishDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        page.click(ADD_TASK_BUTTON);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Create a simple task with default dates.
     */
    public TasksPage createTask(String title, String description) {
        LocalDate today = LocalDate.now();
        return createTask(title, description, today, today.plusDays(7));
    }

    // ==================== FILTER FUNCTIONALITY ====================

    /**
     * Click the "All" filter option.
     */
    public TasksPage clickFilterAll() {
        clickFirstVisible(FILTER_ALL);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Click the "Open" filter option.
     */
    public TasksPage clickFilterOpen() {
        clickFirstVisible(FILTER_OPEN);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Click the "Completed" filter option.
     */
    public TasksPage clickFilterCompleted() {
        clickFirstVisible(FILTER_COMPLETED);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Select a filter by name.
     */
    public TasksPage selectFilter(String filterName) {
        switch (filterName.toLowerCase()) {
            case "all":
                return clickFilterAll();
            case "open":
                return clickFilterOpen();
            case "completed":
                return clickFilterCompleted();
            default:
                throw new IllegalArgumentException("Unknown filter: " + filterName);
        }
    }

    /**
     * Check if a specific filter is selected/active.
     */
    public boolean isFilterSelected(String filterName) {
        String selector = switch (filterName.toLowerCase()) {
            case "all" -> FILTER_ALL;
            case "open" -> FILTER_OPEN;
            case "completed" -> FILTER_COMPLETED;
            default -> throw new IllegalArgumentException("Unknown filter: " + filterName);
        };

        Locator filterElement = page.locator(selector).first();
        if (filterElement.isVisible()) {
            String classAttr = filterElement.getAttribute("class");
            String ariaPressed = filterElement.getAttribute("aria-pressed");
            return (classAttr != null && (classAttr.contains("active") || classAttr.contains("selected")))
                    || "true".equals(ariaPressed);
        }
        return false;
    }

    /**
     * Check if filter controls are visible.
     */
    public boolean areFilterControlsVisible() {
        return page.isVisible(FILTER_ALL) || page.isVisible(FILTER_OPEN) || page.isVisible(FILTER_COMPLETED);
    }

    // ==================== SEARCH FUNCTIONALITY ====================

    /**
     * Enter a search keyword.
     */
    public TasksPage enterSearchKeyword(String keyword) {
        if (page.isVisible(SEARCH_INPUT)) {
            page.fill(SEARCH_INPUT, keyword);
        }
        return this;
    }

    /**
     * Clear the search field.
     */
    public TasksPage clearSearch() {
        if (page.isVisible(SEARCH_INPUT)) {
            page.fill(SEARCH_INPUT, "");
        }
        return this;
    }

    /**
     * Trigger the search (click search button or press Enter).
     */
    public TasksPage triggerSearch() {
        if (page.isVisible(SEARCH_BUTTON)) {
            page.click(SEARCH_BUTTON);
        } else if (page.isVisible(SEARCH_INPUT)) {
            page.press(SEARCH_INPUT, "Enter");
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Perform a complete search operation.
     */
    public TasksPage searchFor(String keyword) {
        enterSearchKeyword(keyword);
        triggerSearch();
        return this;
    }

    /**
     * Get the current value in the search field.
     */
    public String getSearchFieldValue() {
        if (page.isVisible(SEARCH_INPUT)) {
            return page.inputValue(SEARCH_INPUT);
        }
        return "";
    }

    /**
     * Check if search input is visible.
     */
    public boolean isSearchInputVisible() {
        return page.isVisible(SEARCH_INPUT);
    }

    // ==================== TASK LIST OPERATIONS ====================

    /**
     * Get all visible task titles.
     */
    public List<String> getVisibleTaskTitles() {
        List<String> titles = new ArrayList<>();
        Locator taskItems = page.locator(TASK_ITEM);
        int count = taskItems.count();

        for (int i = 0; i < count; i++) {
            Locator titleElement = taskItems.nth(i).locator(TASK_TITLE_IN_LIST).first();
            if (titleElement.isVisible()) {
                titles.add(titleElement.textContent().trim());
            }
        }
        return titles;
    }

    /**
     * Get the count of visible tasks.
     */
    public int getVisibleTaskCount() {
        Locator taskItems = page.locator(TASK_ITEM);
        return taskItems.count();
    }

    /**
     * Check if a task with specific title is visible.
     */
    public boolean isTaskVisible(String taskTitle) {
        return page.isVisible("text=" + taskTitle) ||
                page.locator(TASK_ITEM + ":has-text('" + taskTitle + "')").isVisible();
    }

    /**
     * Check if empty state message is displayed.
     */
    public boolean isEmptyStateDisplayed() {
        return page.isVisible(EMPTY_STATE) ||
                page.isVisible("text=No tasks") ||
                page.isVisible("text=no tasks");
    }

    /**
     * Get all task statuses visible on the page.
     */
    public List<String> getVisibleTaskStatuses() {
        List<String> statuses = new ArrayList<>();
        Locator statusElements = page.locator(TASK_STATUS_BADGE);
        int count = statusElements.count();

        for (int i = 0; i < count; i++) {
            String status = statusElements.nth(i).textContent().trim().toUpperCase();
            if (status.contains("OPEN") || status.contains("COMPLETED")) {
                statuses.add(status.contains("OPEN") ? "OPEN" : "COMPLETED");
            }
        }
        return statuses;
    }

    /**
     * Check if all visible tasks have a specific status.
     */
    public boolean allTasksHaveStatus(String expectedStatus) {
        List<String> statuses = getVisibleTaskStatuses();
        if (statuses.isEmpty()) {
            return true; // No tasks, so condition is vacuously true
        }
        return statuses.stream().allMatch(s -> s.equalsIgnoreCase(expectedStatus));
    }

    /**
     * Mark a task as completed by title.
     */
    public TasksPage markTaskCompleted(String taskTitle) {
        Locator taskRow = page.locator(TASK_ITEM + ":has-text('" + taskTitle + "')");
        if (taskRow.isVisible()) {
            Locator completeBtn = taskRow.locator(COMPLETE_BUTTON);
            if (completeBtn.isVisible()) {
                completeBtn.click();
                page.waitForLoadState(LoadState.NETWORKIDLE);
            }
        }
        return this;
    }

    // ==================== URL AND NAVIGATION ====================

    /**
     * Get the current URL.
     */
    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Check if URL contains a specific query parameter.
     */
    public boolean urlContainsParam(String param) {
        return page.url().contains(param);
    }

    /**
     * Check if URL contains filter parameter.
     */
    public boolean urlContainsFilter(String filterValue) {
        return page.url().contains("filter=" + filterValue);
    }

    /**
     * Check if URL contains keyword parameter.
     */
    public boolean urlContainsKeyword(String keyword) {
        return page.url().contains("keyword=" + keyword);
    }

    /**
     * Refresh the page.
     */
    public TasksPage refresh() {
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Logout from the application.
     */
    public LoginPage logout() {
        page.click(LOGOUT_BUTTON);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return new LoginPage(page, baseUrl);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Click the first visible element matching the selector.
     */
    private void clickFirstVisible(String selector) {
        Locator elements = page.locator(selector);
        for (int i = 0; i < elements.count(); i++) {
            if (elements.nth(i).isVisible()) {
                elements.nth(i).click();
                return;
            }
        }
        // Fallback: click the first one
        elements.first().click();
    }

    /**
     * Wait for the task list to be visible.
     */
    public TasksPage waitForTaskList() {
        page.waitForSelector(TASK_LIST_CONTAINER, new Page.WaitForSelectorOptions().setTimeout(5000));
        return this;
    }
}
