package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utils.WaitUtils;

public class TasksPage {

    private final Page page;

    private final Locator title;
    private final Locator description;
    private final Locator taskDate;
    private final Locator plannedFinishDate;
    private final Locator addTaskButton;

    private final Locator fieldErrors;
    private final Locator globalErrors;

    private final Locator logoutButton;

    public TasksPage(Page page) {
        this.page = page;
        this.title = page.locator("#title");
        this.description = page.locator("#description");
        this.taskDate = page.locator("#taskDate");
        this.plannedFinishDate = page.locator("#plannedFinishDate");
        this.addTaskButton = page.locator("button.btn:text('Add Task')");

        this.fieldErrors = page.locator(".field-error");
        this.globalErrors = page.locator(".alert.error");

        this.logoutButton = page.locator("button.btn.secondary:text('Logout')");
    }

    public void open(String baseUrl) {
        page.navigate(baseUrl + "/tasks");
        WaitUtils.waitForDomReady(page);
    }

    public void createInvalidTask() {
        title.fill("");
        description.fill("");
        // Dates required: keep empty
        addTaskButton.click();
        WaitUtils.waitForDomReady(page);
    }

    public boolean hasAnyFieldErrors() {
        return fieldErrors.count() > 0;
    }

    public boolean hasGlobalErrors() {
        return globalErrors.count() > 0;
    }

    public boolean isLogoutVisible() {
        return logoutButton.isVisible();
    }
}
