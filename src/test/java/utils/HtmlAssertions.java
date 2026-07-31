package utils;

import com.microsoft.playwright.Page;
import org.testng.Assert;

public final class HtmlAssertions {
    private HtmlAssertions() {}

    public static void assertNotJsonEnvelope(Page page) {
        String content = page.content();
        // Heuristic checks to ensure JSON envelope isn't rendered to UI
        Assert.assertFalse(content.contains("\"correlationId\""), "UI rendered JSON key correlationId");
        Assert.assertFalse(content.contains("\"error\""), "UI rendered JSON key error");
        Assert.assertFalse(content.contains("application/json"), "UI seems to expose JSON content type");
    }

    public static void assertNoStackTrace(Page page) {
        String content = page.content();
        Assert.assertFalse(content.contains("Exception"), "UI appears to leak exception text");
        Assert.assertFalse(content.contains("at com."), "UI appears to leak Java stack trace");
        Assert.assertFalse(content.contains("java.lang"), "UI appears to leak Java package");
    }
}
