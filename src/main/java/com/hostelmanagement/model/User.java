package com.hostelmanagement.model;

import com.hostelmanagement.enums.UserRole;
import java.time.LocalDateTime;

/**
 * Base class for all system users (Administrator, Warden, Student).
 *
 * Demonstrates OOP INHERITANCE: common user fields and behavior are defined
 * here and specialized in subclasses. Password hash is stored, never plaintext.
 */
public abstract class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────

    protected User() {}

    protected User(String username, String fullName, String email, UserRole role) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }

    // ── Encapsulated Accessors ────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Polymorphic method — each role provides a display-friendly description.
     */
    public abstract String getRoleDescription();

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
