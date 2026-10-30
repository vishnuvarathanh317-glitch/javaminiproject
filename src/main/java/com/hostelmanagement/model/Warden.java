package com.hostelmanagement.model;

import com.hostelmanagement.enums.UserRole;

/**
 * Warden — staff member who manages day-to-day hostel operations.
 * Demonstrates INHERITANCE from User with additional fields.
 */
public class Warden extends User {
    private int staffId;
    private String assignedBlockName;
    private int assignedBlockId;

    public Warden() {
        setRole(UserRole.WARDEN);
    }

    public Warden(String username, String fullName, String email) {
        super(username, fullName, email, UserRole.WARDEN);
    }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getAssignedBlockName() { return assignedBlockName; }
    public void setAssignedBlockName(String name) { this.assignedBlockName = name; }

    public int getAssignedBlockId() { return assignedBlockId; }
    public void setAssignedBlockId(int id) { this.assignedBlockId = id; }

    @Override
    public String getRoleDescription() {
        return "Hostel Warden — manages students, allocations, complaints, and attendance";
    }
}
