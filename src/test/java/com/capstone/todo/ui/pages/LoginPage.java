package com.capstone.todo.ui.pages;

import com.microsoft.playwright.Page;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public LoginPage open(String baseUrl) {
        page.navigate(baseUrl + "/login");
        return this;
    }

    public LoginPage login(String username, String password) {
        page.locator("#username").fill(username);
        page.locator("#password").fill(password);
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Sign In")).click();
        return this;
    }

    public boolean isDisplayed() {
        return page.locator("h1:text('Welcome Back')").isVisible();
    }

    public String getErrorMessage() {
        return page.locator(".alert.error").textContent();
    }
}
