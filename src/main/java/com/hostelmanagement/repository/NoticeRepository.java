package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.model.Notice;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DAO for notices table. */
public class NoticeRepository {

    public int insert(Notice n) throws SQLException {
        String sql = "INSERT INTO notices (title, body, audience, publish_date, expiry_date, posted_by_user_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getBody());
            ps.setString(3, n.getAudience());
            ps.setDate(4, Date.valueOf(n.getPublishDate()));
            ps.setDate(5, n.getExpiryDate() != null ? Date.valueOf(n.getExpiryDate()) : null);
            ps.setInt(6, n.getPostedByUserId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { n.setId(keys.getInt(1)); return n.getId(); }
            }
        }
        throw new SQLException("Failed to insert notice");
    }

    /** Find notices visible to a given audience, not expired. */
    public List<Notice> findActiveForAudience(String audience) throws SQLException {
        String sql = "SELECT n.*, u.full_name AS posted_by_name FROM notices n "
                   + "JOIN users u ON n.posted_by_user_id = u.id "
                   + "WHERE (n.audience = 'ALL' OR n.audience = ?) "
                   + "AND n.publish_date <= ? "
                   + "AND (n.expiry_date IS NULL OR n.expiry_date >= ?) "
                   + "ORDER BY n.publish_date DESC";
        List<Notice> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, audience);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Notice> findAll() throws SQLException {
        String sql = "SELECT n.*, u.full_name AS posted_by_name FROM notices n "
                   + "JOIN users u ON n.posted_by_user_id = u.id ORDER BY n.publish_date DESC";
        List<Notice> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Notice mapRow(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setId(rs.getInt("id"));
        n.setTitle(rs.getString("title"));
        n.setBody(rs.getString("body"));
        n.setAudience(rs.getString("audience"));
        n.setPublishDate(rs.getDate("publish_date").toLocalDate());
        Date exp = rs.getDate("expiry_date");
        if (exp != null) n.setExpiryDate(exp.toLocalDate());
        n.setPostedByUserId(rs.getInt("posted_by_user_id"));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        n.setPostedByName(rs.getString("posted_by_name"));
        return n;
    }
}
