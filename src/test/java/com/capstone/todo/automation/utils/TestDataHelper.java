package com.capstone.todo.automation.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Utility class for generating test data.
 */
public class TestDataHelper {

    private static final Random random = new Random();

    /**
     * Generate a unique task title with timestamp.
     */
    public static String generateUniqueTaskTitle(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    /**
     * Generate a random task title.
     */
    public static String generateRandomTaskTitle() {
        String[] prefixes = {"Task", "Meeting", "Review", "Urgent", "Project", "Sprint"};
        String[] suffixes = {"Alpha", "Beta", "Gamma", "Delta", "Omega"};
        return prefixes[random.nextInt(prefixes.length)] + " " + 
               suffixes[random.nextInt(suffixes.length)] + " " + 
               random.nextInt(1000);
    }

    /**
     * Get today's date formatted as ISO date.
     */
    public static String getTodayFormatted() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get a future date formatted as ISO date.
     */
    public static String getFutureDateFormatted(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Generate test description.
     */
    public static String generateDescription(String keyword) {
        return "Test description containing " + keyword + " for testing purposes. Generated at " + 
               System.currentTimeMillis();
    }
}
