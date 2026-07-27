package com.capstone.todo.pages;

import com.microsoft.playwright.Page;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public LoginPage open() {
        page.navigate(com.capstone.todo.utils.ConfigManager.getBaseUrl() + "/login");
        return this;
    }

    public LoginPage enterUsername(String username) {
        page.locator("#username").fill(username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        page.locator("#password").fill(password);
        return this;
    }

    public void submit() {
        page.locator("button[type='submit']").click();
    }
}
