package com.capstone.todo.ui.tests;

import com.capstone.todo.ui.base.BaseUiTest;
import com.capstone.todo.ui.pages.LoginPage;
import com.capstone.todo.ui.pages.RegisterPage;
import com.capstone.todo.ui.pages.TasksPage;
import com.capstone.todo.ui.utils.TestDataFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TaskDashboardFilterUiTest extends BaseUiTest {

    @Test(description = "Verify authenticated user can filter tasks by keyword and preserve filter values")
    public void shouldFilterTasksByKeywordAndPreserveValues() {
        String username = TestDataFactory.uniqueUsername("filteruser");
        String password = "Password@123";
        String titleMatch = TestDataFactory.uniqueTaskTitle("AlphaSearch");
        String titleOther = TestDataFactory.uniqueTaskTitle("BetaOther");

        new RegisterPage(page)
            .open(BASE_URL)
            .register(username, "Filter User", password, password);

        new LoginPage(page)
            .login(username, password);

        TasksPage tasksPage = new TasksPage(page);
        Assert.assertTrue(tasksPage.isDisplayed(), "Tasks dashboard should be displayed after login");

        tasksPage.createTask(titleMatch, "Contains KEYword token", TestDataFactory.today(), TestDataFactory.plusDays(1));
        tasksPage.createTask(titleOther, "Different description", TestDataFactory.plusDays(2), TestDataFactory.plusDays(3));

        tasksPage.enterKeyword("keyword");
        tasksPage.applyFilters();

        Assert.assertTrue(tasksPage.hasTaskWithTitle(titleMatch), "Expected matching task to remain visible");
        Assert.assertFalse(tasksPage.hasTaskWithTitle(titleOther), "Expected non-matching task to be filtered out");
        Assert.assertEquals(tasksPage.getKeywordValue(), "keyword", "Keyword value should be preserved");
    }

    @Test(description = "Verify invalid date range shows inline validation and clear filters restores tasks")
    public void shouldShowValidationForInvalidDateRangeAndClearFilters() {
        String username = TestDataFactory.uniqueUsername("dateuser");
        String password = "Password@123";
        String title = TestDataFactory.uniqueTaskTitle("DateRangeTask");

        new RegisterPage(page)
            .open(BASE_URL)
            .register(username, "Date User", password, password);

        new LoginPage(page)
            .login(username, password);

        TasksPage tasksPage = new TasksPage(page);
        Assert.assertTrue(tasksPage.isDisplayed(), "Tasks dashboard should be displayed after login");

        tasksPage.createTask(title, "Date validation task", TestDataFactory.today(), TestDataFactory.plusDays(5));

        tasksPage.setDateFrom(TestDataFactory.plusDays(10));
        tasksPage.setDateTo(TestDataFactory.today());
        tasksPage.applyFilters();

        Assert.assertTrue(tasksPage.hasFilterError(), "Expected inline validation message for invalid date range");
        Assert.assertTrue(tasksPage.getFilterErrorText().toLowerCase().contains("date"), "Error should mention date validation");
        Assert.assertTrue(tasksPage.hasTaskWithTitle(title), "Fallback task list should remain visible after invalid filter");

        tasksPage.clearFilters();
        Assert.assertTrue(tasksPage.hasTaskWithTitle(title), "Task should still be visible after clearing filters");
        Assert.assertEquals(tasksPage.getKeywordValue(), "", "Keyword should be cleared");
        Assert.assertEquals(tasksPage.getDateFromValue(), "", "Date From should be cleared");
        Assert.assertEquals(tasksPage.getDateToValue(), "", "Date To should be cleared");
    }

    @Test(description = "Verify status filtering works for a completed task")
    public void shouldFilterCompletedTasksByStatus() {
        String username = TestDataFactory.uniqueUsername("statususer");
        String password = "Password@123";
        String titleOpen = TestDataFactory.uniqueTaskTitle("OpenTask");
        String titleToComplete = TestDataFactory.uniqueTaskTitle("CompleteMe");

        new RegisterPage(page)
            .open(BASE_URL)
            .register(username, "Status User", password, password);

        new LoginPage(page)
            .login(username, password);

        TasksPage tasksPage = new TasksPage(page);
        Assert.assertTrue(tasksPage.isDisplayed(), "Tasks dashboard should be displayed after login");

        tasksPage.createTask(titleOpen, "Open task description", TestDataFactory.today(), TestDataFactory.plusDays(2));
        tasksPage.createTask(titleToComplete, "Task that will be completed", TestDataFactory.plusDays(1), TestDataFactory.plusDays(4));

        tasksPage.applyFilters();
        tasksPage.markTaskCompletedByTitle(titleToComplete);

        tasksPage.selectStatus("COMPLETED");
        tasksPage.applyFilters();

        Assert.assertTrue(tasksPage.hasTaskWithTitle(titleToComplete), "Completed task should appear in COMPLETED filter");
        Assert.assertFalse(tasksPage.hasTaskWithTitle(titleOpen), "Open task should not appear in COMPLETED filter");
    }

    @Test(description = "Verify inclusive date range filter keeps tasks on boundary dates and removes out-of-range tasks")
    public void shouldFilterTasksByInclusiveDateRange() {
        String username = TestDataFactory.uniqueUsername("boundaryuser");
        String password = "Password@123";
        String titleFromBoundary = TestDataFactory.uniqueTaskTitle("FromBoundary");
        String titleToBoundary = TestDataFactory.uniqueTaskTitle("ToBoundary");
        String titleOutOfRange = TestDataFactory.uniqueTaskTitle("OutOfRange");
        String dateFrom = TestDataFactory.today();
        String dateTo = TestDataFactory.plusDays(2);

        new RegisterPage(page)
            .open(BASE_URL)
            .register(username, "Boundary User", password, password);

        new LoginPage(page)
            .login(username, password);

        TasksPage tasksPage = new TasksPage(page);
        Assert.assertTrue(tasksPage.isDisplayed(), "Tasks dashboard should be displayed after login");

        tasksPage.createTask(titleFromBoundary, "Task on lower boundary", dateFrom, TestDataFactory.plusDays(3));
        tasksPage.createTask(titleToBoundary, "Task on upper boundary", dateTo, TestDataFactory.plusDays(4));
        tasksPage.createTask(titleOutOfRange, "Task outside range", TestDataFactory.plusDays(5), TestDataFactory.plusDays(6));

        tasksPage.setDateFrom(dateFrom);
        tasksPage.setDateTo(dateTo);
        tasksPage.applyFilters();

        Assert.assertTrue(tasksPage.hasTaskWithTitle(titleFromBoundary), "Task on dateFrom boundary should be visible");
        Assert.assertTrue(tasksPage.hasTaskWithTitle(titleToBoundary), "Task on dateTo boundary should be visible");
        Assert.assertFalse(tasksPage.hasTaskWithTitle(titleOutOfRange), "Task outside inclusive range should be filtered out");
    }

    @Test(description = "Verify no matching filters show empty state")
    public void shouldShowEmptyStateWhenNoTasksMatchFilters() {
        String username = TestDataFactory.uniqueUsername("emptyuser");
        String password = "Password@123";
        String title = TestDataFactory.uniqueTaskTitle("VisibleTask");

        new RegisterPage(page)
            .open(BASE_URL)
            .register(username, "Empty User", password, password);

        new LoginPage(page)
            .login(username, password);

        TasksPage tasksPage = new TasksPage(page);
        Assert.assertTrue(tasksPage.isDisplayed(), "Tasks dashboard should be displayed after login");

        tasksPage.createTask(title, "Task that should not match query", TestDataFactory.today(), TestDataFactory.plusDays(1));

        tasksPage.enterKeyword("no-such-keyword-xyz");
        tasksPage.applyFilters();

        Assert.assertTrue(tasksPage.isEmptyStateVisible(), "Empty state should be shown when no tasks match");
        Assert.assertTrue(tasksPage.getEmptyStateText().toLowerCase().contains("no tasks"), "Empty state should explain that no tasks were found");
        Assert.assertFalse(tasksPage.hasTaskWithTitle(title), "Existing task should not appear when it does not match filter");
    }
}
