package com.hostelmanagement.ui;

import com.hostelmanagement.algorithms.OccupancyCalculator;
import com.hostelmanagement.enums.AttendanceStatus;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.AttendanceService;
import com.hostelmanagement.service.ComplaintService;
import com.hostelmanagement.service.HostelService;
import com.hostelmanagement.service.LeaveService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Warden Operations Dashboard.
 * Focused on operational student management: occupancy, attendance, complaints, and leave approvals.
 */
public class WardenDashboardView {

    private final VBox root;
    private final HostelService hostelService = new HostelService();
    private final ComplaintService complaintService = new ComplaintService();
    private final LeaveService leaveService = new LeaveService();
    private final AttendanceService attendanceService = new AttendanceService();

    public WardenDashboardView() {
        root = new VBox(24);
        root.setPadding(new Insets(10));

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        OccupancyCalculator.OccupancyStats occStats = new OccupancyCalculator.OccupancyStats(0,0,0,0,0,0.0);
        int pendingComplaints = 0;
        int pendingLeaves = 0;
        int presentToday = 0;

        try {
            occStats = hostelService.getOccupancyStats();
            pendingComplaints = complaintService.getPendingComplaintsCount();
            pendingLeaves = leaveService.getPendingLeavesCount();
            presentToday = attendanceService.getTodayAttendanceCount(AttendanceStatus.PRESENT);
        } catch (Exception ignored) {}

        VBox card1 = createStatCard("AVAILABLE BEDS", String.valueOf(occStats.getAvailableBeds()), "Ready for allocation", "#2563eb");
        VBox card2 = createStatCard("PENDING COMPLAINTS", String.valueOf(pendingComplaints), "Awaiting resolution", "#d97706");
        VBox card3 = createStatCard("LEAVE APPROVALS", String.valueOf(pendingLeaves), "Requests to review", "#7c3aed");
        VBox card4 = createStatCard("TODAY'S PRESENT", String.valueOf(presentToday), "Marked present", "#059669");

        statsGrid.add(card1, 0, 0);
        statsGrid.add(card2, 1, 0);
        statsGrid.add(card3, 2, 0);
        statsGrid.add(card4, 3, 0);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            statsGrid.getColumnConstraints().add(col);
        }

        HBox actionsRow = new HBox(16);
        VBox actionCard = new VBox(12);
        actionCard.getStyleClass().add("card");
        HBox.setHgrow(actionCard, Priority.ALWAYS);

        Label title = new Label("Daily Warden Responsibilities");
        title.getStyleClass().add("section-title");

        HBox buttons = new HBox(12);
        Button attBtn = new Button("Mark Daily Attendance");
        attBtn.getStyleClass().add("btn-primary");
        attBtn.setOnAction(e -> ViewNavigator.setContent(new AttendanceView().getView(), "Roll-Call Attendance"));

        Button leaveBtn = new Button("Review Leave Applications");
        leaveBtn.getStyleClass().add("btn-secondary");
        leaveBtn.setOnAction(e -> ViewNavigator.setContent(new LeaveManagementView().getView(), "Leave Approvals"));

        Button compBtn = new Button("View & Assign Complaints");
        compBtn.getStyleClass().add("btn-secondary");
        compBtn.setOnAction(e -> ViewNavigator.setContent(new ComplaintManagementView().getView(), "Complaints & Maintenance"));

        buttons.getChildren().addAll(attBtn, leaveBtn, compBtn);
        actionCard.getChildren().addAll(title, buttons);

        actionsRow.getChildren().add(actionCard);
        root.getChildren().addAll(statsGrid, actionsRow);
    }

    private VBox createStatCard(String header, String val, String subtitle, String accentColor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setStyle(card.getStyle() + "; -fx-border-color: " + accentColor + " transparent transparent transparent; -fx-border-width: 4px 1px 1px 1px;");

        Label h = new Label(header);
        h.getStyleClass().add("card-title");
        h.setStyle("-fx-font-size: 11px; -fx-text-fill: " + accentColor + ";");

        Label v = new Label(val);
        v.getStyleClass().add("stat-value");

        Label s = new Label(subtitle);
        s.getStyleClass().add("stat-subtitle");

        card.getChildren().addAll(h, v, s);
        return card;
    }

    public VBox getView() {
        return root;
    }
}
