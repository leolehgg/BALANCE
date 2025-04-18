package com.wellbeing.deviceusage.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class DateTimeUtil {

    /**
     * Get the start of the week (Monday) for the given date
     */
    public static LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Get the end of the week (Sunday) for the given date
     */
    public static LocalDate getEndOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Calculate duration in seconds between two LocalDateTime objects
     */
    public static long durationInSeconds(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * Parse days of week string (e.g. "1,2,3,4,5") to a list of DayOfWeek
     */
    public static List<DayOfWeek> parseDaysOfWeek(String daysOfWeekStr) {
        List<DayOfWeek> result = new ArrayList<>();

        if (daysOfWeekStr == null || daysOfWeekStr.isEmpty()) {
            return result;
        }

        String[] parts = daysOfWeekStr.split(",");
        for (String part : parts) {
            int dayValue = Integer.parseInt(part.trim());
            result.add(DayOfWeek.of(dayValue));
        }

        return result;
    }

    /**
     * Format a list of DayOfWeek to a string (e.g. "1,2,3,4,5")
     */
    public static String formatDaysOfWeek(List<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (DayOfWeek day : days) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(day.getValue());
        }

        return sb.toString();
    }

    /**
     * Check if a date falls on one of the days in the days of week string
     */
    public static boolean isDateInDaysOfWeek(LocalDate date, String daysOfWeekStr) {
        if (daysOfWeekStr == null || daysOfWeekStr.isEmpty()) {
            return true; // If no days specified, apply to all days
        }

        List<DayOfWeek> daysOfWeek = parseDaysOfWeek(daysOfWeekStr);
        return daysOfWeek.contains(date.getDayOfWeek());
    }
}