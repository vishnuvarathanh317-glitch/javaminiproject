package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.model.WaitingListEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for waiting_list table. Entries ordered by created_at for FIFO processing. */
public class WaitingListRepository {

    public int insert(WaitingListEntry entry) throws SQLException {
        String sql = "INSERT INTO waiting_list (student_record_id, preferred_block, "
                   + "preferred_room_type, preferred_floor) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entry.getStudentRecordId());
            ps.setString(2, entry.getPreferredBlockName());
            ps.setString(3, entry.getPreferredRoomType());
            ps.setInt(4, entry.getPreferredFloor());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { entry.setId(keys.getInt(1)); return entry.getId(); }
            }
        }
        throw new SQLException("Failed to insert waiting list entry");
    }

    /** Get unfulfilled entries in FIFO order. */
    public List<WaitingListEntry> findUnfulfilled() throws SQLException {
        String sql = "SELECT w.*, u.full_name AS student_name, s.register_number "
                   + "FROM waiting_list w JOIN students s ON w.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "WHERE w.fulfilled = FALSE ORDER BY w.created_at ASC";
        List<WaitingListEntry> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                WaitingListEntry e = new WaitingListEntry();
                e.setId(rs.getInt("id"));
                e.setStudentRecordId(rs.getInt("student_record_id"));
                e.setPreferredBlockName(rs.getString("preferred_block"));
                e.setPreferredRoomType(rs.getString("preferred_room_type"));
                e.setPreferredFloor(rs.getInt("preferred_floor"));
                e.setFulfilled(rs.getBoolean("fulfilled"));
                e.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                e.setStudentName(rs.getString("student_name"));
                e.setRegisterNumber(rs.getString("register_number"));
                list.add(e);
            }
        }
        return list;
    }

    public void markFulfilled(int id) throws SQLException {
        String sql = "UPDATE waiting_list SET fulfilled = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void fulfillForStudent(int studentRecordId) throws SQLException {
        String sql = "UPDATE waiting_list SET fulfilled = TRUE WHERE student_record_id = ? AND fulfilled = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentRecordId);
            ps.executeUpdate();
        }
    }
}
