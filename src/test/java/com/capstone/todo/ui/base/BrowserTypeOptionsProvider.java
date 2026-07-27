package com.capstone.todo.ui.base;

import com.microsoft.playwright.BrowserType;

public class BrowserTypeOptionsProvider {

    public BrowserType.LaunchOptions headless() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        return new BrowserType.LaunchOptions().setHeadless(headless);
    }
}
