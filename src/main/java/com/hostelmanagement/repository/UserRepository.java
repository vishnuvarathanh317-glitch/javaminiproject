package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for users table. Uses PreparedStatement and try-with-resources.
 * Demonstrates the Repository pattern — separates data access from business logic.
 */
public class UserRepository {

    /** Insert a new user. Returns the generated ID. */
    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setBoolean(7, user.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated user ID");
    }

    /** Find user by username (for login). */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** Find user by ID. */
    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** List all users, optionally filtered by role. */
    public List<User> findAll(UserRole roleFilter) throws SQLException {
        String sql = roleFilter == null
                ? "SELECT * FROM users ORDER BY full_name"
                : "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (roleFilter != null) {
                ps.setString(1, roleFilter.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    /** Update user details (not password). */
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name=?, email=?, phone=?, active=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        }
    }

    /** Update password hash only. */
    public void updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Count users by role. */
    public int countByRole(UserRole role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ? AND active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Check if any admin user exists (for first-run setup). */
    public boolean adminExists() throws SQLException {
        return countByRole(UserRole.ADMIN) > 0;
    }

    /** Map a ResultSet row to the appropriate User subclass. */
    private User mapRow(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));
        User user;
        switch (role) {
            case ADMIN -> user = new Administrator();
            case WARDEN -> user = new Warden();
            case STUDENT -> user = new Student();
            default -> throw new SQLException("Unknown role: " + role);
        }
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(role);
        user.setActive(rs.getBoolean("active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) user.setUpdatedAt(updated.toLocalDateTime());
        return user;
    }
}
