package com.hostelmanagement.model;

import com.hostelmanagement.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Daily attendance record for a student. One record per student per date. */
public class AttendanceRecord {
    private int id;
    private int studentRecordId;
    private LocalDate date;
    private AttendanceStatus status;
    private int recordedByUserId;
    private LocalDateTime createdAt;
    // Transient display
    private String studentName;
    private String registerNumber;

    public AttendanceRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(int id) { this.studentRecordId = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate d) { this.date = d; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus s) { this.status = s; }
    public int getRecordedByUserId() { return recordedByUserId; }
    public void setRecordedByUserId(int id) { this.recordedByUserId = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String n) { this.studentName = n; }
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String r) { this.registerNumber = r; }
}
