package com.hostelmanagement.ui;

import com.hostelmanagement.algorithms.OccupancyCalculator;
import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.model.FeeRecord;
import com.hostelmanagement.service.*;
import com.hostelmanagement.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Reporting & Analytics Screen (Module J).
 * Compiles real-time reports from persistent MySQL data and exports to standardized CSV.
 */
public class ReportsView {

    private final VBox root;
    private final ReportService reportService = new ReportService();
    private final HostelService hostelService = new HostelService();
    private final AllocationService allocationService = new AllocationService();
    private final ComplaintService complaintService = new ComplaintService();
    private final FeeService feeService = new FeeService();

    private final ComboBox<String> reportTypeBox = new ComboBox<>();
    private final TableView<ObservableList<String>> reportTable = new TableView<>();
    private final Label summaryMetricLabel = new Label();

    public ReportsView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        // ── TOP CONTROLS ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Institutional Reports & Analytics");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        reportTypeBox.getItems().addAll(
                "Room Occupancy & Bed Inventory",
                "Active Resident Allocations",
                "Maintenance & Complaints Ledger",
                "Fee Collections & Dues"
        );
        reportTypeBox.setValue("Room Occupancy & Bed Inventory");
        reportTypeBox.setOnAction(e -> loadSelectedReport());

        Button exportBtn = new Button("📥 Export to CSV");
        exportBtn.getStyleClass().add("btn-primary");
        exportBtn.setOnAction(e -> handleExportCsv());

        topBar.getChildren().addAll(title, spacer, new Label("Report:"), reportTypeBox, exportBtn);

        // ── SUMMARY CARD ──
        summaryMetricLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
        HBox summaryCard = new HBox(summaryMetricLabel);
        summaryCard.getStyleClass().add("card");
        summaryCard.setStyle("-fx-padding: 10px 16px;");

        // ── TABLE ──
        VBox.setVgrow(reportTable, Priority.ALWAYS);

        root.getChildren().addAll(topBar, summaryCard, reportTable);
        loadSelectedReport();
    }

    private void loadSelectedReport() {
        reportTable.getColumns().clear();
        reportTable.getItems().clear();

        String type = reportTypeBox.getValue();
        if ("Room Occupancy & Bed Inventory".equals(type)) {
            loadOccupancyReport();
        } else if ("Active Resident Allocations".equals(type)) {
            loadAllocationsReport();
        } else if ("Maintenance & Complaints Ledger".equals(type)) {
            loadComplaintsReport();
        } else if ("Fee Collections & Dues".equals(type)) {
            loadFeeReport();
        }
    }

    private void loadOccupancyReport() {
        setupColumns("Bed ID", "Bed Number", "Room", "Floor", "Block", "Status");
        OccupancyCalculator.OccupancyStats stats = reportService.getOccupancySummary();
        summaryMetricLabel.setText(String.format("Total Beds: %d | Occupied: %d | Available: %d | Occupancy Rate: %.1f%%",
                stats.getTotalBeds(), stats.getOccupiedBeds(), stats.getAvailableBeds(), stats.getOccupancyRate()));

        try {
            hostelService.getAllBeds().forEach(b -> {
                ObservableList<String> row = FXCollections.observableArrayList(
                        String.valueOf(b.getId()),
                        b.getBedNumber() != null ? b.getBedNumber() : "",
                        b.getRoomNumber() != null ? b.getRoomNumber() : "",
                        String.valueOf(b.getFloorNumber()),
                        b.getBlockName() != null ? b.getBlockName() : "",
                        b.getStatus().name()
                );
                reportTable.getItems().add(row);
            });
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load occupancy report", ex.getMessage());
        }
    }

    private void loadAllocationsReport() {
        setupColumns("Alloc ID", "Register No", "Resident Name", "Block", "Room", "Bed", "Allotted Date");
        List<Allocation> list = allocationService.getAllActiveAllocations();
        summaryMetricLabel.setText("Active Bed Allotments Count: " + list.size());

        for (Allocation a : list) {
            ObservableList<String> row = FXCollections.observableArrayList(
                    String.valueOf(a.getId()),
                    a.getRegisterNumber(),
                    a.getStudentName(),
                    a.getBlockName(),
                    a.getRoomNumber(),
                    a.getBedNumber(),
                    DateUtil.formatDate(a.getStartDate())
            );
            reportTable.getItems().add(row);
        }
    }

    private void loadComplaintsReport() {
        setupColumns("ID", "Student", "Category", "Priority", "Status", "Description", "Filed Date");
        List<Complaint> list = complaintService.getAllComplaints();
        summaryMetricLabel.setText("Total Maintenance Complaints: " + list.size() + " (" + complaintService.getPendingComplaintsCount() + " pending)");

        for (Complaint c : list) {
            ObservableList<String> row = FXCollections.observableArrayList(
                    String.valueOf(c.getId()),
                    c.getStudentName() != null ? c.getStudentName() : "",
                    c.getCategory().name(),
                    c.getPriority().name(),
                    c.getStatus().name(),
                    c.getDescription(),
                    DateUtil.formatDateTime(c.getCreatedAt())
            );
            reportTable.getItems().add(row);
        }
    }

    private void loadFeeReport() {
        setupColumns("Fee ID", "Register No", "Resident", "Term", "Billed (₹)", "Paid (₹)", "Balance (₹)", "Status", "Due Date");
        List<FeeRecord> list = feeService.getAllFeeRecords();
        summaryMetricLabel.setText("Total Fee Records: " + list.size());

        for (FeeRecord f : list) {
            ObservableList<String> row = FXCollections.observableArrayList(
                    String.valueOf(f.getId()),
                    f.getRegisterNumber(),
                    f.getStudentName(),
                    f.getDescription(),
                    "₹ " + f.getTotalAmount(),
                    "₹ " + f.getPaidAmount(),
                    "₹ " + f.getOutstandingAmount(),
                    f.getStatus().name(),
                    DateUtil.formatDate(f.getDueDate())
            );
            reportTable.getItems().add(row);
        }
    }

    private void setupColumns(String... headers) {
        reportTable.getColumns().clear();
        for (int i = 0; i < headers.length; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(colIndex)));
            col.setPrefWidth(130);
            reportTable.getColumns().add(col);
        }
    }

    private void handleExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report as CSV");
        String safeName = reportTypeBox.getValue().toLowerCase().replace(" ", "_") + ".csv";
        chooser.setInitialFileName(safeName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = chooser.showSaveDialog(ViewNavigator.getPrimaryStage());
        if (file != null) {
            try {
                String type = reportTypeBox.getValue();
                if ("Room Occupancy & Bed Inventory".equals(type)) {
                    reportService.exportOccupancyReport(file);
                } else if ("Active Resident Allocations".equals(type)) {
                    reportService.exportAllocationsReport(file);
                } else if ("Maintenance & Complaints Ledger".equals(type)) {
                    reportService.exportComplaintsReport(file);
                } else if ("Fee Collections & Dues".equals(type)) {
                    reportService.exportFeeReport(file);
                }
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Report Exported",
                        "Report exported successfully to: " + file.getAbsolutePath());
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not export CSV", ex.getMessage());
            }
        }
    }

    public VBox getView() {
        return root;
    }
}
