package com.hostelmanagement.service;

import com.hostelmanagement.enums.AttendanceStatus;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.AttendanceRecord;
import com.hostelmanagement.repository.AttendanceRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Attendance Service.
 * Records attendance and prevents duplicate daily entries per student.
 */
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService() {
        this.attendanceRepository = new AttendanceRepository();
    }

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public AttendanceRecord recordAttendance(int studentRecordId, LocalDate date,
                                            AttendanceStatus status, int recordedByUserId, String remarks) {
        if (date == null) throw new ValidationException("Attendance date is required.");
        if (status == null) throw new ValidationException("Attendance status is required.");

        try {
            if (attendanceRepository.exists(studentRecordId, date)) {
                throw new ValidationException("Attendance for this student on " + date + " is already recorded.");
            }

            AttendanceRecord record = new AttendanceRecord();
            record.setStudentRecordId(studentRecordId);
            record.setDate(date);
            record.setStatus(status);
            record.setRecordedByUserId(recordedByUserId);

            attendanceRepository.insert(record);
            return record;
        } catch (SQLException e) {
            throw new DatabaseException("Database error recording attendance: " + e.getMessage(), e);
        }
    }

    public List<AttendanceRecord> getAttendanceByDate(LocalDate date) {
        if (date == null) date = LocalDate.now();
        try {
            return attendanceRepository.findByDate(date);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding attendance by date: " + e.getMessage(), e);
        }
    }

    public List<AttendanceRecord> getStudentAttendance(int studentRecordId) {
        try {
            return attendanceRepository.findByStudentAndMonth(studentRecordId, LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding student attendance: " + e.getMessage(), e);
        }
    }

    public int getTodayAttendanceCount(AttendanceStatus status) {
        try {
            return attendanceRepository.countTodayByStatus(status);
        } catch (SQLException e) {
            throw new DatabaseException("Database error counting today's attendance: " + e.getMessage(), e);
        }
    }
}
