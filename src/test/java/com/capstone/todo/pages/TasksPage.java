package com.capstone.todo.pages;

import com.capstone.todo.utils.ConfigManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

public class TasksPage {

    private final Page page;

    public TasksPage(Page page) {
        this.page = page;
    }

    public TasksPage open() {
        page.navigate(ConfigManager.getBaseUrl() + "/tasks");
        return this;
    }

    public TasksPage selectStatus(String status) {
        page.locator("#status").selectOption(status);
        return this;
    }

    public TasksPage setFromDate(String fromDate) {
        Locator locator = page.locator("#fromDate");
        locator.fill("");
        if (fromDate != null) {
            locator.fill(fromDate);
        }
        return this;
    }

    public TasksPage setToDate(String toDate) {
        Locator locator = page.locator("#toDate");
        locator.fill("");
        if (toDate != null) {
            locator.fill(toDate);
        }
        return this;
    }

    public TasksPage applyFilters() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Apply Filters")).click();
        return this;
    }

    public TasksPage clickReset() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Reset")).click();
        return this;
    }

    public TasksPage createTask(String title, String description, String taskDate, String plannedFinishDate) {
        page.locator("#title").fill(title);
        page.locator("#description").fill(description);
        page.locator("#taskDate").fill(taskDate);
        page.locator("#plannedFinishDate").fill(plannedFinishDate);
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Add Task")).click();
        return this;
    }

    public String getSelectedStatus() {
        return page.locator("#status").inputValue();
    }

    public String getFromDate() {
        return page.locator("#fromDate").inputValue();
    }

    public String getToDate() {
        return page.locator("#toDate").inputValue();
    }

    public String getErrorMessage() {
        Locator error = page.locator(".alert.error").first();
        return error.isVisible() ? error.textContent().trim() : "";
    }

    public String getEmptyStateMessage() {
        return page.locator(".empty-state").textContent().trim();
    }

    public List<String> getTaskStatuses() {
        return page.locator(".task-item .status").allTextContents().stream()
            .map(String::trim)
            .toList();
    }

    public List<String> getTaskTitles() {
        return page.locator(".task-item h3").allTextContents().stream()
            .map(String::trim)
            .toList();
    }

    public int getTaskCount() {
        return page.locator(".task-item").count();
    }

    public String getCurrentUrl() {
        return page.url();
    }
}
