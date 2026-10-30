package com.hostelmanagement.model;

import com.hostelmanagement.enums.UserRole;

/**
 * Administrator — extends User with full system access.
 * Demonstrates INHERITANCE from User.
 */
public class Administrator extends User {

    public Administrator() {
        setRole(UserRole.ADMIN);
    }

    public Administrator(String username, String fullName, String email) {
        super(username, fullName, email, UserRole.ADMIN);
    }

    /** Polymorphic role description. */
    @Override
    public String getRoleDescription() {
        return "System Administrator — full access to manage the hostel system";
    }
}
