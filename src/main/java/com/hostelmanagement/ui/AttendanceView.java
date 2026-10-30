package com.hostelmanagement.ui;

import com.hostelmanagement.enums.AttendanceStatus;
import com.hostelmanagement.model.AttendanceRecord;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.AttendanceService;
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

/**
 * Hostel Daily Attendance Screen (Module G).
 * Prevents duplicate daily attendance entries per student and provides roll-call recording.
 */
public class AttendanceView {

    private final VBox root;
    private final AttendanceService attendanceService = new AttendanceService();
    private final StudentService studentService = new StudentService();

    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TableView<AttendanceRecord> table = new TableView<>();
    private final ObservableList<AttendanceRecord> recordList = FXCollections.observableArrayList();

    public AttendanceView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        // ── TOP CONTROLS ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Daily Roll-Call Attendance");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label("Date:");
        datePicker.setOnAction(e -> loadAttendance());

        Button refreshBtn = new Button("View Records");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadAttendance());

        Button markBtn = new Button("+ Mark Attendance");
        markBtn.getStyleClass().add("btn-primary");
        markBtn.setOnAction(e -> showMarkAttendanceDialog());

        topBar.getChildren().addAll(title, spacer, dateLabel, datePicker, refreshBtn, markBtn);

        // ── TABLE ──
        TableColumn<AttendanceRecord, String> regCol = new TableColumn<>("Register No");
        regCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegisterNumber()));
        regCol.setPrefWidth(140);

        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Resident Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));
        nameCol.setPrefWidth(180);

        TableColumn<AttendanceRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getDate())));
        dateCol.setPrefWidth(120);

        TableColumn<AttendanceRecord, String> statusCol = new TableColumn<>("Attendance Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        statusCol.setPrefWidth(140);

        table.getColumns().addAll(regCol, nameCol, dateCol, statusCol);
        table.setItems(recordList);
        table.setPlaceholder(new Label("No attendance recorded for " + DateUtil.formatDate(datePicker.getValue()) + ". Click '+ Mark Attendance'."));
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(topBar, table);
        loadAttendance();
    }

    private void loadAttendance() {
        recordList.clear();
        LocalDate target = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
        try {
            recordList.addAll(attendanceService.getAttendanceByDate(target));
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load attendance", ex.getMessage());
        }
    }

    private void showMarkAttendanceDialog() {
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Notice", "No Students Found", "Please register students first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Daily Roll-Call Attendance");
        dialog.setHeaderText("Mark individual student attendance. Duplicate entries on the same date are rejected.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<Student> sBox = new ComboBox<>(FXCollections.observableArrayList(students));
        sBox.setValue(students.get(0));

        DatePicker dPicker = new DatePicker(datePicker.getValue());
        ComboBox<AttendanceStatus> statusBox = new ComboBox<>(FXCollections.observableArrayList(AttendanceStatus.values()));
        statusBox.setValue(AttendanceStatus.PRESENT);

        grid.add(new Label("Resident:"), 0, 0); grid.add(sBox, 1, 0);
        grid.add(new Label("Date:"), 0, 1); grid.add(dPicker, 1, 1);
        grid.add(new Label("Status:"), 0, 2); grid.add(statusBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    int staffUserId = SessionManager.getCurrentUser().getId();
                    attendanceService.recordAttendance(
                            sBox.getValue().getStudentRecordId(),
                            dPicker.getValue(),
                            statusBox.getValue(),
                            staffUserId,
                            "Manual roll call"
                    );
                    loadAttendance();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Attendance Recorded",
                            sBox.getValue().getFullName() + " marked as " + statusBox.getValue());
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Integrity Error", "Could not record attendance", ex.getMessage());
                }
            }
        });
    }

    public VBox getView() {
        return root;
    }
}
