package com.capstone.todo.tests;

import com.capstone.todo.base.BaseUiTest;
import com.capstone.todo.listeners.ExtentTestListener;
import com.capstone.todo.pages.LoginPage;
import com.capstone.todo.pages.RegisterPage;
import com.capstone.todo.pages.TasksPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Listeners(ExtentTestListener.class)
public class TaskDashboardFilteringUiTest extends BaseUiTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private String username;
    private String password;
    private TasksPage tasksPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareTestData() {
        username = "user" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        password = "Password@123";

        RegisterPage registerPage = new RegisterPage(getPage());
        registerPage.open()
            .enterUsername(username)
            .enterFullName("UI Automation User")
            .enterPassword(password)
            .enterConfirmPassword(password)
            .submit();

        LoginPage loginPage = new LoginPage(getPage());
        loginPage.open()
            .enterUsername(username)
            .enterPassword(password)
            .submit();

        tasksPage = new TasksPage(getPage());
        seedTasks();
    }

    @Test
    public void verifyStatusFilterDisplaysOnlyOpenTasks() {
        tasksPage.open()
            .selectStatus("OPEN")
            .applyFilters();

        List<String> statuses = tasksPage.getTaskStatuses();
        Assert.assertFalse(statuses.isEmpty(), "Expected at least one task after OPEN filter");
        Assert.assertTrue(statuses.stream().allMatch(status -> status.equalsIgnoreCase("OPEN")));
        Assert.assertEquals(tasksPage.getSelectedStatus(), "OPEN");
    }

    @Test
    public void verifyCombinedStatusAndDateFilterPreservesValues() {
        String fromDate = LocalDate.of(2026, 6, 15).format(DATE_FORMATTER);
        String toDate = LocalDate.of(2026, 6, 30).format(DATE_FORMATTER);

        tasksPage.open()
            .selectStatus("OPEN")
            .setFromDate(fromDate)
            .setToDate(toDate)
            .applyFilters();

        Assert.assertEquals(tasksPage.getSelectedStatus(), "OPEN");
        Assert.assertEquals(tasksPage.getFromDate(), fromDate);
        Assert.assertEquals(tasksPage.getToDate(), toDate);
        Assert.assertFalse(tasksPage.getTaskTitles().isEmpty(), "Expected at least one filtered task");
        Assert.assertTrue(tasksPage.getTaskTitles().contains("Open - Late June"));
        Assert.assertTrue(tasksPage.getTaskStatuses().stream().allMatch(status -> status.equalsIgnoreCase("OPEN")));
        Assert.assertFalse(tasksPage.getTaskTitles().contains("Open - Early June"), "Task before fromDate should not be displayed");
    }

    @Test
    public void verifyInvalidDateRangeShowsErrorAndPreservesInputValues() {
        String fromDate = LocalDate.of(2026, 6, 30).format(DATE_FORMATTER);
        String toDate = LocalDate.of(2026, 6, 1).format(DATE_FORMATTER);

        tasksPage.open()
            .selectStatus("OPEN")
            .setFromDate(fromDate)
            .setToDate(toDate)
            .applyFilters();

        Assert.assertEquals(tasksPage.getErrorMessage(), "From date cannot be after to date");
        Assert.assertEquals(tasksPage.getSelectedStatus(), "OPEN");
        Assert.assertEquals(tasksPage.getFromDate(), fromDate);
        Assert.assertEquals(tasksPage.getToDate(), toDate);
    }

    @Test
    public void verifyResetClearsFiltersAndReturnsAllTasks() {
        tasksPage.open()
            .selectStatus("COMPLETED")
            .setFromDate("2026-06-01")
            .setToDate("2026-06-30")
            .applyFilters()
            .clickReset();

        Assert.assertTrue(tasksPage.getCurrentUrl().endsWith("/tasks"));
        Assert.assertEquals(tasksPage.getSelectedStatus(), "ALL");
        Assert.assertEquals(tasksPage.getFromDate(), "");
        Assert.assertEquals(tasksPage.getToDate(), "");
        Assert.assertEquals(tasksPage.getTaskCount(), 3);
    }

    @Test
    public void verifyEmptyStateMessageWhenNoTasksMatchFilters() {
        tasksPage.open()
            .selectStatus("COMPLETED")
            .setFromDate("2026-06-16")
            .setToDate("2026-06-30")
            .applyFilters();

        Assert.assertEquals(tasksPage.getTaskCount(), 0);
        Assert.assertEquals(tasksPage.getEmptyStateMessage(), "No tasks match the selected filters.");
        Assert.assertEquals(tasksPage.getSelectedStatus(), "COMPLETED");
    }

    private void seedTasks() {
        tasksPage.open()
            .createTask("Open - Early June", "Open task before range", "2026-06-10", "2026-06-11")
            .createTask("Completed - Mid June", "Task to complete", "2026-06-15", "2026-06-16");

        getPage().locator("form[action*='/complete'] button").first().click();

        tasksPage.createTask("Open - Late June", "Open task within range", "2026-06-20", "2026-06-21");
        tasksPage.open();
    }
}
