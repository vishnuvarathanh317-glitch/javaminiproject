package com.hostelmanagement.service;

import com.hostelmanagement.ai.ComplaintPrioritizer;
import com.hostelmanagement.enums.ComplaintCategory;
import com.hostelmanagement.enums.ComplaintPriority;
import com.hostelmanagement.enums.ComplaintStatus;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.model.ComplaintPriorityRecommendation;
import com.hostelmanagement.repository.ComplaintRepository;
import com.hostelmanagement.util.InputValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Complaint Management Service.
 * Implements rule-based AI prioritization and controlled status lifecycle.
 */
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintPrioritizer prioritizer;

    public ComplaintService() {
        this.complaintRepository = new ComplaintRepository();
        this.prioritizer = new ComplaintPrioritizer();
    }

    public ComplaintService(ComplaintRepository complaintRepository, ComplaintPrioritizer prioritizer) {
        this.complaintRepository = complaintRepository;
        this.prioritizer = prioritizer;
    }

    /**
     * Submits a complaint, applying AI priority recommendation automatically.
     */
    public Complaint submitComplaint(int studentRecordId, ComplaintCategory category, String description,
                                     String location, int urgency, int impactCount) {
        if (category == null) throw new ValidationException("Complaint category is required.");
        InputValidator.requireNonEmpty(description, "Complaint description");

        // Calculate AI Priority Recommendation (0 waiting hours on initial submission)
        ComplaintPriorityRecommendation rec = prioritizer.calculatePriority(category, urgency, impactCount, 0);

        Complaint complaint = new Complaint();
        complaint.setStudentRecordId(studentRecordId);
        complaint.setCategory(category);
        complaint.setDescription(description.trim());
        complaint.setLocation(location != null ? location.trim() : "");
        complaint.setUrgency(Math.max(1, Math.min(5, urgency)));
        complaint.setImpactCount(Math.max(1, impactCount));
        complaint.setRecommendedPriority(rec.getRecommendedPriority());
        complaint.setPriorityScore(rec.getScore());
        complaint.setPriorityExplanation(rec.getExplanation());
        // Default assigned priority = recommended priority
        complaint.setPriority(rec.getRecommendedPriority());
        complaint.setStatus(ComplaintStatus.OPEN);

        try {
            int id = complaintRepository.insert(complaint);
            complaint.setId(id);
            return complaint;
        } catch (SQLException e) {
            throw new DatabaseException("Database error submitting complaint: " + e.getMessage(), e);
        }
    }

    /**
     * Transition complaint status with state validation and audit trail.
     */
    public void updateStatus(int complaintId, ComplaintStatus newStatus, int changedByUserId, String notes) {
        try {
            complaintRepository.updateStatus(complaintId, newStatus, changedByUserId, notes);
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating complaint status: " + e.getMessage(), e);
        }
    }

    public Complaint getComplaintById(int complaintId) {
        try {
            return complaintRepository.findById(complaintId).orElse(null);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding complaint: " + e.getMessage(), e);
        }
    }

    public List<Complaint> getAllComplaints() {
        try {
            return complaintRepository.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving complaints: " + e.getMessage(), e);
        }
    }

    public List<Complaint> getComplaintsByStudent(int studentRecordId) {
        try {
            return complaintRepository.findByStudent(studentRecordId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving complaints by student: " + e.getMessage(), e);
        }
    }

    public List<Complaint> getComplaintsByStatus(ComplaintStatus status) {
        try {
            return complaintRepository.findByStatus(status);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving complaints by status: " + e.getMessage(), e);
        }
    }

    public int getPendingComplaintsCount() {
        try {
            return complaintRepository.countPending();
        } catch (SQLException e) {
            throw new DatabaseException("Database error counting complaints: " + e.getMessage(), e);
        }
    }
}
