package com.hostelmanagement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }
}
