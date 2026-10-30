package com.hostelmanagement.model;

import com.hostelmanagement.enums.LeaveStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Leave request submitted by a student. */
public class LeaveRequest {
    private int id;
    private int studentRecordId;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private String reason;
    private LeaveStatus status;
    private int decidedByUserId;
    private String decisionRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Transient display
    private String studentName;
    private String registerNumber;
    private String decidedByName;

    public LeaveRequest() { this.status = LeaveStatus.PENDING; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(int id) { this.studentRecordId = id; }
    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate d) { this.departureDate = d; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate d) { this.returnDate = d; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }
    public int getDecidedByUserId() { return decidedByUserId; }
    public void setDecidedByUserId(int id) { this.decidedByUserId = id; }
    public String getDecisionRemarks() { return decisionRemarks; }
    public void setDecisionRemarks(String r) { this.decisionRemarks = r; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime ts) { this.updatedAt = ts; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String n) { this.studentName = n; }
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String r) { this.registerNumber = r; }
    public String getDecidedByName() { return decidedByName; }
    public void setDecidedByName(String n) { this.decidedByName = n; }
}
