package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.enums.AttendanceStatus;
import com.hostelmanagement.model.AttendanceRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DAO for attendance table. Enforces one record per student per date via UK. */
public class AttendanceRepository {

    public void insert(AttendanceRecord rec) throws SQLException {
        String sql = "INSERT INTO attendance (student_record_id, attendance_date, status, recorded_by_user_id) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rec.getStudentRecordId());
            ps.setDate(2, Date.valueOf(rec.getDate()));
            ps.setString(3, rec.getStatus().name());
            ps.setInt(4, rec.getRecordedByUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new SQLException("Attendance already recorded for this student on " + rec.getDate());
            }
            throw e;
        }
    }

    public boolean exists(int studentRecordId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_record_id = ? AND attendance_date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentRecordId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Batch insert attendance for multiple students. */
    public void insertBatch(List<AttendanceRecord> records) throws SQLException {
        String sql = "INSERT INTO attendance (student_record_id, attendance_date, status, recorded_by_user_id) "
                   + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (AttendanceRecord rec : records) {
                ps.setInt(1, rec.getStudentRecordId());
                ps.setDate(2, Date.valueOf(rec.getDate()));
                ps.setString(3, rec.getStatus().name());
                ps.setInt(4, rec.getRecordedByUserId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<AttendanceRecord> findByStudentAndMonth(int studentRecordId, int year, int month) throws SQLException {
        String sql = "SELECT a.*, u.full_name AS student_name, s.register_number "
                   + "FROM attendance a JOIN students s ON a.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "WHERE a.student_record_id = ? AND YEAR(a.attendance_date) = ? AND MONTH(a.attendance_date) = ? "
                   + "ORDER BY a.attendance_date";
        return executeQuery(sql, ps -> {
            ps.setInt(1, studentRecordId);
            ps.setInt(2, year);
            ps.setInt(3, month);
        });
    }

    public List<AttendanceRecord> findByDate(LocalDate date) throws SQLException {
        String sql = "SELECT a.*, u.full_name AS student_name, s.register_number "
                   + "FROM attendance a JOIN students s ON a.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "WHERE a.attendance_date = ? ORDER BY u.full_name";
        return executeQuery(sql, ps -> ps.setDate(1, Date.valueOf(date)));
    }

    /** Count today's attendance by status. */
    public int countTodayByStatus(AttendanceStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE attendance_date = CURDATE() AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    @FunctionalInterface
    interface ParamSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<AttendanceRecord> executeQuery(String sql, ParamSetter setter) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord r = new AttendanceRecord();
                    r.setId(rs.getInt("id"));
                    r.setStudentRecordId(rs.getInt("student_record_id"));
                    r.setDate(rs.getDate("attendance_date").toLocalDate());
                    r.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
                    r.setRecordedByUserId(rs.getInt("recorded_by_user_id"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    r.setStudentName(rs.getString("student_name"));
                    r.setRegisterNumber(rs.getString("register_number"));
                    list.add(r);
                }
            }
        }
        return list;
    }
}
