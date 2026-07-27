package com.capstone.todo.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.capstone.todo.base.BaseUiTest;
import com.capstone.todo.utils.ExtentManager;
import com.microsoft.playwright.Page;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExtentTestListener implements ITestListener {

    private static final ThreadLocal<ExtentTest> EXTENT_TEST = new ThreadLocal<>();
    private final ExtentReports extentReports = ExtentManager.getInstance();

    @Override
    public void onStart(ITestContext context) {
        extentReports.setSystemInfo("Suite", context.getSuite().getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extentReports.createTest(result.getMethod().getMethodName());
        test.log(Status.INFO, "Test execution started");
        EXTENT_TEST.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        EXTENT_TEST.get().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = EXTENT_TEST.get();
        test.log(Status.FAIL, result.getThrowable());
        Object instance = result.getInstance();
        if (instance instanceof BaseUiTest baseUiTest) {
            Page page = baseUiTest.getPage();
            if (page != null) {
                try {
                    Path screenshotDirectory = Path.of("target", "screenshots");
                    Files.createDirectories(screenshotDirectory);
                    Path screenshotPath = screenshotDirectory.resolve(result.getMethod().getMethodName() + "_" + System.currentTimeMillis() + ".png");
                    page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));
                    test.addScreenCaptureFromPath(screenshotPath.toString());
                } catch (IOException exception) {
                    test.log(Status.WARNING, "Unable to capture screenshot: " + exception.getMessage());
                }
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        EXTENT_TEST.get().log(Status.SKIP, "Test skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        extentReports.flush();
    }
}
