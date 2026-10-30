package com.hostelmanagement.model;

import com.hostelmanagement.enums.*;
import java.time.LocalDateTime;

/** A maintenance or service complaint filed by a student. */
public class Complaint {
    private int id;
    private int studentRecordId;
    private ComplaintCategory category;
    private String description;
    private String location;
    private ComplaintPriority priority;
    private ComplaintPriority recommendedPriority;
    private double priorityScore;
    private String priorityExplanation;
    private ComplaintStatus status;
    private int assignedToUserId;
    private String resolutionNotes;
    private int urgency;           // 1-5 reported by student
    private int impactCount;       // people affected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Transient display
    private String studentName;
    private String registerNumber;
    private String assignedToName;

    public Complaint() {
        this.status = ComplaintStatus.OPEN;
        this.urgency = 3;
        this.impactCount = 1;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(int id) { this.studentRecordId = id; }
    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory c) { this.category = c; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getLocation() { return location; }
    public void setLocation(String l) { this.location = l; }
    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority p) { this.priority = p; }
    public ComplaintPriority getRecommendedPriority() { return recommendedPriority; }
    public void setRecommendedPriority(ComplaintPriority p) { this.recommendedPriority = p; }
    public double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(double s) { this.priorityScore = s; }
    public String getPriorityExplanation() { return priorityExplanation; }
    public void setPriorityExplanation(String e) { this.priorityExplanation = e; }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus s) { this.status = s; }
    public int getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(int id) { this.assignedToUserId = id; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String n) { this.resolutionNotes = n; }
    public int getUrgency() { return urgency; }
    public void setUrgency(int u) { this.urgency = u; }
    public int getImpactCount() { return impactCount; }
    public void setImpactCount(int c) { this.impactCount = c; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime ts) { this.updatedAt = ts; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String n) { this.studentName = n; }
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String r) { this.registerNumber = r; }
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String n) { this.assignedToName = n; }
}
