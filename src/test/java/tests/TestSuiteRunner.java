package tests;

import org.testng.TestNG;

public class TestSuiteRunner {
    public static void main(String[] args) {
        TestNG testng = new TestNG();
        testng.setTestSuites(java.util.List.of("src/test/resources/testng.xml"));
        testng.run();
    }
}
