package utils;

public final class TestData {
    private TestData() {}

    public static String uniqueUsername() {
        return "uiuser_" + System.currentTimeMillis();
    }

    public static String validPassword() {
        return System.getProperty("ui.password", "Password123!");
    }
}
