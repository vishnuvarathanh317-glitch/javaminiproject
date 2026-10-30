package com.hostelmanagement.service;

import com.hostelmanagement.enums.FeeStatus;
import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.FeeRecord;
import com.hostelmanagement.model.Payment;
import com.hostelmanagement.repository.FeeRepository;
import com.hostelmanagement.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Hostel Fee Management Service.
 * Manages student fee obligations and records academic payments transactionally.
 */
public class FeeService {

    private final FeeRepository feeRepository;

    public FeeService() {
        this.feeRepository = new FeeRepository();
    }

    public FeeService(FeeRepository feeRepository) {
        this.feeRepository = feeRepository;
    }

    public FeeRecord createFeeObligation(int studentRecordId, String description,
                                        BigDecimal totalAmount, LocalDate dueDate) {
        InputValidator.requireNonEmpty(description, "Fee description");
        InputValidator.validatePositiveAmount(totalAmount, "Total amount");
        if (dueDate == null) throw new ValidationException("Fee due date is required.");

        FeeRecord record = new FeeRecord();
        record.setStudentRecordId(studentRecordId);
        record.setDescription(description.trim());
        record.setTotalAmount(totalAmount);
        record.setPaidAmount(BigDecimal.ZERO);
        record.setDueDate(dueDate);
        record.setStatus(FeeStatus.UNPAID);

        try {
            int id = feeRepository.insertFeeRecord(record);
            record.setId(id);
            return record;
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating fee obligation: " + e.getMessage(), e);
        }
    }

    public Payment recordPayment(int feeRecordId, BigDecimal amount, LocalDate paymentDate,
                                 String paymentMethod, String referenceNumber, int recordedByUserId) {
        InputValidator.validatePositiveAmount(amount, "Payment amount");
        InputValidator.requireNonEmpty(paymentMethod, "Payment method");
        if (paymentDate == null) paymentDate = LocalDate.now();

        try {
            Optional<FeeRecord> opt = feeRepository.findById(feeRecordId);
            if (opt.isEmpty()) throw new ValidationException("Fee record not found.");
            FeeRecord record = opt.get();

            BigDecimal outstanding = record.getOutstandingAmount();
            if (amount.compareTo(outstanding) > 0) {
                throw new ValidationException("Payment amount (" + amount + ") exceeds outstanding balance (" + outstanding + ").");
            }

            Payment payment = new Payment();
            payment.setFeeRecordId(feeRecordId);
            payment.setAmount(amount);
            payment.setPaymentDate(paymentDate);
            payment.setPaymentMethod(paymentMethod.trim());
            payment.setReferenceNumber(referenceNumber != null ? referenceNumber.trim() : "REF-" + System.currentTimeMillis());
            payment.setRecordedByUserId(recordedByUserId);

            feeRepository.recordPayment(payment);
            return payment;
        } catch (SQLException e) {
            throw new DatabaseException("Database error recording fee payment: " + e.getMessage(), e);
        }
    }

    public List<FeeRecord> getStudentFees(int studentRecordId) {
        try {
            return feeRepository.findByStudent(studentRecordId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving student fees: " + e.getMessage(), e);
        }
    }

    public List<FeeRecord> getAllFeeRecords() {
        try {
            return feeRepository.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving fee records: " + e.getMessage(), e);
        }
    }

    public List<Payment> getPaymentsForFee(int feeRecordId) {
        try {
            return feeRepository.findPaymentsByFee(feeRecordId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving payments: " + e.getMessage(), e);
        }
    }

    public BigDecimal[] getTotalFeeSummary() {
        try {
            return feeRepository.getTotalFeeSummary();
        } catch (SQLException e) {
            throw new DatabaseException("Database error calculating fee summary: " + e.getMessage(), e);
        }
    }
}
