package com.hostelmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A payment made against a fee obligation. */
public class Payment {
    private int id;
    private int feeRecordId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private int recordedByUserId;
    private LocalDateTime createdAt;

    public Payment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFeeRecordId() { return feeRecordId; }
    public void setFeeRecordId(int id) { this.feeRecordId = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal a) { this.amount = a; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate d) { this.paymentDate = d; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String m) { this.paymentMethod = m; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String r) { this.referenceNumber = r; }
    public int getRecordedByUserId() { return recordedByUserId; }
    public void setRecordedByUserId(int id) { this.recordedByUserId = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
}
