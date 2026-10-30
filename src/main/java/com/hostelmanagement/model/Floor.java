package com.hostelmanagement.model;

/** A floor within a block. */
public class Floor {
    private int id;
    private int blockId;
    private int floorNumber;
    private String blockName; // transient display field

    public Floor() {}
    public Floor(int blockId, int floorNumber) {
        this.blockId = blockId;
        this.floorNumber = floorNumber;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }
    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    @Override
    public String toString() { return "Floor " + floorNumber; }
}
