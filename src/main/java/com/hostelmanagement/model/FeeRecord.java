package com.hostelmanagement.model;

import com.hostelmanagement.enums.FeeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A fee obligation for a student (e.g., hostel fee for a semester). */
public class FeeRecord {
    private int id;
    private int studentRecordId;
    private String description;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private FeeStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Transient
    private String studentName;
    private String registerNumber;

    public FeeRecord() {
        this.paidAmount = BigDecimal.ZERO;
        this.status = FeeStatus.UNPAID;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(int id) { this.studentRecordId = id; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal a) { this.totalAmount = a; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal a) { this.paidAmount = a; }
    public FeeStatus getStatus() { return status; }
    public void setStatus(FeeStatus s) { this.status = s; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate d) { this.dueDate = d; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime ts) { this.updatedAt = ts; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String n) { this.studentName = n; }
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String r) { this.registerNumber = r; }

    public BigDecimal getOutstandingAmount() {
        return totalAmount.subtract(paidAmount);
    }
}
