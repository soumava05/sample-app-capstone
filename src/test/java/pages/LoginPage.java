package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utils.WaitUtils;

public class LoginPage {

    private final Page page;

    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator signInButton;

    private final Locator invalidCredentialsAlert;
    private final Locator registeredSuccessAlert;

    public LoginPage(Page page) {
        this.page = page;
        this.usernameInput = page.locator("#username");
        this.passwordInput = page.locator("#password");
        this.signInButton = page.locator("button[type='submit']");
        this.invalidCredentialsAlert = page.locator(".alert.error");
        this.registeredSuccessAlert = page.locator(".alert.success");
    }

    public void open(String baseUrl) {
        page.navigate(baseUrl + "/login");
        WaitUtils.waitForDomReady(page);
    }

    public void login(String username, String password) {
        usernameInput.fill(username);
        passwordInput.fill(password);
        signInButton.click();
        WaitUtils.waitForDomReady(page);
    }

    public boolean isInvalidCredentialsDisplayed() {
        return invalidCredentialsAlert.isVisible();
    }

    public boolean isRegisteredSuccessDisplayed() {
        return registeredSuccessAlert.isVisible();
    }
}
