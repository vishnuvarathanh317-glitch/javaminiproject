package com.hostelmanagement.model;

import java.time.LocalDateTime;

/** Waiting-list entry for bed allocation. Processed FIFO. */
public class WaitingListEntry {
    private int id;
    private int studentRecordId;
    private String preferredBlockName;
    private String preferredRoomType;
    private int preferredFloor;
    private boolean fulfilled;
    private LocalDateTime createdAt;
    // Transient
    private String studentName;
    private String registerNumber;

    public WaitingListEntry() { this.fulfilled = false; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(int id) { this.studentRecordId = id; }
    public String getPreferredBlockName() { return preferredBlockName; }
    public void setPreferredBlockName(String n) { this.preferredBlockName = n; }
    public String getPreferredRoomType() { return preferredRoomType; }
    public void setPreferredRoomType(String t) { this.preferredRoomType = t; }
    public int getPreferredFloor() { return preferredFloor; }
    public void setPreferredFloor(int f) { this.preferredFloor = f; }
    public boolean isFulfilled() { return fulfilled; }
    public void setFulfilled(boolean f) { this.fulfilled = f; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String n) { this.studentName = n; }
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String r) { this.registerNumber = r; }
}
