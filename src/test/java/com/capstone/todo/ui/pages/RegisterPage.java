package com.capstone.todo.ui.pages;

import com.microsoft.playwright.Page;

public class RegisterPage {

    private final Page page;

    public RegisterPage(Page page) {
        this.page = page;
    }

    public RegisterPage open(String baseUrl) {
        page.navigate(baseUrl + "/register");
        return this;
    }

    public RegisterPage register(String username, String fullName, String password, String confirmPassword) {
        page.locator("#username").fill(username);
        page.locator("#fullName").fill(fullName);
        page.locator("#password").fill(password);
        page.locator("#confirmPassword").fill(confirmPassword);
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Register")).click();
        return this;
    }
}
