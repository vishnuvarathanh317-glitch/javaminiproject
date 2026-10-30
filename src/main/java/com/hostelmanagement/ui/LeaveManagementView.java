package com.hostelmanagement.ui;

import com.hostelmanagement.enums.LeaveStatus;
import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.model.LeaveRequest;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.model.User;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.LeaveService;
import com.hostelmanagement.service.StudentService;
import com.hostelmanagement.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Leave Management Interface (Module E).
 * Validates dates, manages student submissions, and processes warden approvals.
 */
public class LeaveManagementView {

    private final VBox root;
    private final LeaveService leaveService = new LeaveService();
    private final StudentService studentService = new StudentService();
    private final TableView<LeaveRequest> table = new TableView<>();
    private final ObservableList<LeaveRequest> leaveList = FXCollections.observableArrayList();

    public LeaveManagementView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        User currentUser = SessionManager.getCurrentUser();
        boolean isStudent = currentUser.getRole() == UserRole.STUDENT;

        // ── TOP HEADER / ACTION BAR ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(isStudent ? "My Leave Applications" : "Student Leave Applications & Approvals");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button applyBtn = new Button("+ Apply for Leave");
        applyBtn.getStyleClass().add("btn-primary");
        applyBtn.setOnAction(e -> showApplyLeaveDialog());

        topBar.getChildren().addAll(title, spacer, applyBtn);

        // ── TABLE ──
        if (!isStudent) {
            TableColumn<LeaveRequest, String> sCol = new TableColumn<>("Student Name");
            sCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));
            sCol.setPrefWidth(160);

            TableColumn<LeaveRequest, String> rCol = new TableColumn<>("Register No");
            rCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegisterNumber()));
            rCol.setPrefWidth(120);

            table.getColumns().addAll(sCol, rCol);
        }

        TableColumn<LeaveRequest, String> startCol = new TableColumn<>("Departure Date");
        startCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getDepartureDate())));
        startCol.setPrefWidth(120);

        TableColumn<LeaveRequest, String> endCol = new TableColumn<>("Return Date");
        endCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getReturnDate())));
        endCol.setPrefWidth(120);

        TableColumn<LeaveRequest, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));
        reasonCol.setPrefWidth(220);

        TableColumn<LeaveRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        statusCol.setPrefWidth(100);

        TableColumn<LeaveRequest, String> remarksCol = new TableColumn<>("Decision Remarks");
        remarksCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDecisionRemarks() != null ? d.getValue().getDecisionRemarks() : "—"));
        remarksCol.setPrefWidth(180);

        table.getColumns().addAll(startCol, endCol, reasonCol, statusCol, remarksCol);
        table.setItems(leaveList);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── WARDEN APPROVAL ACTIONS ──
        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        if (!isStudent) {
            Button approveBtn = new Button("✓ Approve Selected Leave");
            approveBtn.getStyleClass().add("btn-success");
            approveBtn.setOnAction(e -> handleDecideLeave(true));

            Button rejectBtn = new Button("✕ Reject Selected Leave");
            rejectBtn.getStyleClass().add("btn-danger");
            rejectBtn.setOnAction(e -> handleDecideLeave(false));

            actionsRow.getChildren().addAll(approveBtn, rejectBtn);
        }

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadLeaves());
        actionsRow.getChildren().add(refreshBtn);

        root.getChildren().addAll(topBar, table, actionsRow);
        loadLeaves();
    }

    private void loadLeaves() {
        leaveList.clear();
        User currentUser = SessionManager.getCurrentUser();
        try {
            if (currentUser.getRole() == UserRole.STUDENT) {
                Student s = studentService.getStudentByUserId(currentUser.getId());
                if (s != null) {
                    leaveList.addAll(leaveService.getLeavesByStudent(s.getStudentRecordId()));
                }
            } else {
                leaveList.addAll(leaveService.getAllLeaves());
            }
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load leave records", ex.getMessage());
        }
    }

    private void showApplyLeaveDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Submit Leave Request");
        dialog.setHeaderText("Enter departure date, return date, and genuine reason.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        DatePicker depPicker = new DatePicker(LocalDate.now().plusDays(1));
        DatePicker retPicker = new DatePicker(LocalDate.now().plusDays(3));
        TextArea reasonArea = new TextArea();
        reasonArea.setPrefRowCount(3);
        reasonArea.setPromptText("Enter purpose of hostel leave...");

        grid.add(new Label("Departure Date:"), 0, 0); grid.add(depPicker, 1, 0);
        grid.add(new Label("Expected Return Date:"), 0, 1); grid.add(retPicker, 1, 1);
        grid.add(new Label("Reason:"), 0, 2); grid.add(reasonArea, 1, 2);

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
                        ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "No Student Found", "Cannot submit leave without student profile.");
                        return;
                    }

                    leaveService.submitLeaveRequest(studentId, depPicker.getValue(), retPicker.getValue(), reasonArea.getText());
                    loadLeaves();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Leave Submitted", "Your request is pending warden approval.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Leave Request", ex.getMessage());
                }
            }
        });
    }

    private void handleDecideLeave(boolean approve) {
        LeaveRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Request Selected", "Please select a leave request from the table.");
            return;
        }

        if (selected.getStatus() != LeaveStatus.PENDING) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Notice", "Already Decided", "This request is already " + selected.getStatus());
            return;
        }

        int wardenUserId = SessionManager.getCurrentUser().getId();
        if (approve) {
            if (ViewNavigator.showConfirmation("Approve Leave", "Authorize Student Leave", "Approve leave for " + selected.getStudentName() + "?")) {
                try {
                    leaveService.approveLeave(selected.getId(), wardenUserId);
                    loadLeaves();
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve leave", ex.getMessage());
                }
            }
        } else {
            TextInputDialog reasonDialog = new TextInputDialog("Leave quota exceeded / Disapproved");
            reasonDialog.setTitle("Reject Leave");
            reasonDialog.setHeaderText("Enter reason for leave rejection:");
            reasonDialog.setContentText("Rejection Reason:");
            reasonDialog.showAndWait().ifPresent(reason -> {
                try {
                    leaveService.rejectLeave(selected.getId(), wardenUserId, reason);
                    loadLeaves();
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject leave", ex.getMessage());
                }
            });
        }
    }

    public VBox getView() {
        return root;
    }
}
