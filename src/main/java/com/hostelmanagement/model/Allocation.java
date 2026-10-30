package com.hostelmanagement.model;

import com.hostelmanagement.enums.AllocationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Records a student's bed allocation.
 * Historical records (TRANSFERRED, VACATED) are preserved.
 */
public class Allocation {
    private int id;
    private int studentRecordId;
    private int bedId;
    private LocalDate startDate;
    private LocalDate endDate;
    private AllocationStatus status;
    private int allocatedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Transient display fields
    private String studentName;
    private String registerNumber;
    private String bedNumber;
    private String roomNumber;
    private String blockName;

    public Allocation() { this.status = AllocationStatus.ACTIVE; }

    // ── Accessors ─────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(int id) { this.studentRecordId = id; }
    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public AllocationStatus getStatus() { return status; }
    public void setStatus(AllocationStatus status) { this.status = status; }
    public int getAllocatedByUserId() { return allocatedByUserId; }
    public void setAllocatedByUserId(int id) { this.allocatedByUserId = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime ts) { this.updatedAt = ts; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String n) { this.studentName = n; }
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String r) { this.registerNumber = r; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String b) { this.bedNumber = b; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String r) { this.roomNumber = r; }
    public String getBlockName() { return blockName; }
    public void setBlockName(String b) { this.blockName = b; }
}
