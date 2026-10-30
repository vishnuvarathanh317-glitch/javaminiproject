package com.hostelmanagement.service;

import com.hostelmanagement.exception.DatabaseException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.model.Notice;
import com.hostelmanagement.repository.NoticeRepository;
import com.hostelmanagement.util.InputValidator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Notice and Announcement Service.
 * Publishes and filters announcements by audience and expiry date.
 */
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService() {
        this.noticeRepository = new NoticeRepository();
    }

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public Notice publishNotice(String title, String body, String audience,
                                LocalDate expiryDate, int postedByUserId) {
        InputValidator.requireNonEmpty(title, "Notice title");
        InputValidator.requireNonEmpty(body, "Notice content");
        if (audience == null || audience.trim().isEmpty()) {
            audience = "ALL";
        }

        Notice notice = new Notice();
        notice.setTitle(title.trim());
        notice.setBody(body.trim());
        notice.setAudience(audience.trim().toUpperCase());
        notice.setPublishDate(LocalDate.now());
        notice.setExpiryDate(expiryDate);
        notice.setPostedByUserId(postedByUserId);

        try {
            int id = noticeRepository.insert(notice);
            notice.setId(id);
            return notice;
        } catch (SQLException e) {
            throw new DatabaseException("Database error publishing notice: " + e.getMessage(), e);
        }
    }

    public List<Notice> getActiveNotices(String audience) {
        try {
            return noticeRepository.findActiveForAudience(audience != null ? audience.trim().toUpperCase() : "ALL");
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding active notices: " + e.getMessage(), e);
        }
    }

    public List<Notice> getAllNotices() {
        try {
            return noticeRepository.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Database error finding notices: " + e.getMessage(), e);
        }
    }
}
