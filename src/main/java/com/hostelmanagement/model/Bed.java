package com.hostelmanagement.model;

import com.hostelmanagement.enums.BedStatus;

/** An individual bed within a room. This is the atomic allocatable unit. */
public class Bed {
    private int id;
    private int roomId;
    private String bedNumber;
    private BedStatus status;
    // Transient display fields
    private String roomNumber;
    private String blockName;
    private int floorNumber;

    public Bed() { this.status = BedStatus.AVAILABLE; }

    public Bed(int roomId, String bedNumber) {
        this.roomId = roomId;
        this.bedNumber = bedNumber;
        this.status = BedStatus.AVAILABLE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public BedStatus getStatus() { return status; }
    public void setStatus(BedStatus status) { this.status = status; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    @Override
    public String toString() { return bedNumber + " in " + roomNumber; }
}
