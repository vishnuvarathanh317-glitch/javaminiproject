package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.enums.AllocationStatus;
import com.hostelmanagement.model.Allocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for allocations table.
 * Uses transactions for allocation + bed-status update atomicity.
 */
public class AllocationRepository {

    /**
     * Create allocation AND update bed status in a SINGLE TRANSACTION.
     * Uses SELECT FOR UPDATE to prevent simultaneous allocation of the same bed.
     */
    public int allocate(Allocation alloc) throws SQLException {
        String checkBedSql = "SELECT status FROM beds WHERE id = ? FOR UPDATE";
        String insertSql = "INSERT INTO allocations (student_record_id, bed_id, start_date, status, "
                         + "allocated_by_user_id) VALUES (?, ?, ?, 'ACTIVE', ?)";
        String updateBedSql = "UPDATE beds SET status = 'OCCUPIED' WHERE id = ?";
        String checkStudentSql = "SELECT COUNT(*) FROM allocations WHERE student_record_id = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check student doesn't already have active allocation
                try (PreparedStatement ps = conn.prepareStatement(checkStudentSql)) {
                    ps.setInt(1, alloc.getStudentRecordId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            throw new SQLException("Student already has an active bed allocation. "
                                + "Transfer or vacate the existing allocation first.");
                        }
                    }
                }

                // Lock and check bed availability
                try (PreparedStatement ps = conn.prepareStatement(checkBedSql)) {
                    ps.setInt(1, alloc.getBedId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"AVAILABLE".equals(rs.getString("status"))) {
                            conn.rollback();
                            throw new SQLException("Bed is not available. It may have been allocated by another user.");
                        }
                    }
                }

                // Insert allocation
                int allocId;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, alloc.getStudentRecordId());
                    ps.setInt(2, alloc.getBedId());
                    ps.setDate(3, Date.valueOf(alloc.getStartDate()));
                    ps.setInt(4, alloc.getAllocatedByUserId());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No allocation ID generated");
                        allocId = keys.getInt(1);
                    }
                }

                // Update bed status to OCCUPIED
                try (PreparedStatement ps = conn.prepareStatement(updateBedSql)) {
                    ps.setInt(1, alloc.getBedId());
                    ps.executeUpdate();
                }

                conn.commit();
                alloc.setId(allocId);
                return allocId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Vacate: set allocation to VACATED, bed back to AVAILABLE. */
    public void vacate(int allocationId) throws SQLException {
        String getAllocSql = "SELECT bed_id FROM allocations WHERE id = ? AND status = 'ACTIVE'";
        String updateAllocSql = "UPDATE allocations SET status = 'VACATED', end_date = CURDATE(), "
                              + "updated_at = NOW() WHERE id = ?";
        String updateBedSql = "UPDATE beds SET status = 'AVAILABLE' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bedId;
                try (PreparedStatement ps = conn.prepareStatement(getAllocSql)) {
                    ps.setInt(1, allocationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); throw new SQLException("No active allocation found"); }
                        bedId = rs.getInt("bed_id");
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(updateAllocSql)) {
                    ps.setInt(1, allocationId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateBedSql)) {
                    ps.setInt(1, bedId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Find active allocation for a student. */
    public Optional<Allocation> findActiveByStudent(int studentRecordId) throws SQLException {
        String sql = "SELECT a.*, s.register_number, u.full_name AS student_name, "
                   + "bd.bed_number, r.room_number, b.name AS block_name "
                   + "FROM allocations a "
                   + "JOIN students s ON a.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "JOIN beds bd ON a.bed_id = bd.id "
                   + "JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "WHERE a.student_record_id = ? AND a.status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Allocation> findById(int id) throws SQLException {
        String sql = "SELECT a.*, s.register_number, u.full_name AS student_name, "
                   + "bd.bed_number, r.room_number, b.name AS block_name "
                   + "FROM allocations a "
                   + "JOIN students s ON a.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "JOIN beds bd ON a.bed_id = bd.id "
                   + "JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "WHERE a.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Allocation> findByStudent(int studentRecordId) throws SQLException {
        String sql = "SELECT a.*, s.register_number, u.full_name AS student_name, "
                   + "bd.bed_number, r.room_number, b.name AS block_name "
                   + "FROM allocations a "
                   + "JOIN students s ON a.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "JOIN beds bd ON a.bed_id = bd.id "
                   + "JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "WHERE a.student_record_id = ? ORDER BY a.created_at DESC";
        List<Allocation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** List all allocations (with joins for display). */
    public List<Allocation> findAll() throws SQLException {
        String sql = "SELECT a.*, s.register_number, u.full_name AS student_name, "
                   + "bd.bed_number, r.room_number, b.name AS block_name "
                   + "FROM allocations a "
                   + "JOIN students s ON a.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "JOIN beds bd ON a.bed_id = bd.id "
                   + "JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "ORDER BY a.created_at DESC";
        List<Allocation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** List active allocations only. */
    public List<Allocation> findActive() throws SQLException {
        String sql = "SELECT a.*, s.register_number, u.full_name AS student_name, "
                   + "bd.bed_number, r.room_number, b.name AS block_name "
                   + "FROM allocations a "
                   + "JOIN students s ON a.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id "
                   + "JOIN beds bd ON a.bed_id = bd.id "
                   + "JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "WHERE a.status = 'ACTIVE' ORDER BY u.full_name";
        List<Allocation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Allocation mapRow(ResultSet rs) throws SQLException {
        Allocation a = new Allocation();
        a.setId(rs.getInt("id"));
        a.setStudentRecordId(rs.getInt("student_record_id"));
        a.setBedId(rs.getInt("bed_id"));
        a.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) a.setEndDate(endDate.toLocalDate());
        a.setStatus(AllocationStatus.valueOf(rs.getString("status")));
        a.setAllocatedByUserId(rs.getInt("allocated_by_user_id"));
        a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        a.setStudentName(rs.getString("student_name"));
        a.setRegisterNumber(rs.getString("register_number"));
        a.setBedNumber(rs.getString("bed_number"));
        a.setRoomNumber(rs.getString("room_number"));
        a.setBlockName(rs.getString("block_name"));
        return a;
    }
}
