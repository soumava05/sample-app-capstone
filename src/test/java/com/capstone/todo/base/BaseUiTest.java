package com.capstone.todo.base;

import com.capstone.todo.utils.ConfigManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class BaseUiTest {

    private Playwright playwright;
    private Browser browser;
    protected Page page;

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        playwright = Playwright.create();
        BrowserType browserType = resolveBrowserType(playwright);
        browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(ConfigManager.isHeadless()));
        page = browser.newPage();
        page.navigate(ConfigManager.getBaseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    public Page getPage() {
        return page;
    }

    private BrowserType resolveBrowserType(Playwright playwright) {
        return switch (ConfigManager.getBrowser().toLowerCase()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
    }
}
