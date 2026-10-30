package com.hostelmanagement.model;

import com.hostelmanagement.enums.RoomType;

/** A room on a floor, with a type and capacity. */
public class Room {
    private int id;
    private int floorId;
    private String roomNumber;
    private RoomType roomType;
    private int capacity;
    private boolean active;
    // Transient display fields
    private String blockName;
    private int floorNumber;

    public Room() { this.active = true; }

    public Room(int floorId, String roomNumber, RoomType roomType, int capacity) {
        this.floorId = floorId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.active = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFloorId() { return floorId; }
    public void setFloorId(int floorId) { this.floorId = floorId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    @Override
    public String toString() { return roomNumber + " (" + roomType + ")"; }
}
