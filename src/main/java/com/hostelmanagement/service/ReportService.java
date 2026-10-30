package com.hostelmanagement.service;

import com.hostelmanagement.algorithms.OccupancyCalculator;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.model.*;
import com.hostelmanagement.repository.*;
import com.hostelmanagement.util.CsvExporter;
import com.hostelmanagement.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reporting and Analytics Service (Module J).
 * Compiles real-time metrics from the database and exports to CSV.
 */
public class ReportService {

    private final HostelRepository hostelRepository;
    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;
    private final ComplaintRepository complaintRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeeRepository feeRepository;

    public ReportService() {
        this.hostelRepository = new HostelRepository();
        this.studentRepository = new StudentRepository();
        this.allocationRepository = new AllocationRepository();
        this.complaintRepository = new ComplaintRepository();
        this.leaveRepository = new LeaveRepository();
        this.attendanceRepository = new AttendanceRepository();
        this.feeRepository = new FeeRepository();
    }

    public OccupancyCalculator.OccupancyStats getOccupancySummary() {
        try {
            return OccupancyCalculator.calculate(hostelRepository.findAllBeds());
        } catch (SQLException e) {
            throw new DatabaseException("Database error calculating occupancy summary: " + e.getMessage(), e);
        }
    }

    public void exportOccupancyReport(File file) throws IOException {
        try {
            List<Bed> beds = hostelRepository.findAllBeds();
            List<String> headers = List.of("Bed ID", "Bed Number", "Room", "Floor", "Block", "Status");
            List<List<String>> rows = new ArrayList<>();
            for (Bed b : beds) {
                rows.add(List.of(
                        String.valueOf(b.getId()),
                        b.getBedNumber() != null ? b.getBedNumber() : "",
                        b.getRoomNumber() != null ? b.getRoomNumber() : "",
                        String.valueOf(b.getFloorNumber()),
                        b.getBlockName() != null ? b.getBlockName() : "",
                        b.getStatus() != null ? b.getStatus().name() : ""
                ));
            }
            CsvExporter.exportToCsv(file, headers, rows);
        } catch (SQLException e) {
            throw new DatabaseException("Database error exporting occupancy: " + e.getMessage(), e);
        }
    }

    public void exportAllocationsReport(File file) throws IOException {
        try {
            List<Allocation> list = allocationRepository.findActive();
            List<String> headers = List.of("Allocation ID", "Student Name", "Register No", "Room", "Bed", "Block", "Allocated Date");
            List<List<String>> rows = new ArrayList<>();
            for (Allocation a : list) {
                rows.add(List.of(
                        String.valueOf(a.getId()),
                        a.getStudentName() != null ? a.getStudentName() : "",
                        a.getRegisterNumber() != null ? a.getRegisterNumber() : "",
                        a.getRoomNumber() != null ? a.getRoomNumber() : "",
                        a.getBedNumber() != null ? a.getBedNumber() : "",
                        a.getBlockName() != null ? a.getBlockName() : "",
                        DateUtil.formatDate(a.getStartDate())
                ));
            }
            CsvExporter.exportToCsv(file, headers, rows);
        } catch (SQLException e) {
            throw new DatabaseException("Database error exporting allocations: " + e.getMessage(), e);
        }
    }

    public void exportComplaintsReport(File file) throws IOException {
        try {
            List<Complaint> list = complaintRepository.findAll();
            List<String> headers = List.of("ID", "Student", "Category", "Priority", "Status", "Description", "Created Date");
            List<List<String>> rows = new ArrayList<>();
            for (Complaint c : list) {
                rows.add(List.of(
                        String.valueOf(c.getId()),
                        c.getStudentName() != null ? c.getStudentName() : "",
                        c.getCategory() != null ? c.getCategory().name() : "",
                        c.getPriority() != null ? c.getPriority().name() : "",
                        c.getStatus() != null ? c.getStatus().name() : "",
                        c.getDescription() != null ? c.getDescription() : "",
                        DateUtil.formatDateTime(c.getCreatedAt())
                ));
            }
            CsvExporter.exportToCsv(file, headers, rows);
        } catch (SQLException e) {
            throw new DatabaseException("Database error exporting complaints: " + e.getMessage(), e);
        }
    }

    public void exportFeeReport(File file) throws IOException {
        try {
            List<FeeRecord> list = feeRepository.findAll();
            List<String> headers = List.of("Fee ID", "Student", "Register No", "Description", "Total Amount", "Paid Amount", "Outstanding", "Status", "Due Date");
            List<List<String>> rows = new ArrayList<>();
            for (FeeRecord f : list) {
                rows.add(List.of(
                        String.valueOf(f.getId()),
                        f.getStudentName() != null ? f.getStudentName() : "",
                        f.getRegisterNumber() != null ? f.getRegisterNumber() : "",
                        f.getDescription() != null ? f.getDescription() : "",
                        f.getTotalAmount() != null ? f.getTotalAmount().toString() : "0",
                        f.getPaidAmount() != null ? f.getPaidAmount().toString() : "0",
                        f.getOutstandingAmount().toString(),
                        f.getStatus() != null ? f.getStatus().name() : "",
                        DateUtil.formatDate(f.getDueDate())
                ));
            }
            CsvExporter.exportToCsv(file, headers, rows);
        } catch (SQLException e) {
            throw new DatabaseException("Database error exporting fees: " + e.getMessage(), e);
        }
    }
}
