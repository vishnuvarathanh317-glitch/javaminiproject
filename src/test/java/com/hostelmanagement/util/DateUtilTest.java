package com.hostelmanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Date Utility (Standard Formatter Formatting)")
class DateUtilTest {

    @Test
    @DisplayName("Format LocalDate to dd-MM-yyyy format")
    void testFormatDate() {
        LocalDate date = LocalDate.of(2026, 10, 21);
        assertEquals("21-10-2026", DateUtil.formatDate(date));
        assertEquals("", DateUtil.formatDate(null));
    }

    @Test
    @DisplayName("Format LocalDateTime to dd-MM-yyyy HH:mm format")
    void testFormatDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 10, 21, 14, 30);
        assertEquals("21-10-2026 14:30", DateUtil.formatDateTime(dt));
        assertEquals("", DateUtil.formatDateTime(null));
    }
}
