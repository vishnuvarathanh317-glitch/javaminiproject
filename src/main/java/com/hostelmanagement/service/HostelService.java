package com.hostelmanagement.service;

import com.hostelmanagement.algorithms.OccupancyCalculator;
import com.hostelmanagement.enums.BedStatus;
import com.hostelmanagement.enums.RoomType;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.*;
import com.hostelmanagement.repository.HostelRepository;
import com.hostelmanagement.util.InputValidator;

import java.sql.SQLException;
import java.util.List;

/**
 * Hostel Hierarchy Service.
 * Manages Hostels, Blocks, Floors, Rooms, Beds, and calculates live occupancy.
 */
public class HostelService {

    private final HostelRepository hostelRepository;

    public HostelService() {
        this.hostelRepository = new HostelRepository();
    }

    public HostelService(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    // ── HOSTEL ──────────────────────────────────────────────────
    public Hostel createHostel(String name, String address) {
        InputValidator.requireNonEmpty(name, "Hostel name");
        Hostel hostel = new Hostel(name.trim(), address != null ? address.trim() : "");
        try {
            int id = hostelRepository.insertHostel(hostel);
            hostel.setId(id);
            return hostel;
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating hostel: " + e.getMessage(), e);
        }
    }

    public List<Hostel> getAllHostels() {
        try {
            return hostelRepository.findAllHostels();
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving hostels: " + e.getMessage(), e);
        }
    }

    // ── BLOCK ───────────────────────────────────────────────────
    public Block createBlock(int hostelId, String name) {
        InputValidator.requireNonEmpty(name, "Block name");
        Block block = new Block(hostelId, name.trim());
        try {
            int id = hostelRepository.insertBlock(block);
            block.setId(id);
            return block;
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating block: " + e.getMessage(), e);
        }
    }

    public List<Block> getBlocksByHostel(int hostelId) {
        try {
            return hostelRepository.findBlocksByHostel(hostelId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving blocks: " + e.getMessage(), e);
        }
    }

    public List<Block> getAllBlocks() {
        try {
            return hostelRepository.findAllBlocks();
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving all blocks: " + e.getMessage(), e);
        }
    }

    // ── FLOOR ───────────────────────────────────────────────────
    public Floor createFloor(int blockId, int floorNumber) {
        Floor floor = new Floor(blockId, floorNumber);
        try {
            int id = hostelRepository.insertFloor(floor);
            floor.setId(id);
            return floor;
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating floor: " + e.getMessage(), e);
        }
    }

    public List<Floor> getFloorsByBlock(int blockId) {
        try {
            return hostelRepository.findFloorsByBlock(blockId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving floors: " + e.getMessage(), e);
        }
    }

    // ── ROOM & BEDS ─────────────────────────────────────────────
    public Room createRoom(int floorId, String roomNumber, RoomType roomType, int capacity) {
        InputValidator.requireNonEmpty(roomNumber, "Room number");
        if (roomType == null) throw new ValidationException("Room type is required.");
        if (capacity < 1 || capacity > 10) {
            throw new ValidationException("Room capacity must be between 1 and 10.");
        }

        Room room = new Room(floorId, roomNumber.trim(), roomType, capacity);
        try {
            int roomId = hostelRepository.insertRoom(room);
            room.setId(roomId);

            // Automatically provision atomic beds for the room's capacity
            for (int i = 1; i <= capacity; i++) {
                String bedNum = roomNumber.trim() + "-" + (char) ('A' + (i - 1));
                Bed bed = new Bed(roomId, bedNum);
                bed.setStatus(BedStatus.AVAILABLE);
                hostelRepository.insertBed(bed);
            }

            return room;
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating room and beds: " + e.getMessage(), e);
        }
    }

    public List<Room> getRoomsByFloor(int floorId) {
        try {
            return hostelRepository.findRoomsByFloor(floorId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving rooms: " + e.getMessage(), e);
        }
    }

    public List<Room> getAllRooms() {
        try {
            return hostelRepository.findAllRooms();
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving all rooms: " + e.getMessage(), e);
        }
    }

    public List<Bed> getBedsByRoom(int roomId) {
        try {
            return hostelRepository.findBedsByRoom(roomId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving beds by room: " + e.getMessage(), e);
        }
    }

    public List<Bed> getAllBeds() {
        try {
            return hostelRepository.findAllBeds();
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving all beds: " + e.getMessage(), e);
        }
    }

    public List<Bed> getAvailableBeds() {
        try {
            return hostelRepository.findAvailableBeds();
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving available beds: " + e.getMessage(), e);
        }
    }

    public void setBedStatus(int bedId, BedStatus status) {
        if (status == null) throw new ValidationException("Bed status cannot be null.");
        try {
            hostelRepository.updateBedStatus(bedId, status);
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating bed status: " + e.getMessage(), e);
        }
    }

    public OccupancyCalculator.OccupancyStats getOccupancyStats() {
        List<Bed> allBeds = getAllBeds();
        return OccupancyCalculator.calculate(allBeds);
    }
}
