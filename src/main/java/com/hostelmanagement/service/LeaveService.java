package com.hostelmanagement.service;

import com.hostelmanagement.enums.LeaveStatus;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.LeaveRequest;
import com.hostelmanagement.repository.LeaveRepository;
import com.hostelmanagement.util.InputValidator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Leave Management Service.
 * Validates leave dates and processes warden approval workflows.
 */
public class LeaveService {

    private final LeaveRepository leaveRepository;

    public LeaveService() {
        this.leaveRepository = new LeaveRepository();
    }

    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    public LeaveRequest submitLeaveRequest(int studentRecordId, LocalDate startDate, LocalDate endDate, String reason) {
        InputValidator.validateLeaveDates(startDate, endDate);
        InputValidator.requireNonEmpty(reason, "Leave reason");

        LeaveRequest req = new LeaveRequest();
        req.setStudentRecordId(studentRecordId);
        req.setDepartureDate(startDate);
        req.setReturnDate(endDate);
        req.setReason(reason.trim());
        req.setStatus(LeaveStatus.PENDING);

        try {
            int id = leaveRepository.insert(req);
            req.setId(id);
            return req;
        } catch (SQLException e) {
            throw new DatabaseException("Database error submitting leave request: " + e.getMessage(), e);
        }
    }

    public void approveLeave(int leaveId, int approvedByUserId) {
        try {
            Optional<LeaveRequest> opt = leaveRepository.findById(leaveId);
            if (opt.isEmpty()) throw new ValidationException("Leave request not found.");
            LeaveRequest req = opt.get();
            if (req.getStatus() != LeaveStatus.PENDING) {
                throw new ValidationException("Cannot approve leave that is already " + req.getStatus() + ".");
            }

            leaveRepository.decide(leaveId, LeaveStatus.APPROVED, approvedByUserId, "Approved by warden");
        } catch (SQLException e) {
            throw new DatabaseException("Database error approving leave: " + e.getMessage(), e);
        }
    }

    public void rejectLeave(int leaveId, int approvedByUserId, String rejectionReason) {
        InputValidator.requireNonEmpty(rejectionReason, "Rejection reason");
        try {
            Optional<LeaveRequest> opt = leaveRepository.findById(leaveId);
            if (opt.isEmpty()) throw new ValidationException("Leave request not found.");
            LeaveRequest req = opt.get();
            if (req.getStatus() != LeaveStatus.PENDING) {
                throw new ValidationException("Cannot reject leave that is already " + req.getStatus() + ".");
            }

            leaveRepository.decide(leaveId, LeaveStatus.REJECTED, approvedByUserId, rejectionReason.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Database error rejecting leave: " + e.getMessage(), e);
        }
    }

    public List<LeaveRequest> getPendingLeaves() {
        try {
            return leaveRepository.findByStatus(LeaveStatus.PENDING);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding pending leaves: " + e.getMessage(), e);
        }
    }

    public List<LeaveRequest> getAllLeaves() {
        try {
            return leaveRepository.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding all leaves: " + e.getMessage(), e);
        }
    }

    public List<LeaveRequest> getLeavesByStudent(int studentRecordId) {
        try {
            return leaveRepository.findByStudent(studentRecordId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding student leaves: " + e.getMessage(), e);
        }
    }

    public int getPendingLeavesCount() {
        try {
            return leaveRepository.countPending();
        } catch (SQLException e) {
            throw new DatabaseException("Database error counting pending leaves: " + e.getMessage(), e);
        }
    }
}
