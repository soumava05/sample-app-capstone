package utils;

public final class Config {
    private Config() {}

    public static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8090");

    public static final String DEFAULT_USERNAME = System.getProperty("ui.username", "john");
    public static final String DEFAULT_PASSWORD = System.getProperty("ui.password", "Password123!");
}
