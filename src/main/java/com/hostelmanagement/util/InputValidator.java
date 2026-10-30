package com.hostelmanagement.util;

import com.hostelmanagement.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Validates inputs for forms, domain rules, and security.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,12}$");
    private static final Pattern REGISTER_NO_PATTERN = Pattern.compile("^[A-Za-z0-9]{5,20}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");

    private InputValidator() {}

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validateUsername(String username) {
        requireNonEmpty(username, "Username");
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException("Username must be 3-30 characters long (letters, numbers, '.', '_', '-').");
        }
    }

    public static void validateRegisterNumber(String regNo) {
        requireNonEmpty(regNo, "Register number");
        if (!REGISTER_NO_PATTERN.matcher(regNo.trim()).matches()) {
            throw new ValidationException("Register number must be 5-20 alphanumeric characters.");
        }
    }

    public static void validateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new ValidationException("Invalid email address format.");
            }
        }
    }

    public static void validatePhone(String phone, String fieldName) {
        if (phone != null && !phone.trim().isEmpty()) {
            String digits = phone.replaceAll("[^0-9]", "");
            if (!PHONE_PATTERN.matcher(digits).matches()) {
                throw new ValidationException(fieldName + " must contain 10-12 numeric digits.");
            }
        }
    }

    public static void validateLeaveDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Both start date and end date are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("End date cannot be earlier than start date.");
        }
        if (startDate.isBefore(LocalDate.now().minusDays(1))) {
            throw new ValidationException("Start date cannot be in the past.");
        }
    }

    public static void validatePositiveAmount(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName + " must be a positive amount greater than zero.");
        }
    }

    public static void validateNonNegativeAmount(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }
}
