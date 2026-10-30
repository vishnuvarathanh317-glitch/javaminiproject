package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.enums.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for students table (joined with users).
 * Demonstrates JDBC transactions for multi-table inserts.
 */
public class StudentRepository {

    /**
     * Insert student + user in a SINGLE TRANSACTION.
     * If either fails, both are rolled back.
     */
    public int insert(Student student) throws SQLException {
        String userSql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, active) "
                       + "VALUES (?, ?, ?, ?, ?, 'STUDENT', ?)";
        String studentSql = "INSERT INTO students (user_id, register_number, department, year, "
                          + "guardian_name, guardian_phone) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);  // START TRANSACTION
            try {
                // Insert into users
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, student.getUsername());
                    ps.setString(2, student.getPasswordHash());
                    ps.setString(3, student.getFullName());
                    ps.setString(4, student.getEmail());
                    ps.setString(5, student.getPhone());
                    ps.setBoolean(6, student.isActive());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No user ID generated");
                        userId = keys.getInt(1);
                    }
                }

                // Insert into students
                int studentRecordId;
                try (PreparedStatement ps = conn.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setString(2, student.getRegisterNumber());
                    ps.setString(3, student.getDepartment());
                    ps.setInt(4, student.getYear());
                    ps.setString(5, student.getGuardianName());
                    ps.setString(6, student.getGuardianPhone());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No student record ID generated");
                        studentRecordId = keys.getInt(1);
                    }
                }

                conn.commit();  // COMMIT TRANSACTION
                student.setId(userId);
                student.setStudentRecordId(studentRecordId);
                return studentRecordId;

            } catch (SQLException e) {
                conn.rollback();  // ROLLBACK on failure
                throw e;
            }
        }
    }

    /** Find student by register number. */
    public Optional<Student> findByRegisterNumber(String regNo) throws SQLException {
        String sql = "SELECT u.*, s.id AS student_record_id, s.register_number, s.department, "
                   + "s.year, s.guardian_name, s.guardian_phone "
                   + "FROM students s JOIN users u ON s.user_id = u.id "
                   + "WHERE s.register_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Find student by user ID. */
    public Optional<Student> findByUserId(int userId) throws SQLException {
        String sql = "SELECT u.*, s.id AS student_record_id, s.register_number, s.department, "
                   + "s.year, s.guardian_name, s.guardian_phone "
                   + "FROM students s JOIN users u ON s.user_id = u.id WHERE u.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Find student by student record ID. */
    public Optional<Student> findById(int studentRecordId) throws SQLException {
        String sql = "SELECT u.*, s.id AS student_record_id, s.register_number, s.department, "
                   + "s.year, s.guardian_name, s.guardian_phone "
                   + "FROM students s JOIN users u ON s.user_id = u.id WHERE s.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Search students by name, register number, or department. */
    public List<Student> search(String query) throws SQLException {
        String sql = "SELECT u.*, s.id AS student_record_id, s.register_number, s.department, "
                   + "s.year, s.guardian_name, s.guardian_phone "
                   + "FROM students s JOIN users u ON s.user_id = u.id "
                   + "WHERE u.full_name LIKE ? OR s.register_number LIKE ? OR s.department LIKE ? "
                   + "ORDER BY u.full_name";
        List<Student> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    /** List all students. */
    public List<Student> findAll() throws SQLException {
        String sql = "SELECT u.*, s.id AS student_record_id, s.register_number, s.department, "
                   + "s.year, s.guardian_name, s.guardian_phone "
                   + "FROM students s JOIN users u ON s.user_id = u.id ORDER BY u.full_name";
        List<Student> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    /** Update student-specific fields. */
    public void update(Student student) throws SQLException {
        String userSql = "UPDATE users SET full_name=?, email=?, phone=?, active=?, updated_at=NOW() WHERE id=?";
        String studentSql = "UPDATE students SET department=?, year=?, guardian_name=?, guardian_phone=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                    ps.setString(1, student.getFullName());
                    ps.setString(2, student.getEmail());
                    ps.setString(3, student.getPhone());
                    ps.setBoolean(4, student.isActive());
                    ps.setInt(5, student.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
                    ps.setString(1, student.getDepartment());
                    ps.setInt(2, student.getYear());
                    ps.setString(3, student.getGuardianName());
                    ps.setString(4, student.getGuardianPhone());
                    ps.setInt(5, student.getStudentRecordId());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void setActiveStatus(int studentRecordId, boolean active) throws SQLException {
        String sql = "UPDATE users u JOIN students s ON u.id = s.user_id SET u.active = ?, u.updated_at = NOW() WHERE s.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, studentRecordId);
            ps.executeUpdate();
        }
    }

    /** Count active students. */
    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students s JOIN users u ON s.user_id = u.id WHERE u.active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setUsername(rs.getString("username"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setRole(UserRole.STUDENT);
        s.setActive(rs.getBoolean("active"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        s.setStudentRecordId(rs.getInt("student_record_id"));
        s.setRegisterNumber(rs.getString("register_number"));
        s.setDepartment(rs.getString("department"));
        s.setYear(rs.getInt("year"));
        s.setGuardianName(rs.getString("guardian_name"));
        s.setGuardianPhone(rs.getString("guardian_phone"));
        return s;
    }
}
