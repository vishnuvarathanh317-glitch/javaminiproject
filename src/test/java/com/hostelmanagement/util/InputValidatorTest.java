package com.hostelmanagement.util;

import com.hostelmanagement.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Input Validator (Form Validation & Sanitization)")
class InputValidatorTest {

    @Test
    @DisplayName("Empty or blank strings trigger ValidationException")
    void testRequireNonEmpty() {
        assertThrows(ValidationException.class, () -> InputValidator.requireNonEmpty(null, "TestField"));
        assertThrows(ValidationException.class, () -> InputValidator.requireNonEmpty("   ", "TestField"));
        assertDoesNotThrow(() -> InputValidator.requireNonEmpty("ValidContent", "TestField"));
    }

    @Test
    @DisplayName("Email format validation passes correct emails and catches invalid patterns")
    void testValidateEmail() {
        assertDoesNotThrow(() -> InputValidator.validateEmail("student@university.edu"));
        assertDoesNotThrow(() -> InputValidator.validateEmail("john.doe+hostel@domain.co.in"));

        assertThrows(ValidationException.class, () -> InputValidator.validateEmail("plainaddress"));
        assertThrows(ValidationException.class, () -> InputValidator.validateEmail("@missinguser.com"));
        assertThrows(ValidationException.class, () -> InputValidator.validateEmail("user@.com.my"));
    }

    @Test
    @DisplayName("Phone number validates 10-12 digits and ignores formatting punctuation")
    void testValidatePhone() {
        assertDoesNotThrow(() -> InputValidator.validatePhone("9876543210", "Phone"));
        assertDoesNotThrow(() -> InputValidator.validatePhone("+91 98765 43210", "Phone"));

        assertThrows(ValidationException.class, () -> InputValidator.validatePhone("12345", "Phone")); // Too short
        assertThrows(ValidationException.class, () -> InputValidator.validatePhone("123456789012345", "Phone")); // Too long
    }

    @Test
    @DisplayName("Leave dates require non-null, future or current start, and end date >= start date")
    void testValidateLeaveDates() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusDays(7);

        assertDoesNotThrow(() -> InputValidator.validateLeaveDates(tomorrow, nextWeek));
        assertDoesNotThrow(() -> InputValidator.validateLeaveDates(today, today));

        // End before start
        assertThrows(ValidationException.class, () -> InputValidator.validateLeaveDates(nextWeek, tomorrow));

        // Start in the distant past
        assertThrows(ValidationException.class, () -> InputValidator.validateLeaveDates(today.minusDays(5), tomorrow));
    }

    @Test
    @DisplayName("Amount validations handle positive and non-negative numbers correctly")
    void testValidateAmounts() {
        assertDoesNotThrow(() -> InputValidator.validatePositiveAmount(new BigDecimal("100.00"), "Fee"));
        assertThrows(ValidationException.class, () -> InputValidator.validatePositiveAmount(BigDecimal.ZERO, "Fee"));
        assertThrows(ValidationException.class, () -> InputValidator.validatePositiveAmount(new BigDecimal("-10.00"), "Fee"));

        assertDoesNotThrow(() -> InputValidator.validateNonNegativeAmount(BigDecimal.ZERO, "Fee"));
        assertDoesNotThrow(() -> InputValidator.validateNonNegativeAmount(new BigDecimal("50.00"), "Fee"));
        assertThrows(ValidationException.class, () -> InputValidator.validateNonNegativeAmount(new BigDecimal("-0.01"), "Fee"));
    }
}
