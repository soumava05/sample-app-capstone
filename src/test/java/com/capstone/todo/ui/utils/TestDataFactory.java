package com.capstone.todo.ui.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class TestDataFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private TestDataFactory() {
    }

    public static String uniqueUsername(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static String uniqueTaskTitle(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    public static String today() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String plusDays(long days) {
        return LocalDate.now().plusDays(days).format(DATE_FORMATTER);
    }

    public static String minusDays(long days) {
        return LocalDate.now().minusDays(days).format(DATE_FORMATTER);
    }
}
