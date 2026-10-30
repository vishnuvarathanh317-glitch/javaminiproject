package com.hostelmanagement.repository;

import com.hostelmanagement.database.DatabaseConnection;
import com.hostelmanagement.model.*;
import com.hostelmanagement.enums.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the hostel physical hierarchy: hostels, blocks, floors, rooms, beds.
 */
public class HostelRepository {

    // ═══════════════ HOSTELS ═══════════════════════════════════

    public int insertHostel(Hostel hostel) throws SQLException {
        String sql = "INSERT INTO hostels (name, address) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hostel.getName());
            ps.setString(2, hostel.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { hostel.setId(keys.getInt(1)); return hostel.getId(); }
            }
        }
        throw new SQLException("Failed to insert hostel");
    }

    public List<Hostel> findAllHostels() throws SQLException {
        List<Hostel> list = new ArrayList<>();
        String sql = "SELECT * FROM hostels ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Hostel h = new Hostel();
                h.setId(rs.getInt("id"));
                h.setName(rs.getString("name"));
                h.setAddress(rs.getString("address"));
                h.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(h);
            }
        }
        return list;
    }

    // ═══════════════ BLOCKS ═══════════════════════════════════

    public int insertBlock(Block block) throws SQLException {
        String sql = "INSERT INTO blocks (hostel_id, name) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, block.getHostelId());
            ps.setString(2, block.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { block.setId(keys.getInt(1)); return block.getId(); }
            }
        }
        throw new SQLException("Failed to insert block");
    }

    public List<Block> findBlocksByHostel(int hostelId) throws SQLException {
        List<Block> list = new ArrayList<>();
        String sql = "SELECT b.*, h.name AS hostel_name FROM blocks b "
                   + "JOIN hostels h ON b.hostel_id = h.id WHERE b.hostel_id = ? ORDER BY b.name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hostelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Block b = new Block();
                    b.setId(rs.getInt("id"));
                    b.setHostelId(rs.getInt("hostel_id"));
                    b.setName(rs.getString("name"));
                    b.setHostelName(rs.getString("hostel_name"));
                    list.add(b);
                }
            }
        }
        return list;
    }

    public List<Block> findAllBlocks() throws SQLException {
        List<Block> list = new ArrayList<>();
        String sql = "SELECT b.*, h.name AS hostel_name FROM blocks b "
                   + "JOIN hostels h ON b.hostel_id = h.id ORDER BY h.name, b.name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Block b = new Block();
                b.setId(rs.getInt("id"));
                b.setHostelId(rs.getInt("hostel_id"));
                b.setName(rs.getString("name"));
                b.setHostelName(rs.getString("hostel_name"));
                list.add(b);
            }
        }
        return list;
    }

    // ═══════════════ FLOORS ═══════════════════════════════════

    public int insertFloor(Floor floor) throws SQLException {
        String sql = "INSERT INTO floors (block_id, floor_number) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, floor.getBlockId());
            ps.setInt(2, floor.getFloorNumber());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { floor.setId(keys.getInt(1)); return floor.getId(); }
            }
        }
        throw new SQLException("Failed to insert floor");
    }

    public List<Floor> findFloorsByBlock(int blockId) throws SQLException {
        List<Floor> list = new ArrayList<>();
        String sql = "SELECT f.*, b.name AS block_name FROM floors f "
                   + "JOIN blocks b ON f.block_id = b.id WHERE f.block_id = ? ORDER BY f.floor_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blockId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Floor f = new Floor();
                    f.setId(rs.getInt("id"));
                    f.setBlockId(rs.getInt("block_id"));
                    f.setFloorNumber(rs.getInt("floor_number"));
                    f.setBlockName(rs.getString("block_name"));
                    list.add(f);
                }
            }
        }
        return list;
    }

    // ═══════════════ ROOMS ═══════════════════════════════════

    public int insertRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (floor_id, room_number, room_type, capacity, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, room.getFloorId());
            ps.setString(2, room.getRoomNumber());
            ps.setString(3, room.getRoomType().name());
            ps.setInt(4, room.getCapacity());
            ps.setBoolean(5, room.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { room.setId(keys.getInt(1)); return room.getId(); }
            }
        }
        throw new SQLException("Failed to insert room");
    }

    public List<Room> findRoomsByFloor(int floorId) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, f.floor_number, b.name AS block_name "
                   + "FROM rooms r JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id WHERE r.floor_id = ? ORDER BY r.room_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, floorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRoom(rs));
            }
        }
        return list;
    }

    public List<Room> findAllRooms() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, f.floor_number, b.name AS block_name "
                   + "FROM rooms r JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id ORDER BY b.name, f.floor_number, r.room_number";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRoom(rs));
        }
        return list;
    }

    private Room mapRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt("id"));
        r.setFloorId(rs.getInt("floor_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        r.setCapacity(rs.getInt("capacity"));
        r.setActive(rs.getBoolean("active"));
        r.setFloorNumber(rs.getInt("floor_number"));
        r.setBlockName(rs.getString("block_name"));
        return r;
    }

    // ═══════════════ BEDS ═══════════════════════════════════

    public int insertBed(Bed bed) throws SQLException {
        String sql = "INSERT INTO beds (room_id, bed_number, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bed.getRoomId());
            ps.setString(2, bed.getBedNumber());
            ps.setString(3, bed.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { bed.setId(keys.getInt(1)); return bed.getId(); }
            }
        }
        throw new SQLException("Failed to insert bed");
    }

    public void updateBedStatus(int bedId, BedStatus status) throws SQLException {
        String sql = "UPDATE beds SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bedId);
            ps.executeUpdate();
        }
    }

    /** Find all AVAILABLE beds with room/block info — used by Room Recommendation Engine. */
    public List<Bed> findAvailableBeds() throws SQLException {
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT bd.*, r.room_number, r.room_type, f.floor_number, b.name AS block_name "
                   + "FROM beds bd JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "WHERE bd.status = 'AVAILABLE' AND r.active = TRUE "
                   + "ORDER BY b.name, f.floor_number, r.room_number, bd.bed_number";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapBed(rs));
        }
        return list;
    }

    public List<Bed> findBedsByRoom(int roomId) throws SQLException {
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT bd.*, r.room_number, r.room_type, f.floor_number, b.name AS block_name "
                   + "FROM beds bd JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "WHERE bd.room_id = ? ORDER BY bd.bed_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBed(rs));
            }
        }
        return list;
    }

    public List<Bed> findAllBeds() throws SQLException {
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT bd.*, r.room_number, r.room_type, f.floor_number, b.name AS block_name "
                   + "FROM beds bd JOIN rooms r ON bd.room_id = r.id "
                   + "JOIN floors f ON r.floor_id = f.id "
                   + "JOIN blocks b ON f.block_id = b.id "
                   + "ORDER BY b.name, f.floor_number, r.room_number, bd.bed_number";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapBed(rs));
        }
        return list;
    }

    private Bed mapBed(ResultSet rs) throws SQLException {
        Bed bed = new Bed();
        bed.setId(rs.getInt("id"));
        bed.setRoomId(rs.getInt("room_id"));
        bed.setBedNumber(rs.getString("bed_number"));
        bed.setStatus(BedStatus.valueOf(rs.getString("status")));
        bed.setRoomNumber(rs.getString("room_number"));
        bed.setBlockName(rs.getString("block_name"));
        bed.setFloorNumber(rs.getInt("floor_number"));
        return bed;
    }

    // ═══════════════ OCCUPANCY STATISTICS ═══════════════════

    public int countTotalBeds() throws SQLException {
        String sql = "SELECT COUNT(*) FROM beds bd JOIN rooms r ON bd.room_id = r.id WHERE r.active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countOccupiedBeds() throws SQLException {
        String sql = "SELECT COUNT(*) FROM beds WHERE status = 'OCCUPIED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countAvailableBeds() throws SQLException {
        String sql = "SELECT COUNT(*) FROM beds bd JOIN rooms r ON bd.room_id = r.id "
                   + "WHERE bd.status = 'AVAILABLE' AND r.active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countTotalRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
