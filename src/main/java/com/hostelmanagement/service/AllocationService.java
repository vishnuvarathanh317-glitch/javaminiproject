package com.hostelmanagement.service;

import com.hostelmanagement.ai.RoomRecommendationEngine;
import com.hostelmanagement.enums.AllocationStatus;
import com.hostelmanagement.enums.BedStatus;
import com.hostelmanagement.exception.AllocationConflictException;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.*;
import com.hostelmanagement.repository.AllocationRepository;
import com.hostelmanagement.repository.HostelRepository;
import com.hostelmanagement.repository.StudentRepository;
import com.hostelmanagement.repository.WaitingListRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Smart Bed Allocation Service.
 * Protects database integrity using transactional updates and concurrency control.
 * Integrates the rule-based RoomRecommendationEngine.
 */
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final HostelRepository hostelRepository;
    private final StudentRepository studentRepository;
    private final WaitingListRepository waitingListRepository;
    private final RoomRecommendationEngine recommendationEngine;

    public AllocationService() {
        this.allocationRepository = new AllocationRepository();
        this.hostelRepository = new HostelRepository();
        this.studentRepository = new StudentRepository();
        this.waitingListRepository = new WaitingListRepository();
        this.recommendationEngine = new RoomRecommendationEngine();
    }

    public AllocationService(AllocationRepository allocationRepository,
                             HostelRepository hostelRepository,
                             StudentRepository studentRepository,
                             WaitingListRepository waitingListRepository,
                             RoomRecommendationEngine recommendationEngine) {
        this.allocationRepository = allocationRepository;
        this.hostelRepository = hostelRepository;
        this.studentRepository = studentRepository;
        this.waitingListRepository = waitingListRepository;
        this.recommendationEngine = recommendationEngine;
    }

    /**
     * Generates explainable room recommendations based on available beds and preferences.
     */
    public List<RoomRecommendation> getRecommendations(RoomRecommendationEngine.Preference preference) {
        try {
            List<Bed> availableBeds = hostelRepository.findAvailableBeds();
            return recommendationEngine.recommendBeds(availableBeds, preference);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding available beds: " + e.getMessage(), e);
        }
    }

    /**
     * Allocates a bed to a student in a database transaction with SELECT FOR UPDATE.
     */
    public Allocation allocateBed(int studentRecordId, int bedId, int allocatedByUserId, String remarks) {
        try {
            // Validation 1: Student exists and is active
            Optional<Student> studentOpt = studentRepository.findById(studentRecordId);
            if (studentOpt.isEmpty()) {
                throw new ValidationException("Student record not found.");
            }
            Student student = studentOpt.get();
            if (!student.isActive()) {
                throw new ValidationException("Student account is inactive. Cannot allocate bed.");
            }

            // Validation 2: Check for existing active allocation
            Optional<Allocation> existing = allocationRepository.findActiveByStudent(studentRecordId);
            if (existing.isPresent()) {
                throw new AllocationConflictException("Student already has an active allocation for bed '"
                        + existing.get().getBedNumber() + "' in room '" + existing.get().getRoomNumber() + "'.");
            }

            Allocation alloc = new Allocation();
            alloc.setStudentRecordId(studentRecordId);
            alloc.setBedId(bedId);
            alloc.setStartDate(LocalDate.now());
            alloc.setStatus(AllocationStatus.ACTIVE);
            alloc.setAllocatedByUserId(allocatedByUserId);

            int allocationId = allocationRepository.allocate(alloc);

            // Fulfill any pending waiting list entry for this student
            waitingListRepository.fulfillForStudent(studentRecordId);

            return allocationRepository.findById(allocationId).orElse(alloc);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("already has an active bed allocation")) {
                throw new AllocationConflictException(e.getMessage());
            }
            if (e.getMessage() != null && e.getMessage().contains("Bed is not available")) {
                throw new AllocationConflictException(e.getMessage());
            }
            throw new DatabaseException("Database error allocating bed: " + e.getMessage(), e);
        }
    }

    /**
     * Transfers student from their current bed to a new bed.
     */
    public void transferBed(int studentRecordId, int newBedId, int transferredByUserId, String reason) {
        try {
            Optional<Allocation> currentOpt = allocationRepository.findActiveByStudent(studentRecordId);
            if (currentOpt.isEmpty()) {
                throw new ValidationException("Student does not have an active bed allocation to transfer from.");
            }
            Allocation current = currentOpt.get();
            if (current.getBedId() == newBedId) {
                throw new ValidationException("Student is already allocated to this bed.");
            }

            // Vacate old bed
            allocationRepository.vacate(current.getId());

            // Allocate new bed
            Allocation newAlloc = new Allocation();
            newAlloc.setStudentRecordId(studentRecordId);
            newAlloc.setBedId(newBedId);
            newAlloc.setStartDate(LocalDate.now());
            newAlloc.setStatus(AllocationStatus.ACTIVE);
            newAlloc.setAllocatedByUserId(transferredByUserId);
            allocationRepository.allocate(newAlloc);

        } catch (SQLException e) {
            throw new DatabaseException("Database error during bed transfer: " + e.getMessage(), e);
        }
    }

    /**
     * Vacates the student's currently allocated bed.
     */
    public void vacateBed(int studentRecordId, String remarks) {
        try {
            Optional<Allocation> currentOpt = allocationRepository.findActiveByStudent(studentRecordId);
            if (currentOpt.isEmpty()) {
                throw new ValidationException("No active allocation found for this student.");
            }
            allocationRepository.vacate(currentOpt.get().getId());
        } catch (SQLException e) {
            throw new DatabaseException("Database error vacating bed: " + e.getMessage(), e);
        }
    }

    public Allocation getActiveAllocation(int studentRecordId) {
        try {
            return allocationRepository.findActiveByStudent(studentRecordId).orElse(null);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding active allocation: " + e.getMessage(), e);
        }
    }

    public List<Allocation> getAllActiveAllocations() {
        try {
            return allocationRepository.findActive();
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding active allocations: " + e.getMessage(), e);
        }
    }

    public List<Allocation> getStudentAllocationHistory(int studentRecordId) {
        try {
            return allocationRepository.findByStudent(studentRecordId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding allocation history: " + e.getMessage(), e);
        }
    }

    // ── WAITING LIST ────────────────────────────────────────────

    public void addToWaitingList(int studentRecordId, String prefBlock, String prefRoomType, int prefFloor) {
        try {
            Optional<Student> student = studentRepository.findById(studentRecordId);
            if (student.isEmpty()) throw new ValidationException("Student not found.");

            Optional<Allocation> active = allocationRepository.findActiveByStudent(studentRecordId);
            if (active.isPresent()) {
                throw new AllocationConflictException("Student already has an allocated bed. Waiting list is only for unallocated students.");
            }

            WaitingListEntry entry = new WaitingListEntry();
            entry.setStudentRecordId(studentRecordId);
            entry.setPreferredBlockName(prefBlock);
            entry.setPreferredRoomType(prefRoomType);
            entry.setPreferredFloor(prefFloor);

            waitingListRepository.insert(entry);
        } catch (SQLException e) {
            throw new DatabaseException("Database error adding to waiting list: " + e.getMessage(), e);
        }
    }

    public List<WaitingListEntry> getPendingWaitingList() {
        try {
            return waitingListRepository.findUnfulfilled();
        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching waiting list: " + e.getMessage(), e);
        }
    }
}
