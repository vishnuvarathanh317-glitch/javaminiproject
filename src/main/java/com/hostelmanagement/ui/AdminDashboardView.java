package com.hostelmanagement.ui;

import com.hostelmanagement.algorithms.OccupancyCalculator;
import com.hostelmanagement.service.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

import java.math.BigDecimal;

/**
 * Administrator Overview Dashboard.
 * Displays real-time database-calculated metrics, occupancy gauge, and quick operational shortcuts.
 */
public class AdminDashboardView {

    private final VBox root;
    private final StudentService studentService = new StudentService();
    private final HostelService hostelService = new HostelService();
    private final ComplaintService complaintService = new ComplaintService();
    private final LeaveService leaveService = new LeaveService();
    private final FeeService feeService = new FeeService();

    public AdminDashboardView() {
        root = new VBox(24);
        root.setPadding(new Insets(10));

        // ── TOP STATS GRID ──
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        // Fetch live stats safely
        int studentCount = 0;
        OccupancyCalculator.OccupancyStats occStats = new OccupancyCalculator.OccupancyStats(0,0,0,0,0,0.0);
        int pendingComplaints = 0;
        int pendingLeaves = 0;
        BigDecimal[] feeSummary = new BigDecimal[]{ BigDecimal.ZERO, BigDecimal.ZERO };

        try {
            studentCount = studentService.getAllStudents().size();
            occStats = hostelService.getOccupancyStats();
            pendingComplaints = complaintService.getPendingComplaintsCount();
            pendingLeaves = leaveService.getPendingLeavesCount();
            feeSummary = feeService.getTotalFeeSummary();
        } catch (Exception ex) {
            System.err.println("Dashboard metric load note: " + ex.getMessage());
        }

        VBox card1 = createStatCard("TOTAL RESIDENTS", String.valueOf(studentCount), "Active registered students", "#2563eb");
        VBox card2 = createStatCard("ROOM OCCUPANCY", String.format("%.1f%%", occStats.getOccupancyRate()),
                occStats.getOccupiedBeds() + " occupied / " + occStats.getAvailableBeds() + " available", "#059669");
        VBox card3 = createStatCard("PENDING COMPLAINTS", String.valueOf(pendingComplaints), "Awaiting resolution", "#d97706");
        VBox card4 = createStatCard("LEAVE REQUESTS", String.valueOf(pendingLeaves), "Pending approvals", "#7c3aed");

        statsGrid.add(card1, 0, 0);
        statsGrid.add(card2, 1, 0);
        statsGrid.add(card3, 2, 0);
        statsGrid.add(card4, 3, 0);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            statsGrid.getColumnConstraints().add(col);
        }

        // ── SECOND ROW: OCCUPANCY & FINANCIAL SUMMARY ──
        HBox midRow = new HBox(16);

        // Occupancy Detail Card
        VBox occCard = new VBox(12);
        occCard.getStyleClass().add("card");
        HBox.setHgrow(occCard, Priority.ALWAYS);

        Label occTitle = new Label("Bed Capacity & Live Occupancy");
        occTitle.getStyleClass().add("section-title");

        ProgressBar occProgress = new ProgressBar(occStats.getOccupancyRate() / 100.0);
        occProgress.setMaxWidth(Double.MAX_VALUE);
        occProgress.setPrefHeight(18);
        occProgress.setStyle("-fx-accent: #2563eb;");

        HBox occDetails = new HBox(20);
        occDetails.getChildren().addAll(
                new Label("Total Beds: " + occStats.getTotalBeds()),
                new Label("Occupied: " + occStats.getOccupiedBeds()),
                new Label("Available: " + occStats.getAvailableBeds()),
                new Label("Maintenance: " + occStats.getMaintenanceBeds())
        );
        occDetails.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        Button allocateShortcut = new Button("Open Smart Allocation Engine");
        allocateShortcut.getStyleClass().add("btn-primary");
        allocateShortcut.setOnAction(e -> ViewNavigator.setContent(new AllocationView().getView(), "Smart Bed Allotment"));

        occCard.getChildren().addAll(occTitle, occProgress, occDetails, allocateShortcut);

        // Financial Summary Card
        VBox feeCard = new VBox(12);
        feeCard.getStyleClass().add("card");
        feeCard.setPrefWidth(380);

        Label feeTitle = new Label("Hostel Fee Collections");
        feeTitle.getStyleClass().add("section-title");

        BigDecimal totalObligations = feeSummary[0];
        BigDecimal totalCollected = feeSummary[1];
        BigDecimal outstanding = totalObligations.subtract(totalCollected);

        VBox feeStats = new VBox(6);
        feeStats.getChildren().addAll(
                new Label("Total Fees Billed: ₹ " + totalObligations),
                new Label("Collected to Date: ₹ " + totalCollected),
                new Label("Outstanding Balance: ₹ " + outstanding)
        );
        feeStats.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #334155;");

        Button viewFeesBtn = new Button("View Fee Ledger");
        viewFeesBtn.getStyleClass().add("btn-secondary");
        viewFeesBtn.setOnAction(e -> ViewNavigator.setContent(new FeeManagementView().getView(), "Hostel Fees & Payments"));

        feeCard.getChildren().addAll(feeTitle, feeStats, viewFeesBtn);

        midRow.getChildren().addAll(occCard, feeCard);

        root.getChildren().addAll(statsGrid, midRow);
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
