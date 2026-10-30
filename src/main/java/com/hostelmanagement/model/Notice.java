package com.hostelmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Hostel notice or announcement. */
public class Notice {
    private int id;
    private String title;
    private String body;
    private String audience;       // ALL, STUDENTS, WARDENS
    private LocalDate publishDate;
    private LocalDate expiryDate;
    private int postedByUserId;
    private LocalDateTime createdAt;
    // Transient
    private String postedByName;

    public Notice() {
        this.audience = "ALL";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getBody() { return body; }
    public void setBody(String b) { this.body = b; }
    public String getAudience() { return audience; }
    public void setAudience(String a) { this.audience = a; }
    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate d) { this.publishDate = d; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate d) { this.expiryDate = d; }
    public int getPostedByUserId() { return postedByUserId; }
    public void setPostedByUserId(int id) { this.postedByUserId = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime ts) { this.createdAt = ts; }
    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String n) { this.postedByName = n; }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }
}
