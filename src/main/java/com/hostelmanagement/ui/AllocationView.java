package com.hostelmanagement.ui;

import com.hostelmanagement.ai.RoomRecommendationEngine;
import com.hostelmanagement.enums.RoomType;
import com.hostelmanagement.model.*;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.AllocationService;
import com.hostelmanagement.service.HostelService;
import com.hostelmanagement.service.StudentService;
import com.hostelmanagement.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart Bed Allocation & AI Recommendation Interface (Module D).
 * Demonstrates:
 *   - PriorityQueue data structure for candidate ranking
 *   - Explainable rule-based AI room recommendation engine
 *   - Transactional bed allocation and vacating
 *   - FIFO Waiting List queue processing
 */
public class AllocationView {

    private final VBox root;
    private final AllocationService allocationService = new AllocationService();
    private final StudentService studentService = new StudentService();
    private final HostelService hostelService = new HostelService();

    private final TableView<Allocation> activeAllocTable = new TableView<>();
    private final ObservableList<Allocation> activeAllocations = FXCollections.observableArrayList();

    private final ComboBox<Student> studentSelector = new ComboBox<>();
    private final ComboBox<String> blockPrefSelector = new ComboBox<>();
    private final ComboBox<Integer> floorPrefSelector = new ComboBox<>();
    private final ComboBox<RoomType> typePrefSelector = new ComboBox<>();
    private final VBox recommendationCardsContainer = new VBox(10);
    private RoomRecommendation selectedRecommendation;

    public AllocationView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));

        // ── TOP SECTION: AI ROOM RECOMMENDATION & ALLOCATION ENGINE ──
        VBox aiCard = new VBox(14);
        aiCard.getStyleClass().add("ai-card");

        Label aiHeader = new Label("🧠 Smart Room Recommendation Engine (Rule-based AI)");
        aiHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #5b21b6;");

        Label aiDesc = new Label("Select student and resident preferences to calculate suitability scores using PriorityQueue ranking.");
        aiDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6d28d9;");

        GridPane prefGrid = new GridPane();
        prefGrid.setHgap(12); prefGrid.setVgap(10);

        loadUnallocatedStudents();
        studentSelector.setPrefWidth(240);

        blockPrefSelector.getItems().addAll("Any Block", "Block A (Ramanujan)", "Block B (Gargi)");
        blockPrefSelector.setValue("Any Block");

        floorPrefSelector.getItems().addAll(null, 0, 1, 2);
        floorPrefSelector.setPromptText("Any Floor");

        typePrefSelector.getItems().addAll(null, RoomType.SINGLE, RoomType.DOUBLE, RoomType.TRIPLE);
        typePrefSelector.setPromptText("Any Room Type");

        prefGrid.add(new Label("Select Resident:"), 0, 0);
        prefGrid.add(studentSelector, 1, 0);
        prefGrid.add(new Label("Preferred Block:"), 2, 0);
        prefGrid.add(blockPrefSelector, 3, 0);

        prefGrid.add(new Label("Preferred Floor:"), 0, 1);
        prefGrid.add(floorPrefSelector, 1, 1);
        prefGrid.add(new Label("Preferred Room Type:"), 2, 1);
        prefGrid.add(typePrefSelector, 3, 1);

        Button recommendBtn = new Button("⚡ Generate AI Ranked Recommendations");
        recommendBtn.getStyleClass().add("btn-primary");
        recommendBtn.setStyle("-fx-background-color: #7c3aed;");
        recommendBtn.setOnAction(e -> runRecommendationEngine());

        Button waitlistBtn = new Button("+ Add to Waiting List Queue (FIFO)");
        waitlistBtn.getStyleClass().add("btn-secondary");
        waitlistBtn.setOnAction(e -> handleAddToWaitingList());

        HBox btnRow = new HBox(12, recommendBtn, waitlistBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        recommendationCardsContainer.setStyle("-fx-padding: 8px 0;");

        aiCard.getChildren().addAll(aiHeader, aiDesc, prefGrid, btnRow, recommendationCardsContainer);

        // ── BOTTOM SECTION: CURRENT ACTIVE ALLOCATIONS ──
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tTitle = new Label("Active Bed Allotments (Database Source of Truth)");
        tTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button vacateBtn = new Button("Vacate Selected Bed");
        vacateBtn.getStyleClass().add("btn-danger");
        vacateBtn.setOnAction(e -> handleVacateBed());

        tableHeader.getChildren().addAll(tTitle, spacer, vacateBtn);

        TableColumn<Allocation, String> regCol = new TableColumn<>("Register No");
        regCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegisterNumber()));
        regCol.setPrefWidth(120);

        TableColumn<Allocation, String> sNameCol = new TableColumn<>("Student Name");
        sNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));
        sNameCol.setPrefWidth(180);

        TableColumn<Allocation, String> blockCol = new TableColumn<>("Block");
        blockCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBlockName()));
        blockCol.setPrefWidth(150);

        TableColumn<Allocation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoomNumber()));
        roomCol.setPrefWidth(100);

        TableColumn<Allocation, String> bedCol = new TableColumn<>("Bed");
        bedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBedNumber()));
        bedCol.setPrefWidth(100);

        TableColumn<Allocation, String> dateCol = new TableColumn<>("Allotted Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getStartDate())));
        dateCol.setPrefWidth(120);

        activeAllocTable.getColumns().addAll(regCol, sNameCol, blockCol, roomCol, bedCol, dateCol);
        activeAllocTable.setItems(activeAllocations);
        activeAllocTable.setPrefHeight(220);
        VBox.setVgrow(activeAllocTable, Priority.ALWAYS);

        tableCard.getChildren().addAll(tableHeader, activeAllocTable);

        root.getChildren().addAll(aiCard, tableCard);
        loadActiveAllocations();
    }

    private void loadUnallocatedStudents() {
        studentSelector.getItems().clear();
        try {
            List<Student> all = studentService.getAllStudents();
            for (Student s : all) {
                if (allocationService.getActiveAllocation(s.getStudentRecordId()) == null) {
                    studentSelector.getItems().add(s);
                }
            }
            if (!studentSelector.getItems().isEmpty()) {
                studentSelector.setValue(studentSelector.getItems().get(0));
            }
        } catch (Exception ignored) {}
    }

    private void loadActiveAllocations() {
        activeAllocations.clear();
        try {
            activeAllocations.addAll(allocationService.getAllActiveAllocations());
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load allocations", ex.getMessage());
        }
    }

    private void runRecommendationEngine() {
        recommendationCardsContainer.getChildren().clear();
        selectedRecommendation = null;

        String blk = blockPrefSelector.getValue();
        if ("Any Block".equals(blk)) blk = null;
        Integer floor = floorPrefSelector.getValue();
        RoomType type = typePrefSelector.getValue();

        RoomRecommendationEngine.Preference pref = new RoomRecommendationEngine.Preference(blk, floor, type);
        List<RoomRecommendation> recommendations = allocationService.getRecommendations(pref);

        if (recommendations.isEmpty()) {
            Label noBeds = new Label("No available beds match current criteria, or hostel is at 100% occupancy.");
            noBeds.setStyle("-fx-text-fill: #dc2626; -fx-font-style: italic;");
            recommendationCardsContainer.getChildren().add(noBeds);
            return;
        }

        Label recCount = new Label("Ranked " + recommendations.size() + " available beds using PriorityQueue ordering:");
        recCount.setStyle("-fx-font-weight: bold; -fx-text-fill: #4c1d95; -fx-font-size: 12px;");
        recommendationCardsContainer.getChildren().add(recCount);

        // Display top recommendations as interactive cards
        int rank = 1;
        for (RoomRecommendation rec : recommendations) {
            if (rank > 4) break; // Show top 4 recommendations

            HBox card = new HBox(14);
            card.setStyle("-fx-background-color: #ffffff; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #ddd6fe; -fx-border-radius: 8px;");
            card.setAlignment(Pos.CENTER_LEFT);

            Label badge = new Label("#" + rank);
            badge.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #6d28d9; -fx-padding: 4px 10px; -fx-background-color: #ede9fe; -fx-background-radius: 20px;");

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label bedLabel = new Label("Bed: " + rec.getBed().getBedNumber() + " | Room: " + rec.getBed().getRoomNumber() + " (" + rec.getBed().getBlockName() + ", Floor " + rec.getBed().getFloorNumber() + ")");
            bedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");

            Label scoreLabel = new Label(String.format("Suitability Score: %.1f / 100", rec.getScore()));
            scoreLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: 600; -fx-font-size: 11px;");

            Label expLabel = new Label("AI Justification: " + rec.getExplanation());
            expLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
            expLabel.setWrapText(true);

            info.getChildren().addAll(bedLabel, scoreLabel, expLabel);

            Button allocateBtn = new Button("Confirm Allotment");
            allocateBtn.getStyleClass().add("btn-success");
            allocateBtn.setOnAction(e -> handleConfirmAllocation(rec.getBed().getId(), rec.getBed().getBedNumber()));

            card.getChildren().addAll(badge, info, allocateBtn);
            recommendationCardsContainer.getChildren().add(card);
            rank++;
        }
    }

    private void handleConfirmAllocation(int bedId, String bedNumber) {
        Student selectedStudent = studentSelector.getValue();
        if (selectedStudent == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Student Selected",
                    "Please choose a resident from the 'Select Resident' dropdown.");
            return;
        }

        if (ViewNavigator.showConfirmation("Confirm Allotment", "Authorize Bed Allotment",
                "Allocate Bed " + bedNumber + " to " + selectedStudent.getFullName() + " (" + selectedStudent.getRegisterNumber() + ")?")) {
            try {
                int staffUserId = SessionManager.getCurrentUser().getId();
                allocationService.allocateBed(selectedStudent.getStudentRecordId(), bedId, staffUserId, "AI Recommended allocation");
                loadActiveAllocations();
                loadUnallocatedStudents();
                recommendationCardsContainer.getChildren().clear();
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Bed Allocated",
                        "Bed " + bedNumber + " successfully allocated to " + selectedStudent.getFullName() + ".");
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Allocation Failed", "Could not complete allocation", ex.getMessage());
            }
        }
    }

    private void handleVacateBed() {
        Allocation selected = activeAllocTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Allocation Selected", "Please select an allotment from the table to vacate.");
            return;
        }

        if (ViewNavigator.showConfirmation("Vacate Bed", "Confirm Bed Vacation",
                "Vacate bed " + selected.getBedNumber() + " assigned to " + selected.getStudentName() + "?")) {
            try {
                allocationService.vacateBed(selected.getStudentRecordId(), "Vacated by warden");
                loadActiveAllocations();
                loadUnallocatedStudents();
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Bed Vacated", "Bed status reverted to AVAILABLE.");
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to vacate bed", ex.getMessage());
            }
        }
    }

    private void handleAddToWaitingList() {
        Student s = studentSelector.getValue();
        if (s == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Student Selected", "Please select a student to enqueue.");
            return;
        }
        try {
            String blk = "Any Block".equals(blockPrefSelector.getValue()) ? null : blockPrefSelector.getValue();
            String type = typePrefSelector.getValue() != null ? typePrefSelector.getValue().name() : null;
            int floor = floorPrefSelector.getValue() != null ? floorPrefSelector.getValue() : 0;

            allocationService.addToWaitingList(s.getStudentRecordId(), blk, type, floor);
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Waiting List", "Student Enqueued",
                    s.getFullName() + " added to the FIFO bed waiting list.");
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not add to waiting list", ex.getMessage());
        }
    }

    public VBox getView() {
        return root;
    }
}
