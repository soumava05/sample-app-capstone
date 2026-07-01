package com.capstone.todo.automation.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG Listener for custom test event handling.
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("========================================");
        System.out.println("Starting Test: " + result.getMethod().getMethodName());
        System.out.println("Description: " + result.getMethod().getDescription());
        System.out.println("========================================");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("✓ Test PASSED: " + result.getMethod().getMethodName());
        System.out.println("Duration: " + (result.getEndMillis() - result.getStartMillis()) + " ms");
        System.out.println("----------------------------------------");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("✗ Test FAILED: " + result.getMethod().getMethodName());
        System.out.println("Error: " + result.getThrowable().getMessage());
        System.out.println("Duration: " + (result.getEndMillis() - result.getStartMillis()) + " ms");
        System.out.println("----------------------------------------");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⊘ Test SKIPPED: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            System.out.println("Reason: " + result.getThrowable().getMessage());
        }
        System.out.println("----------------------------------------");
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("========================================");
        System.out.println("TEST SUITE STARTED: " + context.getName());
        System.out.println("Start Time: " + context.getStartDate());
        System.out.println("========================================");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("========================================");
        System.out.println("TEST SUITE FINISHED: " + context.getName());
        System.out.println("Total Tests: " + (context.getPassedTests().size() + 
                                              context.getFailedTests().size() + 
                                              context.getSkippedTests().size()));
        System.out.println("Passed: " + context.getPassedTests().size());
        System.out.println("Failed: " + context.getFailedTests().size());
        System.out.println("Skipped: " + context.getSkippedTests().size());
        System.out.println("End Time: " + context.getEndDate());
        System.out.println("========================================");
    }
}
