package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.enums.LeaveStatus;
import com.hostelmanagement.model.LeaveRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for leave_requests table. */
public class LeaveRepository {

    public int insert(LeaveRequest lr) throws SQLException {
        String sql = "INSERT INTO leave_requests (student_record_id, departure_date, return_date, reason) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lr.getStudentRecordId());
            ps.setDate(2, Date.valueOf(lr.getDepartureDate()));
            ps.setDate(3, Date.valueOf(lr.getReturnDate()));
            ps.setString(4, lr.getReason());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { lr.setId(keys.getInt(1)); return lr.getId(); }
            }
        }
        throw new SQLException("Failed to insert leave request");
    }

    public void decide(int id, LeaveStatus status, int decidedByUserId, String remarks) throws SQLException {
        String sql = "UPDATE leave_requests SET status=?, decided_by_user_id=?, decision_remarks=?, "
                   + "updated_at=NOW() WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, decidedByUserId);
            ps.setString(3, remarks);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public Optional<LeaveRequest> findById(int id) throws SQLException {
        List<LeaveRequest> list = query("lr.id = ?", ps -> ps.setInt(1, id));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<LeaveRequest> findAll() throws SQLException {
        return query("1=1", null);
    }

    public List<LeaveRequest> findByStudent(int studentRecordId) throws SQLException {
        return query("lr.student_record_id = ?", ps -> ps.setInt(1, studentRecordId));
    }

    public List<LeaveRequest> findByStatus(LeaveStatus status) throws SQLException {
        return query("lr.status = ?", ps -> ps.setString(1, status.name()));
    }

    public int countPending() throws SQLException {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @FunctionalInterface
    interface ParamSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<LeaveRequest> query(String where, ParamSetter setter) throws SQLException {
        String sql = "SELECT lr.*, u.full_name AS student_name, s.register_number, "
                   + "u2.full_name AS decided_by_name "
                   + "FROM leave_requests lr "
                   + "JOIN students s ON lr.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "LEFT JOIN users u2 ON lr.decided_by_user_id = u2.id "
                   + "WHERE " + where + " ORDER BY lr.created_at DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (setter != null) setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest();
                    lr.setId(rs.getInt("id"));
                    lr.setStudentRecordId(rs.getInt("student_record_id"));
                    lr.setDepartureDate(rs.getDate("departure_date").toLocalDate());
                    lr.setReturnDate(rs.getDate("return_date").toLocalDate());
                    lr.setReason(rs.getString("reason"));
                    lr.setStatus(LeaveStatus.valueOf(rs.getString("status")));
                    lr.setDecidedByUserId(rs.getInt("decided_by_user_id"));
                    lr.setDecisionRemarks(rs.getString("decision_remarks"));
                    lr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    Timestamp upd = rs.getTimestamp("updated_at");
                    if (upd != null) lr.setUpdatedAt(upd.toLocalDateTime());
                    lr.setStudentName(rs.getString("student_name"));
                    lr.setRegisterNumber(rs.getString("register_number"));
                    lr.setDecidedByName(rs.getString("decided_by_name"));
                    list.add(lr);
                }
            }
        }
        return list;
    }
}
