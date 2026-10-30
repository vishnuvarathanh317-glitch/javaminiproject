package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.enums.FeeStatus;
import com.hostelmanagement.model.FeeRecord;
import com.hostelmanagement.model.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for fee_records and payments tables. Uses transactions for payment recording. */
public class FeeRepository {

    public int insertFeeRecord(FeeRecord fr) throws SQLException {
        String sql = "INSERT INTO fee_records (student_record_id, description, total_amount, due_date) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fr.getStudentRecordId());
            ps.setString(2, fr.getDescription());
            ps.setBigDecimal(3, fr.getTotalAmount());
            ps.setDate(4, fr.getDueDate() != null ? Date.valueOf(fr.getDueDate()) : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { fr.setId(keys.getInt(1)); return fr.getId(); }
            }
        }
        throw new SQLException("Failed to insert fee record");
    }

    /** Record payment and update fee status in a transaction. */
    public void recordPayment(Payment payment) throws SQLException {
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Payment amount must be positive");
        }
        String insertPaySql = "INSERT INTO payments (fee_record_id, amount, payment_date, "
                             + "payment_method, reference_number, recorded_by_user_id) VALUES (?,?,?,?,?,?)";
        String updateFeeSql = "UPDATE fee_records SET paid_amount = paid_amount + ?, "
                            + "status = CASE "
                            + "  WHEN paid_amount + ? >= total_amount THEN 'PAID' "
                            + "  ELSE 'PARTIALLY_PAID' END, "
                            + "updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(insertPaySql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, payment.getFeeRecordId());
                    ps.setBigDecimal(2, payment.getAmount());
                    ps.setDate(3, Date.valueOf(payment.getPaymentDate()));
                    ps.setString(4, payment.getPaymentMethod());
                    ps.setString(5, payment.getReferenceNumber());
                    ps.setInt(6, payment.getRecordedByUserId());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) payment.setId(keys.getInt(1));
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(updateFeeSql)) {
                    ps.setBigDecimal(1, payment.getAmount());
                    ps.setBigDecimal(2, payment.getAmount());
                    ps.setInt(3, payment.getFeeRecordId());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<FeeRecord> findByStudent(int studentRecordId) throws SQLException {
        String sql = "SELECT fr.*, u.full_name AS student_name, s.register_number "
                   + "FROM fee_records fr JOIN students s ON fr.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id WHERE fr.student_record_id = ? ORDER BY fr.created_at DESC";
        List<FeeRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentRecordId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapFeeRow(rs)); }
        }
        return list;
    }

    public Optional<FeeRecord> findById(int id) throws SQLException {
        String sql = "SELECT fr.*, u.full_name AS student_name, s.register_number "
                   + "FROM fee_records fr JOIN students s ON fr.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id WHERE fr.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapFeeRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<FeeRecord> findAll() throws SQLException {
        String sql = "SELECT fr.*, u.full_name AS student_name, s.register_number "
                   + "FROM fee_records fr JOIN students s ON fr.student_record_id = s.id "
                   + "JOIN users u ON s.user_id = u.id ORDER BY fr.created_at DESC";
        List<FeeRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapFeeRow(rs));
        }
        return list;
    }

    public List<Payment> findPaymentsByFee(int feeRecordId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE fee_record_id = ? ORDER BY payment_date DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, feeRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("id"));
                    p.setFeeRecordId(rs.getInt("fee_record_id"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setReferenceNumber(rs.getString("reference_number"));
                    p.setRecordedByUserId(rs.getInt("recorded_by_user_id"));
                    p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(p);
                }
            }
        }
        return list;
    }

    /** Total fees and total paid across all records. */
    public BigDecimal[] getTotalFeeSummary() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount),0), COALESCE(SUM(paid_amount),0) FROM fee_records";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new BigDecimal[]{ rs.getBigDecimal(1), rs.getBigDecimal(2) };
            }
        }
        return new BigDecimal[]{ BigDecimal.ZERO, BigDecimal.ZERO };
    }

    private FeeRecord mapFeeRow(ResultSet rs) throws SQLException {
        FeeRecord fr = new FeeRecord();
        fr.setId(rs.getInt("id"));
        fr.setStudentRecordId(rs.getInt("student_record_id"));
        fr.setDescription(rs.getString("description"));
        fr.setTotalAmount(rs.getBigDecimal("total_amount"));
        fr.setPaidAmount(rs.getBigDecimal("paid_amount"));
        fr.setStatus(FeeStatus.valueOf(rs.getString("status")));
        Date due = rs.getDate("due_date");
        if (due != null) fr.setDueDate(due.toLocalDate());
        fr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        fr.setStudentName(rs.getString("student_name"));
        fr.setRegisterNumber(rs.getString("register_number"));
        return fr;
    }
}
