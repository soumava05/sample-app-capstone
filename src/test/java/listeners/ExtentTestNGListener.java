package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.testng.*;

import java.io.File;

public class ExtentTestNGListener implements ITestListener, ISuiteListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ISuite suite) {
        long epoch = System.currentTimeMillis();
        String jira = "EPMCDMETST-51892";
        String reportPath = jira + File.separator + "automation_run_" + epoch + ".html";
        extent = ExtentManager.getInstance(reportPath);
    }

    @Override
    public void onFinish(ISuite suite) {
        if (extent != null) {
            extent.flush();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName());
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest t = test.get();
        t.fail(result.getThrowable());

        Object screenshotPath = result.getAttribute("screenshotPath");
        if (screenshotPath != null) {
            try {
                t.fail("Screenshot on failure",
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath.toString()).build());
            } catch (Exception e) {
                t.warning("Could not attach screenshot: " + e.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("Test skipped: " + (result.getThrowable() != null ? result.getThrowable().getMessage() : ""));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }

    @Override
    public void onStart(ITestContext context) {
        // no-op
    }

    @Override
    public void onFinish(ITestContext context) {
        // no-op
    }
}
