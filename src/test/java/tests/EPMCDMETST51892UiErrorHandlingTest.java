package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.RegisterPage;
import utils.HtmlAssertions;

public class EPMCDMETST51892UiErrorHandlingTest extends BaseTest {

    @Test(description = "TC_EPMCDMETST-51892_001: UI validation failure shows user-friendly feedback (no JSON)")
    public void uiValidationFailureOnRegisterShowsErrorsNotJson() {
        RegisterPage registerPage = new RegisterPage(page);
        registerPage.open(baseUrl);

        // Ensure we hit server-side validation by bypassing browser required checks
        registerPage.submitServerSideInvalid();

        Assert.assertTrue(registerPage.hasAnyFieldErrors() || registerPage.hasGlobalErrors(),
                "Expected validation errors on Register page");
        HtmlAssertions.assertNotJsonEnvelope(page);
        HtmlAssertions.assertNoStackTrace(page);
    }

    @Test(description = "TC_EPMCDMETST-51892_001 (browser-level): Required fields prevent submission and stay on HTML")
    public void uiBrowserConstraintValidationStaysHtmlAndNotJson() {
        RegisterPage registerPage = new RegisterPage(page);
        registerPage.open(baseUrl);

        registerPage.submitWithBlankRequiredFields();

        Assert.assertTrue(page.url().contains("/register"), "Expected to remain on /register");
        HtmlAssertions.assertNotJsonEnvelope(page);
    }

    @Test(description = "TC_EPMCDMETST-51892_002: UI missing entity shows friendly not found outcome (no JSON)")
    public void uiMissingTaskCompleteDoesNotRenderJson() {
        page.navigate(baseUrl + "/tasks/non-existing-id/complete");
        page.waitForLoadState();

        String content = page.content();
        Assert.assertTrue(content.contains("Login") || content.contains("Sign In") || content.contains("Welcome Back"),
                "Expected UI-friendly HTML response (likely login page)");
        HtmlAssertions.assertNotJsonEnvelope(page);
    }

    @Test(description = "TC_EPMCDMETST-51892_004: UI endpoints should not leak JSON keys like correlationId")
    public void uiPagesDoNotContainJsonEnvelopeKeys() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.open(baseUrl);
        HtmlAssertions.assertNotJsonEnvelope(page);

        RegisterPage registerPage = new RegisterPage(page);
        registerPage.open(baseUrl);
        HtmlAssertions.assertNotJsonEnvelope(page);
    }

    @Test(description = "TC_EPMCDMETST-51892_005: UI preserves navigation safety after errors")
    public void uiErrorOutcomeStillRendersHtmlPage() {
        RegisterPage registerPage = new RegisterPage(page);
        registerPage.open(baseUrl);
        registerPage.submitServerSideInvalid();

        Assert.assertTrue(page.locator("form").count() > 0, "Expected UI form still rendered");
        HtmlAssertions.assertNoStackTrace(page);
    }
}
