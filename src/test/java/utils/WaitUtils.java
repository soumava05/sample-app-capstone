package utils;

import com.microsoft.playwright.Page;

public final class WaitUtils {
    private WaitUtils() {}

    public static void waitForDomReady(Page page) {
        page.waitForLoadState();
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }
}
