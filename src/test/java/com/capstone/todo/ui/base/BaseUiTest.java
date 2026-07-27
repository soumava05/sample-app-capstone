package com.capstone.todo.ui.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ScreenshotType;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public abstract class BaseUiTest {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8090");
    protected static final String BROWSER_NAME = System.getProperty("browser", "chromium");

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeSuite(alwaysRun = true)
    @Parameters({"browser"})
    public void beforeSuite(@Optional("chromium") String browserName) {
        if (playwright != null && browser != null) {
            return;
        }

        playwright = Playwright.create();
        String effectiveBrowser = System.getProperty("browser", browserName);
        browser = switch (effectiveBrowser.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(new BrowserTypeOptionsProvider().headless());
            case "webkit" -> playwright.webkit().launch(new BrowserTypeOptionsProvider().headless());
            default -> playwright.chromium().launch(new BrowserTypeOptionsProvider().headless());
        };
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(Duration.ofSeconds(10).toMillis());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (!result.isSuccess() && page != null) {
            try {
                Path screenshotDir = Path.of("target", "ui-screenshots");
                Files.createDirectories(screenshotDir);
                String fileName = result.getName() + ".png";
                page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotDir.resolve(fileName))
                    .setType(ScreenshotType.PNG)
                    .setFullPage(true));
            } catch (IOException ignored) {
            }
        }

        if (context != null) {
            context.close();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}
