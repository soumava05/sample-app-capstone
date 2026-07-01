package com.capstone.todo.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Page Object for Registration Page.
 * Handles user registration actions.
 */
public class RegistrationPage {

    private final Page page;
    private final String baseUrl;

    // Locators
    private static final String USERNAME_INPUT = "#username";
    private static final String FULL_NAME_INPUT = "#fullName";
    private static final String PASSWORD_INPUT = "#password";
    private static final String CONFIRM_PASSWORD_INPUT = "#confirmPassword";
    private static final String REGISTER_BUTTON = "button[type='submit']";
    private static final String ERROR_MESSAGE = ".alert.error, .field-error";
    private static final String LOGIN_LINK = "a[href='/login']";

    public RegistrationPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    /**
     * Navigate to the registration page.
     */
    public RegistrationPage navigate() {
        page.navigate(baseUrl + "/register");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    /**
     * Fill in registration form and submit.
     */
    public LoginPage register(String username, String fullName, String password) {
        page.waitForSelector(USERNAME_INPUT, new Page.WaitForSelectorOptions().setTimeout(10000));
        page.fill(USERNAME_INPUT, username);
        page.fill(FULL_NAME_INPUT, fullName);
        page.fill(PASSWORD_INPUT, password);
        page.fill(CONFIRM_PASSWORD_INPUT, password);
        page.click(REGISTER_BUTTON);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return new LoginPage(page, baseUrl);
    }

    /**
     * Check if registration was successful (redirected to login with success message).
     */
    public boolean isRegistrationSuccessful() {
        return page.url().contains("/login") && page.url().contains("registered");
    }

    /**
     * Check if registration page is displayed.
     */
    public boolean isDisplayed() {
        try {
            page.waitForSelector(USERNAME_INPUT, new Page.WaitForSelectorOptions().setTimeout(5000));
            return page.isVisible(USERNAME_INPUT) && page.isVisible(FULL_NAME_INPUT);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if error message is displayed.
     */
    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
    }
}
