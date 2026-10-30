package com.hostelmanagement.security;

import com.hostelmanagement.model.User;
import com.hostelmanagement.enums.UserRole;

/**
 * Holds the currently logged-in user session.
 * Provides role-checking methods used by the service layer
 * to enforce authorization — not only by hiding UI buttons.
 */
public class SessionManager {
    private static User currentUser;

    private SessionManager() {}

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    public static boolean isWarden() {
        return hasRole(UserRole.WARDEN);
    }

    public static boolean isStudent() {
        return hasRole(UserRole.STUDENT);
    }

    public static boolean isAdminOrWarden() {
        return isAdmin() || isWarden();
    }

    /**
     * Throws if the current user does not have the required role.
     * Called in the service layer to enforce authorization.
     */
    public static void requireRole(UserRole... allowedRoles) {
        if (currentUser == null) {
            throw new SecurityException("Not authenticated. Please log in.");
        }
        for (UserRole role : allowedRoles) {
            if (currentUser.getRole() == role) return;
        }
        throw new SecurityException("Access denied. Required role: "
            + java.util.Arrays.toString(allowedRoles)
            + ", your role: " + currentUser.getRole());
    }
}
