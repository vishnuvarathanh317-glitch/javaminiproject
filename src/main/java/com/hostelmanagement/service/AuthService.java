package com.hostelmanagement.service;

import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.exception.AuthenticationException;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.Administrator;
import com.hostelmanagement.model.User;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.security.PasswordUtil;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.util.InputValidator;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Authentication and Session Service.
 * Enforces secure salted password hashing, active account checks, and session management.
 */
public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean hasAnyAdmin() {
        try {
            return userRepository.adminExists();
        } catch (SQLException e) {
            throw new DatabaseException("Database error checking admin accounts: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a user by username and plaintext password.
     */
    public User login(String username, String password) {
        InputValidator.requireNonEmpty(username, "Username");
        InputValidator.requireNonEmpty(password, "Password");

        try {
            Optional<User> opt = userRepository.findByUsername(username.trim());
            if (opt.isEmpty()) {
                throw new AuthenticationException("Invalid username or password.");
            }

            User user = opt.get();
            if (!user.isActive()) {
                throw new AuthenticationException("Account has been deactivated. Please contact the administrator.");
            }

            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                throw new AuthenticationException("Invalid username or password.");
            }

            SessionManager.login(user);
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Database error during authentication: " + e.getMessage(), e);
        }
    }

    public void logout() {
        SessionManager.logout();
    }

    /**
     * First-run administrator wizard setup.
     */
    public Administrator createInitialAdmin(String username, String password, String fullName, String email) {
        InputValidator.validateUsername(username);
        InputValidator.requireNonEmpty(password, "Password");
        InputValidator.requireNonEmpty(fullName, "Full name");
        InputValidator.validateEmail(email);

        if (password.length() < 6) {
            throw new ValidationException("Administrator password must be at least 6 characters.");
        }

        try {
            if (userRepository.adminExists()) {
                throw new ValidationException("An administrator account already exists. Use standard login.");
            }

            if (userRepository.findByUsername(username.trim()).isPresent()) {
                throw new ValidationException("Username already in use.");
            }

            Administrator admin = new Administrator(username.trim(), fullName.trim(), email != null ? email.trim() : null);
            admin.setPasswordHash(PasswordUtil.hashPassword(password));
            admin.setActive(true);

            int userId = userRepository.insert(admin);
            admin.setId(userId);
            return admin;
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating administrator: " + e.getMessage(), e);
        }
    }

    public void changePassword(int userId, String oldPassword, String newPassword) {
        InputValidator.requireNonEmpty(oldPassword, "Current password");
        InputValidator.requireNonEmpty(newPassword, "New password");
        if (newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters.");
        }

        try {
            Optional<User> opt = userRepository.findById(userId);
            if (opt.isEmpty()) {
                throw new AuthenticationException("User not found.");
            }

            User user = opt.get();
            if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Incorrect current password.");
            }

            String newHash = PasswordUtil.hashPassword(newPassword);
            userRepository.updatePassword(userId, newHash);
        } catch (SQLException e) {
            throw new DatabaseException("Database error changing password: " + e.getMessage(), e);
        }
    }
}
