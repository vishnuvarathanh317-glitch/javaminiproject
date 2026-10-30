package com.hostelmanagement.service;

import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.repository.AllocationRepository;
import com.hostelmanagement.repository.StudentRepository;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.security.PasswordUtil;
import com.hostelmanagement.util.InputValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Student Management Service.
 * Manages student profiles, linked authentication accounts, search, and history.
 */
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AllocationRepository allocationRepository;

    public StudentService() {
        this.studentRepository = new StudentRepository();
        this.userRepository = new UserRepository();
        this.allocationRepository = new AllocationRepository();
    }

    public StudentService(StudentRepository studentRepository, UserRepository userRepository, AllocationRepository allocationRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.allocationRepository = allocationRepository;
    }

    public Student registerStudent(String username, String password, String fullName, String email,
                                  String registerNumber, String department, int year,
                                  String guardianName, String guardianPhone) {
        InputValidator.validateUsername(username);
        InputValidator.requireNonEmpty(password, "Password");
        InputValidator.requireNonEmpty(fullName, "Full name");
        InputValidator.validateRegisterNumber(registerNumber);
        InputValidator.requireNonEmpty(department, "Department");
        InputValidator.validateEmail(email);
        InputValidator.validatePhone(guardianPhone, "Guardian phone");

        if (year < 1 || year > 5) {
            throw new ValidationException("Academic year must be between 1 and 5.");
        }

        try {
            if (userRepository.findByUsername(username.trim()).isPresent()) {
                throw new ValidationException("Username '" + username + "' is already registered.");
            }

            if (studentRepository.findByRegisterNumber(registerNumber.trim()).isPresent()) {
                throw new ValidationException("Register number '" + registerNumber + "' is already registered.");
            }

            Student student = new Student(username.trim(), fullName.trim(),
                    email != null ? email.trim() : null,
                    registerNumber.trim().toUpperCase(),
                    department.trim(), year);
            student.setPasswordHash(PasswordUtil.hashPassword(password));
            student.setActive(true);
            student.setGuardianName(guardianName != null ? guardianName.trim() : null);
            student.setGuardianPhone(guardianPhone != null ? guardianPhone.trim() : null);

            int studentRecordId = studentRepository.insert(student);
            student.setStudentRecordId(studentRecordId);
            return student;
        } catch (SQLException e) {
            throw new DatabaseException("Database error registering student: " + e.getMessage(), e);
        }
    }

    public void updateStudent(Student student) {
        if (student == null) throw new ValidationException("Student cannot be null.");
        InputValidator.requireNonEmpty(student.getFullName(), "Full name");
        InputValidator.validateRegisterNumber(student.getRegisterNumber());
        InputValidator.requireNonEmpty(student.getDepartment(), "Department");
        InputValidator.validateEmail(student.getEmail());
        InputValidator.validatePhone(student.getGuardianPhone(), "Guardian phone");

        try {
            // Verify duplicate register number
            Optional<Student> existing = studentRepository.findByRegisterNumber(student.getRegisterNumber().trim());
            if (existing.isPresent() && existing.get().getStudentRecordId() != student.getStudentRecordId()) {
                throw new ValidationException("Register number '" + student.getRegisterNumber() + "' is already used by another student.");
            }

            studentRepository.update(student);
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating student: " + e.getMessage(), e);
        }
    }

    public void setAccountStatus(int studentRecordId, boolean active) {
        try {
            studentRepository.setActiveStatus(studentRecordId, active);
        } catch (SQLException e) {
            throw new DatabaseException("Database error changing account status: " + e.getMessage(), e);
        }
    }

    public Student getStudentByRecordId(int studentRecordId) {
        try {
            return studentRepository.findById(studentRecordId).orElse(null);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding student: " + e.getMessage(), e);
        }
    }

    public Student getStudentByUserId(int userId) {
        try {
            return studentRepository.findByUserId(userId).orElse(null);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding student by user id: " + e.getMessage(), e);
        }
    }

    public Student getStudentByRegisterNumber(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) return null;
        try {
            return studentRepository.findByRegisterNumber(regNo.trim().toUpperCase()).orElse(null);
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding student by register number: " + e.getMessage(), e);
        }
    }

    public List<Student> getAllStudents() {
        try {
            return studentRepository.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching all students: " + e.getMessage(), e);
        }
    }

    public List<Student> searchStudents(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return studentRepository.findAll();
            }
            return studentRepository.search(query.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Database error searching students: " + e.getMessage(), e);
        }
    }

    public List<Allocation> getStudentAllocationHistory(int studentRecordId) {
        try {
            return allocationRepository.findByStudent(studentRecordId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching allocation history: " + e.getMessage(), e);
        }
    }

    public Allocation getActiveAllocation(int studentRecordId) {
        try {
            return allocationRepository.findActiveByStudent(studentRecordId).orElse(null);
        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching active allocation: " + e.getMessage(), e);
        }
    }
}
