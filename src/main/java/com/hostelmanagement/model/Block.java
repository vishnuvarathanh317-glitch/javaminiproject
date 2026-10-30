package com.hostelmanagement.model;

/** A block within a hostel (e.g., Block A, Block B). */
public class Block {
    private int id;
    private int hostelId;
    private String name;
    private String hostelName; // transient display field

    public Block() {}
    public Block(int hostelId, String name) {
        this.hostelId = hostelId;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHostelId() { return hostelId; }
    public void setHostelId(int hostelId) { this.hostelId = hostelId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }

    @Override
    public String toString() { return name; }
}
