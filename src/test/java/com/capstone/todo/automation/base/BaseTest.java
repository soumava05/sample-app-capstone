package com.capstone.todo.automation.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.capstone.todo.automation.pages.LoginPage;
import com.capstone.todo.automation.pages.RegistrationPage;
import com.capstone.todo.automation.pages.TasksPage;
import com.microsoft.playwright.*;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Base Test class for Playwright UI Automation.
 * Provides browser setup, teardown, and Extent Reports integration.
 */
public abstract class BaseTest {

    // Application runs on port 8090 as per application.yml
    protected static final String BASE_URL = "http://localhost:8090";
    protected static final String DEFAULT_PASSWORD = "Password123!";
    protected static final String DEFAULT_FULL_NAME = "Test User";

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected static ExtentReports extentReports;
    protected ExtentTest extentTest;
    private static String reportPath;
    
    private static boolean userRegistered = false;
    private static String registeredUsername;

    @BeforeSuite
    public void setupSuite() {
        // Initialize Playwright
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setSlowMo(50));

        // Initialize Extent Reports
        long epochTime = Instant.now().getEpochSecond();
        reportPath = "EPMCDMETST-52763/automation_run_" + epochTime + ".html";

        File reportDir = new File("EPMCDMETST-52763");
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("EPMCDMETST-52763 - Task Filter & Search Test Report");
        sparkReporter.config().setReportName("Todo Dashboard - Filter & Search Automation Results");
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Application", "Todo Dashboard");
        extentReports.setSystemInfo("Feature", "Task List Filters and Keyword Search");
        extentReports.setSystemInfo("Jira Ticket", "EPMCDMETST-52763");
        extentReports.setSystemInfo("Browser", "Chromium");
        extentReports.setSystemInfo("Environment", "Local");
        extentReports.setSystemInfo("Base URL", BASE_URL);
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));

        // Register test user once
        registerTestUserIfNeeded();
    }

    /**
     * Register a test user if not already registered.
     */
    private void registerTestUserIfNeeded() {
        if (!userRegistered) {
            BrowserContext tempContext = browser.newContext();
            Page tempPage = tempContext.newPage();
            
            try {
                registeredUsername = "testuser" + System.currentTimeMillis();
                RegistrationPage regPage = new RegistrationPage(tempPage, BASE_URL);
                regPage.navigate();
                
                if (regPage.isDisplayed()) {
                    regPage.register(registeredUsername, DEFAULT_FULL_NAME, DEFAULT_PASSWORD);
                    userRegistered = true;
                    System.out.println("Test user registered: " + registeredUsername);
                }
            } catch (Exception e) {
                System.out.println("Registration failed, user might already exist: " + e.getMessage());
                // Try with existing user
                registeredUsername = "testuser";
            } finally {
                tempContext.close();
            }
        }
    }

    /**
     * Get the registered username for tests.
     */
    protected String getTestUsername() {
        return registeredUsername != null ? registeredUsername : "testuser";
    }

    @BeforeMethod
    public void setupMethod(ITestResult result) {
        // Create a new browser context for each test (isolated session)
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        page = context.newPage();

        // Create Extent Test
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        extentTest = extentReports.createTest(testName, description != null ? description : "");

        // Add test categories based on method groups
        String[] groups = result.getMethod().getGroups();
        for (String group : groups) {
            extentTest.assignCategory(group);
        }
    }

    @AfterMethod
    public void teardownMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // Capture screenshot on failure
            String screenshotPath = captureScreenshot(result.getMethod().getMethodName());
            extentTest.fail("Test Failed: " + result.getThrowable().getMessage());
            if (screenshotPath != null) {
                try {
                    extentTest.addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
                } catch (Exception e) {
                    extentTest.warning("Could not attach screenshot: " + e.getMessage());
                }
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            extentTest.pass("Test Passed");
        } else if (result.getStatus() == ITestResult.SKIP) {
            extentTest.skip("Test Skipped: " + (result.getThrowable() != null ? result.getThrowable().getMessage() : "No reason"));
        }

        // Log execution time
        long duration = result.getEndMillis() - result.getStartMillis();
        extentTest.info("Execution Time: " + duration + " ms");

        // Close context after each test
        if (context != null) {
            context.close();
        }
    }

    @AfterSuite
    public void teardownSuite() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        if (extentReports != null) {
            extentReports.flush();
            System.out.println("Extent Report generated at: " + reportPath);
        }
    }

    /**
     * Captures a screenshot and returns the file path.
     */
    protected String captureScreenshot(String testName) {
        try {
            String screenshotDir = "EPMCDMETST-52763/screenshots";
            File dir = new File(screenshotDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String screenshotPath = screenshotDir + "/" + testName + "_" + System.currentTimeMillis() + ".png";
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(screenshotPath))
                    .setFullPage(true));
            return screenshotPath;
        } catch (Exception e) {
            System.out.println("Failed to capture screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logs an info message to the Extent Report.
     */
    protected void logInfo(String message) {
        if (extentTest != null) {
            extentTest.log(Status.INFO, message);
        }
        System.out.println("INFO: " + message);
    }

    /**
     * Logs a step to the Extent Report.
     */
    protected void logStep(String step) {
        if (extentTest != null) {
            extentTest.log(Status.INFO, "STEP: " + step);
        }
        System.out.println("STEP: " + step);
    }

    /**
     * Returns the static report path for external access.
     */
    public static String getReportPath() {
        return reportPath;
    }

    /**
     * Perform login and return TasksPage.
     */
    protected TasksPage loginAndGetTasksPage() {
        LoginPage loginPage = new LoginPage(page, BASE_URL);
        loginPage.navigate();
        return loginPage.login(getTestUsername(), DEFAULT_PASSWORD);
    }
}
