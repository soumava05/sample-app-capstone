package com.capstone.todo.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Page Object for Login Page.
 * Handles user authentication actions.
 */
public class LoginPage {

    private final Page page;
    private final String baseUrl;

    // Locators
    private static final String USERNAME_INPUT = "#username";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "button[type='submit']";
    private static final String ERROR_MESSAGE = ".alert.error";
    private static final String REGISTER_LINK = "a[href='/register']";

    public LoginPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    /**
     * Navigate to the login page.
     */
    public LoginPage navigate() {
        page.navigate(baseUrl + "/login");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Enter username.
     */
    public LoginPage enterUsername(String username) {
        page.fill(USERNAME_INPUT, username);
        return this;
    }

    /**
     * Enter password.
     */
    public LoginPage enterPassword(String password) {
        page.fill(PASSWORD_INPUT, password);
        return this;
    }

    /**
     * Click login button.
     */
    public void clickLogin() {
        page.click(LOGIN_BUTTON);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Perform complete login action.
     */
    public TasksPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        return new TasksPage(page, baseUrl);
    }

    /**
     * Check if error message is displayed.
     */
    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
    }

    /**
     * Get error message text.
     */
    public String getErrorMessage() {
        return page.textContent(ERROR_MESSAGE);
    }

    /**
     * Check if login page is displayed.
     */
    public boolean isDisplayed() {
        return page.isVisible(USERNAME_INPUT) && page.isVisible(PASSWORD_INPUT);
    }
}
