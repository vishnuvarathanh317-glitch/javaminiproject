package com.hostelmanagement.ui;

import com.hostelmanagement.enums.ComplaintCategory;
import com.hostelmanagement.enums.ComplaintPriority;
import com.hostelmanagement.enums.ComplaintStatus;
import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.model.User;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.ComplaintService;
import com.hostelmanagement.service.StudentService;
import com.hostelmanagement.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Intelligent Complaint Management Screen (Module F & AI Feature B).
 * Features rule-based AI priority scoring, explanation breakdown,
 * and controlled status lifecycle (OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED -> CLOSED).
 */
public class ComplaintManagementView {

    private final VBox root;
    private final ComplaintService complaintService = new ComplaintService();
    private final StudentService studentService = new StudentService();
    private final TableView<Complaint> table = new TableView<>();
    private final ObservableList<Complaint> complaintList = FXCollections.observableArrayList();

    public ComplaintManagementView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        User currentUser = SessionManager.getCurrentUser();
        boolean isStudent = currentUser.getRole() == UserRole.STUDENT;

        // ── TOP HEADER / ACTION BAR ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(isStudent ? "My Maintenance Complaints" : "Hostel Maintenance & Complaint Tracker");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button fileBtn = new Button("⚡ + Report Issue (AI Prioritized)");
        fileBtn.getStyleClass().add("btn-primary");
        fileBtn.setStyle("-fx-background-color: #7c3aed;");
        fileBtn.setOnAction(e -> showSubmitComplaintDialog());

        topBar.getChildren().addAll(title, spacer, fileBtn);

        // ── TABLE ──
        TableColumn<Complaint, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        idCol.setPrefWidth(50);

        if (!isStudent) {
            TableColumn<Complaint, String> sCol = new TableColumn<>("Student");
            sCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));
            sCol.setPrefWidth(140);
            table.getColumns().add(sCol);
        }

        TableColumn<Complaint, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory().name()));
        catCol.setPrefWidth(130);

        TableColumn<Complaint, String> prioCol = new TableColumn<>("Priority (AI)");
        prioCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().name() + " (" + String.format("%.0f", d.getValue().getPriorityScore()) + " pts)"));
        prioCol.setPrefWidth(130);

        TableColumn<Complaint, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        statusCol.setPrefWidth(110);

        TableColumn<Complaint, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        descCol.setPrefWidth(240);

        TableColumn<Complaint, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation() != null ? d.getValue().getLocation() : "—"));
        locCol.setPrefWidth(120);

        TableColumn<Complaint, String> dateCol = new TableColumn<>("Filed Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDateTime(d.getValue().getCreatedAt())));
        dateCol.setPrefWidth(130);

        table.getColumns().addAll(idCol, catCol, prioCol, statusCol, descCol, locCol, dateCol);
        table.setItems(complaintList);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── BOTTOM ACTIONS ──
        HBox bottomActions = new HBox(12);
        bottomActions.setAlignment(Pos.CENTER_LEFT);

        Button viewDetailsBtn = new Button("View AI Justification & History");
        viewDetailsBtn.getStyleClass().add("btn-secondary");
        viewDetailsBtn.setOnAction(e -> showDetailsDialog());

        bottomActions.getChildren().add(viewDetailsBtn);

        if (!isStudent) {
            Button progressBtn = new Button("Advance Lifecycle Status");
            progressBtn.getStyleClass().add("btn-primary");
            progressBtn.setOnAction(e -> showTransitionDialog());

            bottomActions.getChildren().add(progressBtn);
        }

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadComplaints());
        bottomActions.getChildren().add(refreshBtn);

        root.getChildren().addAll(topBar, table, bottomActions);
        loadComplaints();
    }

    private void loadComplaints() {
        complaintList.clear();
        User user = SessionManager.getCurrentUser();
        try {
            if (user.getRole() == UserRole.STUDENT) {
                Student s = studentService.getStudentByUserId(user.getId());
                if (s != null) {
                    complaintList.addAll(complaintService.getComplaintsByStudent(s.getStudentRecordId()));
                }
            } else {
                complaintList.addAll(complaintService.getAllComplaints());
            }
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load complaints", ex.getMessage());
        }
    }

    private void showSubmitComplaintDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Report Maintenance / Facility Complaint");
        dialog.setHeaderText("Smart AI will evaluate category, urgency, and impact to determine priority score.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<ComplaintCategory> catBox = new ComboBox<>(FXCollections.observableArrayList(ComplaintCategory.values()));
        catBox.setValue(ComplaintCategory.ELECTRICITY);

        TextField locField = new TextField();
        locField.setPromptText("e.g. Room A-001, Common Restroom, 1st Floor Hall");

        Spinner<Integer> urgencySpinner = new Spinner<>(1, 5, 3);
        Spinner<Integer> impactSpinner = new Spinner<>(1, 50, 1);

        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);
        descArea.setPromptText("Describe the problem clearly...");

        grid.add(new Label("Category:"), 0, 0); grid.add(catBox, 1, 0);
        grid.add(new Label("Location:"), 0, 1); grid.add(locField, 1, 1);
        grid.add(new Label("Urgency (1 = low, 5 = critical):"), 0, 2); grid.add(urgencySpinner, 1, 2);
        grid.add(new Label("Impact (students affected):"), 0, 3); grid.add(impactSpinner, 1, 3);
        grid.add(new Label("Description:"), 0, 4); grid.add(descArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    User user = SessionManager.getCurrentUser();
                    int studentId = 0;
                    if (user.getRole() == UserRole.STUDENT) {
                        Student s = studentService.getStudentByUserId(user.getId());
                        if (s != null) studentId = s.getStudentRecordId();
                    } else {
                        List<Student> students = studentService.getAllStudents();
                        if (!students.isEmpty()) studentId = students.get(0).getStudentRecordId();
                    }

                    if (studentId == 0) {
                        ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "No Student Record", "Please create a resident first.");
                        return;
                    }

                    Complaint c = complaintService.submitComplaint(
                            studentId,
                            catBox.getValue(),
                            descArea.getText(),
                            locField.getText(),
                            urgencySpinner.getValue(),
                            impactSpinner.getValue()
                    );
                    loadComplaints();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "AI Evaluation Complete",
                            "Complaint Registered with AI Priority: " + c.getPriority(),
                            c.getPriorityExplanation());
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Submission Error", "Failed to submit complaint", ex.getMessage());
                }
            }
        });
    }

    private void showDetailsDialog() {
        Complaint selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Complaint Selected", "Please select a complaint from the table.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Complaint #" + selected.getId() + " — AI Breakdown & Details");
        dialog.setHeaderText("Category: " + selected.getCategory() + " | Current Status: " + selected.getStatus());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        Label aiCardTitle = new Label("🧠 AI Priority Scoring Justification");
        aiCardTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #5b21b6;");

        Label aiExpl = new Label(selected.getPriorityExplanation() != null ? selected.getPriorityExplanation() : "Standard priority assigned.");
        aiExpl.setWrapText(true);
        aiExpl.setStyle("-fx-background-color: #f5f3ff; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #c4b5fd; -fx-border-radius: 8px;");

        Label desc = new Label("Description: " + selected.getDescription());
        desc.setWrapText(true);

        Label notes = new Label("Resolution Notes: " + (selected.getResolutionNotes() != null ? selected.getResolutionNotes() : "None yet"));
        notes.setStyle("-fx-font-style: italic; -fx-text-fill: #475569;");

        content.getChildren().addAll(aiCardTitle, aiExpl, desc, notes);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showTransitionDialog() {
        Complaint selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Complaint Selected", "Please select a complaint to update.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Lifecycle Status");
        dialog.setHeaderText("Transition complaint #" + selected.getId() + " from " + selected.getStatus() + ".");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<ComplaintStatus> statusBox = new ComboBox<>();
        // Only populate valid next states based on status state machine
        for (ComplaintStatus s : ComplaintStatus.values()) {
            if (selected.getStatus().canTransitionTo(s)) {
                statusBox.getItems().add(s);
            }
        }

        if (statusBox.getItems().isEmpty()) {
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Terminal State", "No Transitions Available",
                    "Complaint is currently in final state (" + selected.getStatus() + ").");
            return;
        }
        statusBox.setValue(statusBox.getItems().get(0));

        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(2);
        notesArea.setPromptText("Enter progress or resolution remarks...");

        grid.add(new Label("Next Status:"), 0, 0); grid.add(statusBox, 1, 0);
        grid.add(new Label("Resolution Notes:"), 0, 1); grid.add(notesArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    int staffUserId = SessionManager.getCurrentUser().getId();
                    complaintService.updateStatus(selected.getId(), statusBox.getValue(), staffUserId, notesArea.getText());
                    loadComplaints();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Status Updated",
                            "Complaint transitioned to " + statusBox.getValue());
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Transition Error", "Failed to update status", ex.getMessage());
                }
            }
        });
    }

    public VBox getView() {
        return root;
    }
}
