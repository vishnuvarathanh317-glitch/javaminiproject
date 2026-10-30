package com.hostelmanagement.model;

import java.time.LocalDateTime;

/** Represents a hostel building. Top of the hierarchy: Hostel → Block → Floor → Room → Bed. */
public class Hostel {
    private int id;
    private String name;
    private String address;
    private LocalDateTime createdAt;

    public Hostel() {}
    public Hostel(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return name; }
}
