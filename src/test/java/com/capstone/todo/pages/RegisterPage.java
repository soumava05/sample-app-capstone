package com.capstone.todo.pages;

import com.capstone.todo.utils.ConfigManager;
import com.microsoft.playwright.Page;

public class RegisterPage {

    private final Page page;

    public RegisterPage(Page page) {
        this.page = page;
    }

    public RegisterPage open() {
        page.navigate(ConfigManager.getBaseUrl() + "/register");
        return this;
    }

    public RegisterPage enterUsername(String username) {
        page.locator("#username").fill(username);
        return this;
    }

    public RegisterPage enterFullName(String fullName) {
        page.locator("#fullName").fill(fullName);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        page.locator("#password").fill(password);
        return this;
    }

    public RegisterPage enterConfirmPassword(String confirmPassword) {
        page.locator("#confirmPassword").fill(confirmPassword);
        return this;
    }

    public void submit() {
        page.locator("button[type='submit']").click();
    }
}
