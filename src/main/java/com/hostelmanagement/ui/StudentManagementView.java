package com.hostelmanagement.ui;

import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.model.Student;
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
 * Student Records Management Screen (Module B).
 * Features real-time search, registration dialog, status toggles, and allocation history.
 */
public class StudentManagementView {

    private final VBox root;
    private final StudentService studentService = new StudentService();
    private final TableView<Student> table = new TableView<>();
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();

    public StudentManagementView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        // ── TOP ACTION BAR ──
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Search by name, register number, department...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadStudents(newVal));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadStudents(searchField.getText()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addStudentBtn = new Button("+ Register New Student");
        addStudentBtn.getStyleClass().add("btn-primary");
        addStudentBtn.setOnAction(e -> showRegisterDialog());

        actionBar.getChildren().addAll(searchField, refreshBtn, spacer, addStudentBtn);

        // ── TABLE CONFIGURATION ──
        TableColumn<Student, String> regCol = new TableColumn<>("Register No");
        regCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegisterNumber()));
        regCol.setPrefWidth(120);

        TableColumn<Student, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        nameCol.setPrefWidth(160);

        TableColumn<Student, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartment()));
        deptCol.setPrefWidth(180);

        TableColumn<Student, String> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(data -> new SimpleStringProperty("Year " + data.getValue().getYear()));
        yearCol.setPrefWidth(70);

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail() != null ? data.getValue().getEmail() : "—"));
        emailCol.setPrefWidth(160);

        TableColumn<Student, String> phoneCol = new TableColumn<>("Guardian Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGuardianPhone() != null ? data.getValue().getGuardianPhone() : "—"));
        phoneCol.setPrefWidth(120);

        TableColumn<Student, String> statusCol = new TableColumn<>("Account");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));
        statusCol.setPrefWidth(80);

        table.getColumns().addAll(regCol, nameCol, deptCol, yearCol, emailCol, phoneCol, statusCol);
        table.setItems(studentList);
        table.setPlaceholder(new Label("No student records found. Click '+ Register New Student' to add residents."));
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── BOTTOM ACTION BAR ──
        HBox tableActions = new HBox(12);
        tableActions.setAlignment(Pos.CENTER_LEFT);

        Button toggleStatusBtn = new Button("Toggle Active / Inactive");
        toggleStatusBtn.getStyleClass().add("btn-secondary");
        toggleStatusBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Student Selected", "Please select a student from the table.");
                return;
            }
            boolean newStatus = !selected.isActive();
            studentService.setAccountStatus(selected.getStudentRecordId(), newStatus);
            selected.setActive(newStatus);
            table.refresh();
        });

        Button historyBtn = new Button("View Allocation History");
        historyBtn.getStyleClass().add("btn-secondary");
        historyBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Student Selected", "Please select a student from the table.");
                return;
            }
            showHistoryDialog(selected);
        });

        tableActions.getChildren().addAll(toggleStatusBtn, historyBtn);

        root.getChildren().addAll(actionBar, table, tableActions);
        loadStudents("");
    }

    private void loadStudents(String query) {
        studentList.clear();
        try {
            List<Student> students = studentService.searchStudents(query);
            studentList.addAll(students);
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve student records", ex.getMessage());
        }
    }

    private void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Hostel Resident");
        dialog.setHeaderText("Add student details. A login account will be provisioned automatically.");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ViewNavigator.applyStylesheet(pane.getScene());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        passwordField.setText("Student@123");
        TextField fullNameField = new TextField();
        TextField regNoField = new TextField();
        TextField deptField = new TextField("Computer Science & Engineering");
        ComboBox<Integer> yearBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        yearBox.setValue(1);
        TextField emailField = new TextField();
        TextField guardianNameField = new TextField();
        TextField guardianPhoneField = new TextField();

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Register Number:"), 0, 1);
        grid.add(regNoField, 1, 1);
        grid.add(new Label("Department:"), 0, 2);
        grid.add(deptField, 1, 2);
        grid.add(new Label("Year of Study:"), 0, 3);
        grid.add(yearBox, 1, 3);
        grid.add(new Label("Account Username:"), 0, 4);
        grid.add(usernameField, 1, 4);
        grid.add(new Label("Initial Password:"), 0, 5);
        grid.add(passwordField, 1, 5);
        grid.add(new Label("Email:"), 0, 6);
        grid.add(emailField, 1, 6);
        grid.add(new Label("Guardian Name:"), 0, 7);
        grid.add(guardianNameField, 1, 7);
        grid.add(new Label("Guardian Phone:"), 0, 8);
        grid.add(guardianPhoneField, 1, 8);

        pane.setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    studentService.registerStudent(
                            usernameField.getText(),
                            passwordField.getText(),
                            fullNameField.getText(),
                            emailField.getText(),
                            regNoField.getText(),
                            deptField.getText(),
                            yearBox.getValue(),
                            guardianNameField.getText(),
                            guardianPhoneField.getText()
                    );
                    loadStudents(searchField.getText());
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Resident Registered",
                            "Student record and login credentials created successfully.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Registration Error", "Could not register resident", ex.getMessage());
                }
            }
        });
    }

    private void showHistoryDialog(Student s) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Allocation History — " + s.getFullName());
        dialog.setHeaderText("Previous bed and room allotments for " + s.getRegisterNumber());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<Allocation> histTable = new TableView<>();
        TableColumn<Allocation, String> bCol = new TableColumn<>("Bed");
        bCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBedNumber()));
        TableColumn<Allocation, String> rCol = new TableColumn<>("Room");
        rCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoomNumber()));
        TableColumn<Allocation, String> blkCol = new TableColumn<>("Block");
        blkCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBlockName()));
        TableColumn<Allocation, String> sCol = new TableColumn<>("Start Date");
        sCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getStartDate())));
        TableColumn<Allocation, String> eCol = new TableColumn<>("End Date");
        eCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate() != null ? DateUtil.formatDate(d.getValue().getEndDate()) : "Present"));
        TableColumn<Allocation, String> stCol = new TableColumn<>("Status");
        stCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));

        histTable.getColumns().addAll(bCol, rCol, blkCol, sCol, eCol, stCol);
        histTable.setPrefHeight(250);

        List<Allocation> history = studentService.getStudentAllocationHistory(s.getStudentRecordId());
        histTable.setItems(FXCollections.observableArrayList(history));

        dialog.getDialogPane().setContent(histTable);
        dialog.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
