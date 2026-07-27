package com.capstone.todo.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public final class ExtentManager {

    private static ExtentReports extentReports;
    private static String reportPath;

    private ExtentManager() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            reportPath = "EDMCDMETST-55979/automation_run_" + System.currentTimeMillis() + ".html";
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setReportName("EDMCDMETST-55979 UI Automation Report");
            sparkReporter.config().setDocumentTitle("Task Dashboard Filtering Execution Report");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            extentReports.setSystemInfo("Application", "Todo App");
            extentReports.setSystemInfo("Feature", "Task Dashboard Filtering");
            extentReports.setSystemInfo("Base URL", ConfigManager.getBaseUrl());
            extentReports.setSystemInfo("Browser", ConfigManager.getBrowser());
            extentReports.setSystemInfo("Headless", String.valueOf(ConfigManager.isHeadless()));
        }
        return extentReports;
    }

    public static String getReportPath() {
        getInstance();
        return reportPath;
    }
}
