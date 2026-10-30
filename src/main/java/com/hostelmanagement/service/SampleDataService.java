package com.hostelmanagement.service;

import com.hostelmanagement.enums.*;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.model.*;
import com.hostelmanagement.repository.HostelRepository;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.security.PasswordUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Safe Sample Demonstration Data Initializer (Section 14).
 * Creates realistic college hostel data if the database is currently empty.
 * Idempotent: checks before inserting and never duplicates records.
 */
public class SampleDataService {

    private final HostelRepository hostelRepository = new HostelRepository();
    private final UserRepository userRepository = new UserRepository();
    private final StudentService studentService = new StudentService();
    private final HostelService hostelService = new HostelService();
    private final AllocationService allocationService = new AllocationService();
    private final ComplaintService complaintService = new ComplaintService();
    private final LeaveService leaveService = new LeaveService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final FeeService feeService = new FeeService();
    private final NoticeService noticeService = new NoticeService();

    public boolean isSampleDataPresent() {
        try {
            return !hostelRepository.findAllHostels().isEmpty();
        } catch (SQLException e) {
            throw new DatabaseException("Error checking sample data status: " + e.getMessage(), e);
        }
    }

    public void initializeSampleDataIfEmpty() {
        if (isSampleDataPresent()) {
            return; // Already populated
        }

        try {
            // 1. Create Hostel
            Hostel hostel = hostelService.createHostel("Cauvery Residential Campus", "Engineering College Campus, South Wing");

            // 2. Create Blocks
            Block blockA = hostelService.createBlock(hostel.getId(), "Block A (Ramanujan)");
            Block blockB = hostelService.createBlock(hostel.getId(), "Block B (Gargi)");

            // 3. Create Floors for Block A
            Floor f0 = hostelService.createFloor(blockA.getId(), 0); // Ground Floor
            Floor f1 = hostelService.createFloor(blockA.getId(), 1); // 1st Floor

            // 4. Create Rooms & Beds in Floor 0 & Floor 1
            Room r101 = hostelService.createRoom(f0.getId(), "A-001", RoomType.DOUBLE, 2);
            Room r102 = hostelService.createRoom(f0.getId(), "A-002", RoomType.DOUBLE, 2);
            Room r201 = hostelService.createRoom(f1.getId(), "A-101", RoomType.TRIPLE, 3);
            Room r202 = hostelService.createRoom(f1.getId(), "A-102", RoomType.SINGLE, 1);

            // Floors for Block B
            Floor fb0 = hostelService.createFloor(blockB.getId(), 0);
            Room rb101 = hostelService.createRoom(fb0.getId(), "B-001", RoomType.DOUBLE, 2);

            // 5. Create Staff / Warden account
            Warden warden = new Warden("warden_sharma", "Dr. Rajesh Sharma", "warden@college.edu");
            warden.setPasswordHash(PasswordUtil.hashPassword("Warden@123"));
            warden.setActive(true);
            warden.setPhone("9876500001");
            int wardenId = userRepository.insert(warden);

            // 6. Register Sample Students
            Student s1 = studentService.registerStudent(
                    "arun_kumar", "Student@123", "Arun Kumar", "arun.kumar@college.edu",
                    "22BCSE101", "Computer Science & Engineering", 2,
                    "Suresh Kumar", "9876543210");

            Student s2 = studentService.registerStudent(
                    "priya_dharshini", "Student@123", "Priya Dharshini", "priya.d@college.edu",
                    "22BIT204", "Information Technology", 2,
                    "R. Dharshini", "9876543211");

            Student s3 = studentService.registerStudent(
                    "rahul_varma", "Student@123", "Rahul Varma", "rahul.v@college.edu",
                    "23BCSE305", "Computer Science & Engineering", 1,
                    "Mohan Varma", "9876543212");

            // 7. Allocate Bed to Arun Kumar in Room A-001
            List<Bed> r101Beds = hostelRepository.findBedsByRoom(r101.getId());
            if (!r101Beds.isEmpty()) {
                allocationService.allocateBed(s1.getStudentRecordId(), r101Beds.get(0).getId(), wardenId, "Standard allotment for Year 2");
            }

            // 8. Submit Sample Complaints
            complaintService.submitComplaint(
                    s1.getStudentRecordId(),
                    ComplaintCategory.WIFI,
                    "Wi-Fi connection drops intermittently on 1st floor router near Room A-001.",
                    "Room A-001 Desk Area",
                    4, 2);

            complaintService.submitComplaint(
                    s1.getStudentRecordId(),
                    ComplaintCategory.WATER_SUPPLY,
                    "Low pressure in tap in ground floor washroom during morning hours.",
                    "Ground floor wing rest room",
                    5, 12);

            // 9. Submit Sample Leave Request
            leaveService.submitLeaveRequest(
                    s1.getStudentRecordId(),
                    LocalDate.now().plusDays(2),
                    LocalDate.now().plusDays(5),
                    "Attending cousin's wedding ceremony in hometown.");

            // 10. Record Attendance for today
            attendanceService.recordAttendance(
                    s1.getStudentRecordId(),
                    LocalDate.now(),
                    AttendanceStatus.PRESENT,
                    wardenId,
                    "Regular evening biometric attendance roll-call");

            // 11. Create Fee Obligations & Record Partial Payment
            FeeRecord fee = feeService.createFeeObligation(
                    s1.getStudentRecordId(),
                    "Hostel & Mess Fee — Odd Semester 2026-27",
                    new BigDecimal("45000.00"),
                    LocalDate.now().plusMonths(1));

            feeService.recordPayment(
                    fee.getId(),
                    new BigDecimal("25000.00"),
                    LocalDate.now(),
                    "UPI / Bank Transfer",
                    "TXN-20261021-9988",
                    wardenId);

            // 12. Publish Notices
            noticeService.publishNotice(
                    "Welcome to Academic Year 2026-27 Hostel Orientation",
                    "All residents are requested to attend the hostel orientation and rules briefing session this Friday at 6:00 PM in the Common Hall. Biometric attendance will be recorded.",
                    "ALL",
                    LocalDate.now().plusWeeks(2),
                    wardenId);

            noticeService.publishNotice(
                    "Hostel Mess Committee Election Notice",
                    "Nominations are invited for student mess representatives for the academic term. Interested students may register at the Warden Office before the weekend.",
                    "STUDENTS",
                    LocalDate.now().plusWeeks(1),
                    wardenId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error during sample data initialization: " + e.getMessage(), e);
        }
    }
}
