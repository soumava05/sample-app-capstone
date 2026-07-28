package com.capstone.todo.utils;

public final class ConfigManager {

    private static final String DEFAULT_BASE_URL = "http://localhost:8090";
    private static final String DEFAULT_BROWSER = "chromium";
    private static final boolean DEFAULT_HEADLESS = true;

    private ConfigManager() {
    }

    public static String getBaseUrl() {
        return System.getProperty("base.url", DEFAULT_BASE_URL);
    }

    public static String getBrowser() {
        return System.getProperty("browser", DEFAULT_BROWSER);
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("headless", String.valueOf(DEFAULT_HEADLESS)));
    }
}
