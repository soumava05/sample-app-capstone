package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.util.HashMap;
import java.util.Map;

public final class ExtentManager {

    private static ExtentReports extent;

    private ExtentManager() {}

    public static synchronized ExtentReports getInstance(String reportPath) {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setDocumentTitle("UI Automation Report");
            spark.config().setReportName("EPMCDMETST-51892 - UI Error Handling");

            extent = new ExtentReports();
            extent.attachReporter(spark);

            Map<String, String> env = new HashMap<>();
            env.put("BaseUrl", System.getProperty("baseUrl", "http://localhost:8090"));
            env.put("Browser", System.getProperty("browser", "chromium"));
            env.put("Headless", System.getProperty("headless", "true"));
            extent.setSystemInfo("Environment", env.toString());
        }
        return extent;
    }
}
