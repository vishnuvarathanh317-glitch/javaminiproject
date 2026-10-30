package com.hostelmanagement.ui;

import com.hostelmanagement.model.*;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.AllocationService;
import com.hostelmanagement.service.FeeService;
import com.hostelmanagement.service.NoticeService;
import com.hostelmanagement.service.StudentService;
import com.hostelmanagement.util.DateUtil;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Student Resident Portal.
 * Shows personal profile, room/bed assignment, fee obligations, and campus notices.
 */
public class StudentDashboardView {

    private final VBox root;
    private final StudentService studentService = new StudentService();
    private final AllocationService allocationService = new AllocationService();
    private final FeeService feeService = new FeeService();
    private final NoticeService noticeService = new NoticeService();

    public StudentDashboardView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));

        User currentUser = SessionManager.getCurrentUser();
        Student student = null;
        Allocation allocation = null;
        List<FeeRecord> fees = List.of();
        List<Notice> notices = List.of();

        if (currentUser != null) {
            try {
                student = studentService.getStudentByUserId(currentUser.getId());
                if (student != null) {
                    allocation = allocationService.getActiveAllocation(student.getStudentRecordId());
                    fees = feeService.getStudentFees(student.getStudentRecordId());
                }
                notices = noticeService.getActiveNotices("STUDENTS");
            } catch (Exception ex) {
                System.err.println("Student dashboard note: " + ex.getMessage());
            }
        }

        HBox topRow = new HBox(16);

        // ── PROFILE CARD ──
        VBox profileCard = new VBox(10);
        profileCard.getStyleClass().add("card");
        HBox.setHgrow(profileCard, Priority.ALWAYS);

        Label pTitle = new Label("Resident Information");
        pTitle.getStyleClass().add("section-title");

        String regNo = student != null ? student.getRegisterNumber() : "N/A";
        String dept = student != null ? student.getDepartment() : "N/A";
        int year = student != null ? student.getYear() : 1;
        String guardian = student != null && student.getGuardianName() != null ? student.getGuardianName() : "N/A";

        VBox pDetails = new VBox(6);
        pDetails.getChildren().addAll(
                new Label("Full Name: " + (student != null ? student.getFullName() : currentUser.getFullName())),
                new Label("Register Number: " + regNo),
                new Label("Department: " + dept),
                new Label("Year of Study: Year " + year),
                new Label("Guardian: " + guardian)
        );
        pDetails.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

        profileCard.getChildren().addAll(pTitle, pDetails);

        // ── ROOM & BED CARD ──
        VBox roomCard = new VBox(10);
        roomCard.getStyleClass().add("card");
        HBox.setHgrow(roomCard, Priority.ALWAYS);

        Label rTitle = new Label("Room & Bed Allotment");
        rTitle.getStyleClass().add("section-title");

        VBox rDetails = new VBox(6);
        if (allocation != null) {
            rDetails.getChildren().addAll(
                    new Label("Hostel Block: " + allocation.getBlockName()),
                    new Label("Room Number: " + allocation.getRoomNumber()),
                    new Label("Allocated Bed: " + allocation.getBedNumber()),
                    new Label("Allotted Since: " + DateUtil.formatDate(allocation.getStartDate())),
                    new Label("Status: ACTIVE RESIDENT")
            );
            rDetails.setStyle("-fx-font-size: 13px; -fx-text-fill: #059669; -fx-font-weight: 500;");
        } else {
            Label noRoom = new Label("No bed currently allocated.\nPlease contact the hostel warden for allotment.");
            noRoom.setStyle("-fx-text-fill: #d97706; -fx-font-style: italic;");
            rDetails.getChildren().add(noRoom);
        }

        HBox studentActions = new HBox(10);
        Button applyLeaveBtn = new Button("Apply for Leave");
        applyLeaveBtn.getStyleClass().add("btn-primary");
        applyLeaveBtn.setOnAction(e -> ViewNavigator.setContent(new LeaveManagementView().getView(), "My Leave Applications"));

        Button fileCompBtn = new Button("Report Maintenance Issue");
        fileCompBtn.getStyleClass().add("btn-secondary");
        fileCompBtn.setOnAction(e -> ViewNavigator.setContent(new ComplaintManagementView().getView(), "File Maintenance Complaint"));

        studentActions.getChildren().addAll(applyLeaveBtn, fileCompBtn);
        roomCard.getChildren().addAll(rTitle, rDetails, new Separator(), studentActions);

        topRow.getChildren().addAll(profileCard, roomCard);

        // ── NOTICES SECTION ──
        VBox noticeCard = new VBox(12);
        noticeCard.getStyleClass().add("card");

        Label nTitle = new Label("Latest Hostel Announcements");
        nTitle.getStyleClass().add("section-title");

        VBox noticeList = new VBox(10);
        if (notices.isEmpty()) {
            Label empty = new Label("No active notices at this time.");
            empty.setStyle("-fx-text-fill: #94a3b8;");
            noticeList.getChildren().add(empty);
        } else {
            for (Notice n : notices) {
                VBox item = new VBox(4);
                item.setStyle("-fx-background-color: #f8fafc; -fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");
                Label itemTitle = new Label(n.getTitle());
                itemTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
                Label itemBody = new Label(n.getBody());
                itemBody.setWrapText(true);
                itemBody.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
                Label itemDate = new Label("Posted: " + DateUtil.formatDate(n.getPublishDate()));
                itemDate.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                item.getChildren().addAll(itemTitle, itemBody, itemDate);
                noticeList.getChildren().add(item);
            }
        }

        noticeCard.getChildren().addAll(nTitle, noticeList);

        root.getChildren().addAll(topRow, noticeCard);
    }

    public VBox getView() {
        return root;
    }
}
