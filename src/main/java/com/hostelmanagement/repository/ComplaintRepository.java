package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.enums.*;
import com.hostelmanagement.model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for complaints and complaint_history tables. */
public class ComplaintRepository {

    public int insert(Complaint c) throws SQLException {
        String sql = "INSERT INTO complaints (student_record_id, category, description, location, "
                   + "priority, recommended_priority, priority_score, priority_explanation, "
                   + "status, urgency, impact_count) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getStudentRecordId());
            ps.setString(2, c.getCategory().name());
            ps.setString(3, c.getDescription());
            ps.setString(4, c.getLocation());
            ps.setString(5, c.getPriority() != null ? c.getPriority().name() : "MEDIUM");
            ps.setString(6, c.getRecommendedPriority() != null ? c.getRecommendedPriority().name() : null);
            ps.setDouble(7, c.getPriorityScore());
            ps.setString(8, c.getPriorityExplanation());
            ps.setString(9, c.getStatus().name());
            ps.setInt(10, c.getUrgency());
            ps.setInt(11, c.getImpactCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { c.setId(keys.getInt(1)); return c.getId(); }
            }
        }
        throw new SQLException("Failed to insert complaint");
    }

    /** Update status with transition validation + history record. */
    public void updateStatus(int complaintId, ComplaintStatus newStatus,
                              int changedByUserId, String notes) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get current status
                ComplaintStatus current;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT status FROM complaints WHERE id = ?")) {
                    ps.setInt(1, complaintId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Complaint not found");
                        current = ComplaintStatus.valueOf(rs.getString("status"));
                    }
                }

                if (!current.canTransitionTo(newStatus)) {
                    throw new SQLException("Invalid transition: " + current + " → " + newStatus);
                }

                // Update complaint
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE complaints SET status=?, assigned_to_user_id=COALESCE(assigned_to_user_id, ?), "
                        + "resolution_notes=COALESCE(?, resolution_notes), updated_at=NOW() WHERE id=?")) {
                    ps.setString(1, newStatus.name());
                    ps.setInt(2, changedByUserId);
                    ps.setString(3, notes);
                    ps.setInt(4, complaintId);
                    ps.executeUpdate();
                }

                // Insert history record
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO complaint_history (complaint_id, old_status, new_status, "
                        + "changed_by_user_id, notes) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, complaintId);
                    ps.setString(2, current.name());
                    ps.setString(3, newStatus.name());
                    ps.setInt(4, changedByUserId);
                    ps.setString(5, notes);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<Complaint> findById(int id) throws SQLException {
        List<Complaint> list = query("c.id = ?", ps -> ps.setInt(1, id));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Complaint> findAll() throws SQLException {
        return query("1=1", null);
    }

    public List<Complaint> findByStudent(int studentRecordId) throws SQLException {
        return query("c.student_record_id = ?", ps -> ps.setInt(1, studentRecordId));
    }

    public List<Complaint> findByStatus(ComplaintStatus status) throws SQLException {
        return query("c.status = ?", ps -> ps.setString(1, status.name()));
    }

    public int countByStatus(ComplaintStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public int countPending() throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status IN ('OPEN','ASSIGNED','IN_PROGRESS')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @FunctionalInterface
    interface ParamSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<Complaint> query(String where, ParamSetter setter) throws SQLException {
        String sql = "SELECT c.*, u.full_name AS student_name, s.register_number, "
                   + "u2.full_name AS assigned_to_name "
                   + "FROM complaints c "
                   + "JOIN students s ON c.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "LEFT JOIN users u2 ON c.assigned_to_user_id = u2.id "
                   + "WHERE " + where + " ORDER BY c.created_at DESC";
        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (setter != null) setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setStudentRecordId(rs.getInt("student_record_id"));
        c.setCategory(ComplaintCategory.valueOf(rs.getString("category")));
        c.setDescription(rs.getString("description"));
        c.setLocation(rs.getString("location"));
        c.setPriority(ComplaintPriority.valueOf(rs.getString("priority")));
        String rp = rs.getString("recommended_priority");
        if (rp != null) c.setRecommendedPriority(ComplaintPriority.valueOf(rp));
        c.setPriorityScore(rs.getDouble("priority_score"));
        c.setPriorityExplanation(rs.getString("priority_explanation"));
        c.setStatus(ComplaintStatus.valueOf(rs.getString("status")));
        c.setAssignedToUserId(rs.getInt("assigned_to_user_id"));
        c.setResolutionNotes(rs.getString("resolution_notes"));
        c.setUrgency(rs.getInt("urgency"));
        c.setImpactCount(rs.getInt("impact_count"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        c.setStudentName(rs.getString("student_name"));
        c.setRegisterNumber(rs.getString("register_number"));
        c.setAssignedToName(rs.getString("assigned_to_name"));
        return c;
    }
}
