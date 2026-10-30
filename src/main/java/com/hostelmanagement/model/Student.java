package com.hostelmanagement.model;

import com.hostelmanagement.enums.UserRole;

/**
 * Student — a hostel resident linked to a User account.
 * Demonstrates INHERITANCE from User with student-specific data.
 *
 * COMPOSITION: a Student "has a" room allocation (referenced by IDs).
 */
public class Student extends User {
    private int studentRecordId;      // PK in students table
    private String registerNumber;    // unique college register number
    private String department;
    private int year;
    private String guardianName;
    private String guardianPhone;

    public Student() {
        setRole(UserRole.STUDENT);
    }

    public Student(String username, String fullName, String email,
                   String registerNumber, String department, int year) {
        super(username, fullName, email, UserRole.STUDENT);
        this.registerNumber = registerNumber;
        this.department = department;
        this.year = year;
    }

    // ── Student-specific accessors ─────────────────────────────

    public int getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(int id) { this.studentRecordId = id; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }

    @Override
    public String getRoleDescription() {
        return "Student — " + department + ", Year " + year;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + registerNumber + ")";
    }
}
