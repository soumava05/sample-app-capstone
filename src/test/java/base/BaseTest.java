package base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Path;

public abstract class BaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected String baseUrl;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        baseUrl = System.getProperty("baseUrl", "http://localhost:8090");

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true"))));
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900));
        page = context.newPage();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            // Save screenshot on failure for Extent attachment
            if (!result.isSuccess() && page != null) {
                String fileName = "failure_" + result.getName() + "_" + System.currentTimeMillis() + ".png";
                Path screenshotPath = Path.of("target", "playwright-screenshots", fileName);
                screenshotPath.toFile().getParentFile().mkdirs();
                page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath).setFullPage(true));
                result.setAttribute("screenshotPath", screenshotPath.toString());
            }
        } finally {
            if (context != null) context.close();
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
        }
    }
}
