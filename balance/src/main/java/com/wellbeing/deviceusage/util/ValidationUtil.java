package com.wellbeing.deviceusage.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Regex for email validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    // Regex for package name validation (com.example.app format)
    private static final Pattern PACKAGE_NAME_PATTERN =
            Pattern.compile("^([a-z][a-z0-9_]*\\.)+[a-z][a-z0-9_]*$");

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate package name format
     */
    public static boolean isValidPackageName(String packageName) {
        return packageName != null && PACKAGE_NAME_PATTERN.matcher(packageName).matches();
    }

    /**
     * Validate that a string is not null or empty
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validate that a number is positive
     */
    public static boolean isPositive(Integer number) {
        return number != null && number > 0;
    }

    /**
     * Validate days of week format (comma separated list of numbers 1-7)
     */
    public static boolean isValidDaysOfWeek(String daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return true; // Empty is valid (means all days)
        }

        try {
            String[] parts = daysOfWeek.split(",");
            for (String part : parts) {
                int day = Integer.parseInt(part.trim());
                if (day < 1 || day > 7) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}