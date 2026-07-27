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
}
