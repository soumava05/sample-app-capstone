package com.capstone.todo.ui.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;
import java.time.Instant;

public class ExtentTestNgListener implements ITestListener {

    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();
    private static String reportPath;

    @Override
    public void onStart(ITestContext context) {
        reportPath = Path.of("EPMCDMETST-55879", "automation_run_" + Instant.now().toEpochMilli() + ".html").toString();
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Base URL", System.getProperty("baseUrl", "http://localhost:8090"));
        extentReports.setSystemInfo("Browser", System.getProperty("browser", "chromium"));
        extentReports.setSystemInfo("Headless", System.getProperty("headless", "true"));
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extentReports.createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        TEST.set(extentTest);
        extentTest.info("Test started: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TEST.get().pass("Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = TEST.get();
        test.fail(result.getThrowable());
        Path screenshotPath = Path.of("target", "ui-screenshots", result.getMethod().getMethodName() + ".png");
        if (screenshotPath.toFile().exists()) {
            try {
                test.fail("Screenshot on failure",
                    MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath.toString()).build());
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TEST.get().skip("Test skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    public static String getReportPath() {
        return reportPath;
    }
}
