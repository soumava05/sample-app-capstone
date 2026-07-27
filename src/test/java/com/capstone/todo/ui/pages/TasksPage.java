package com.capstone.todo.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class TasksPage {

    private final Page page;

    public TasksPage(Page page) {
        this.page = page;
    }

    public boolean isDisplayed() {
        return page.locator("h1:text('Todo Dashboard')").isVisible();
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public void enterKeyword(String keyword) {
        page.locator("#keyword").fill(keyword);
    }

    public void selectStatus(String status) {
        page.locator("#status").selectOption(status == null ? "" : status);
    }

    public void setDateFrom(String dateFrom) {
        page.locator("#dateFrom").fill(dateFrom);
    }

    public void setDateTo(String dateTo) {
        page.locator("#dateTo").fill(dateTo);
    }

    public void applyFilters() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Apply Filters")).click();
    }

    public void clearFilters() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Clear Filters")).click();
    }

    public void createTask(String title, String description, String taskDate, String plannedFinishDate) {
        page.locator("#title").fill(title);
        page.locator("#description").fill(description);
        page.locator("#taskDate").fill(taskDate);
        page.locator("#plannedFinishDate").fill(plannedFinishDate);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Task")).click();
    }

    public void markTaskCompletedByTitle(String title) {
        page.locator(".task-item", new Page.LocatorOptions().setHasText(title))
            .locator("form[action*='/complete'] button")
            .click();
    }

    public int getTaskCount() {
        return page.locator(".task-item").count();
    }

    public boolean hasTaskWithTitle(String title) {
        return page.locator(".task-item h3", new Page.LocatorOptions().setHasText(title)).count() > 0;
    }

    public boolean hasAnyTaskContainingText(String text) {
        return page.locator(".task-item", new Page.LocatorOptions().setHasText(text)).count() > 0;
    }

    public boolean hasFilterError() {
        return page.locator(".alert.error").first().isVisible();
    }

    public String getFilterErrorText() {
        return page.locator(".alert.error").first().textContent();
    }

    public String getKeywordValue() {
        return page.locator("#keyword").inputValue();
    }

    public String getSelectedStatus() {
        return page.locator("#status").inputValue();
    }

    public String getDateFromValue() {
        return page.locator("#dateFrom").inputValue();
    }

    public String getDateToValue() {
        return page.locator("#dateTo").inputValue();
    }

    public boolean isEmptyStateVisible() {
        return page.locator(".empty-state").isVisible();
    }

    public String getEmptyStateText() {
        return page.locator(".empty-state").textContent();
    }

    public Locator taskItems() {
        return page.locator(".task-item");
    }
}
