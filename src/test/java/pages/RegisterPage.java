package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utils.WaitUtils;

public class RegisterPage {

    private final Page page;

    private final Locator username;
    private final Locator fullName;
    private final Locator password;
    private final Locator confirmPassword;
    private final Locator registerButton;

    private final Locator fieldErrors;
    private final Locator globalErrors;

    public RegisterPage(Page page) {
        this.page = page;
        this.username = page.locator("#username");
        this.fullName = page.locator("#fullName");
        this.password = page.locator("#password");
        this.confirmPassword = page.locator("#confirmPassword");
        this.registerButton = page.locator("button[type='submit']");
        this.fieldErrors = page.locator(".field-error:visible");
        this.globalErrors = page.locator(".alert.error:visible");
    }

    public void open(String baseUrl) {
        page.navigate(baseUrl + "/register");
        WaitUtils.waitForDomReady(page);
    }

    /**
     * Trigger browser constraint validation (required) in a deterministic way.
     * This does not hit server side, but validates UI remains in HTML and does not leak JSON.
     */
    public void submitWithBlankRequiredFields() {
        username.fill("");
        fullName.fill("");
        password.fill("");
        confirmPassword.fill("");
        registerButton.click();
        // No navigation expected because browser blocks submission
    }

    /**
     * Hit server-side @Valid by bypassing browser 'required' constraints with non-empty values.
     */
    public void submitServerSideInvalid() {
        username.fill("a");
        fullName.fill("a");
        // too short, likely fails Size constraints if present
        password.fill("a");
        confirmPassword.fill("b");
        registerButton.click();
        WaitUtils.waitForDomReady(page);
    }

    public void fillAndSubmit(String u, String fn, String pw, String cpw) {
        username.fill(u);
        fullName.fill(fn);
        password.fill(pw);
        confirmPassword.fill(cpw);
        registerButton.click();
        WaitUtils.waitForDomReady(page);
    }

    public int fieldErrorCount() {
        return (int) fieldErrors.count();
    }

    public int globalErrorCount() {
        return (int) globalErrors.count();
    }

    public boolean hasAnyFieldErrors() {
        return fieldErrorCount() > 0;
    }

    public boolean hasGlobalErrors() {
        return globalErrorCount() > 0;
    }
}
