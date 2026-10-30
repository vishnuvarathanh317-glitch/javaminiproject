package com.hostelmanagement.ui;

import com.hostelmanagement.enums.FeeStatus;
import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.model.FeeRecord;
import com.hostelmanagement.model.Payment;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.model.User;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.FeeService;
import com.hostelmanagement.service.StudentService;
import com.hostelmanagement.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Hostel Fee Management Screen (Module H).
 * Tracks academic fee obligations, records transactional payments, and computes balances.
 */
public class FeeManagementView {

    private final VBox root;
    private final FeeService feeService = new FeeService();
    private final StudentService studentService = new StudentService();

    private final TableView<FeeRecord> table = new TableView<>();
    private final ObservableList<FeeRecord> feeList = FXCollections.observableArrayList();
    private final Label balanceSummaryLabel = new Label();

    public FeeManagementView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        User currentUser = SessionManager.getCurrentUser();
        boolean isStudent = currentUser.getRole() == UserRole.STUDENT;

        // ── TOP SUMMARY BAR ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(isStudent ? "My Hostel Fee Obligations" : "Hostel Fee Ledger & Payment Tracking");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (!isStudent) {
            Button addFeeBtn = new Button("+ Create Fee Obligation");
            addFeeBtn.getStyleClass().add("btn-secondary");
            addFeeBtn.setOnAction(e -> showCreateFeeDialog());

            Button recordPayBtn = new Button("💳 Record Payment Entry");
            recordPayBtn.getStyleClass().add("btn-primary");
            recordPayBtn.setOnAction(e -> showRecordPaymentDialog());

            topBar.getChildren().addAll(title, spacer, addFeeBtn, recordPayBtn);
        } else {
            topBar.getChildren().addAll(title, spacer);
        }

        // ── TABLE ──
        if (!isStudent) {
            TableColumn<FeeRecord, String> regCol = new TableColumn<>("Register No");
            regCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegisterNumber()));
            regCol.setPrefWidth(120);

            TableColumn<FeeRecord, String> nameCol = new TableColumn<>("Student");
            nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));
            nameCol.setPrefWidth(160);

            table.getColumns().addAll(regCol, nameCol);
        }

        TableColumn<FeeRecord, String> descCol = new TableColumn<>("Term / Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        descCol.setPrefWidth(220);

        TableColumn<FeeRecord, String> totalCol = new TableColumn<>("Total (₹)");
        totalCol.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().getTotalAmount()));
        totalCol.setPrefWidth(110);

        TableColumn<FeeRecord, String> paidCol = new TableColumn<>("Paid (₹)");
        paidCol.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().getPaidAmount()));
        paidCol.setPrefWidth(110);

        TableColumn<FeeRecord, String> balCol = new TableColumn<>("Balance (₹)");
        balCol.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().getOutstandingAmount()));
        balCol.setPrefWidth(110);

        TableColumn<FeeRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        statusCol.setPrefWidth(120);

        TableColumn<FeeRecord, String> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDueDate() != null ? DateUtil.formatDate(d.getValue().getDueDate()) : "—"));
        dueCol.setPrefWidth(110);

        table.getColumns().addAll(descCol, totalCol, paidCol, balCol, statusCol, dueCol);
        table.setItems(feeList);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── BOTTOM ACTIONS ──
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        Button historyBtn = new Button("View Payment Receipts History");
        historyBtn.getStyleClass().add("btn-secondary");
        historyBtn.setOnAction(e -> showPaymentHistoryDialog());

        Region bSpacer = new Region();
        HBox.setHgrow(bSpacer, Priority.ALWAYS);

        balanceSummaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        bottomBar.getChildren().addAll(historyBtn, bSpacer, balanceSummaryLabel);

        root.getChildren().addAll(topBar, table, bottomBar);
        loadFees();
    }

    private void loadFees() {
        feeList.clear();
        User user = SessionManager.getCurrentUser();
        try {
            if (user.getRole() == UserRole.STUDENT) {
                Student s = studentService.getStudentByUserId(user.getId());
                if (s != null) {
                    feeList.addAll(feeService.getStudentFees(s.getStudentRecordId()));
                }
            } else {
                feeList.addAll(feeService.getAllFeeRecords());
                BigDecimal[] sum = feeService.getTotalFeeSummary();
                balanceSummaryLabel.setText(String.format("Total Billed: ₹%s | Collected: ₹%s | Balance: ₹%s",
                        sum[0], sum[1], sum[0].subtract(sum[1])));
            }
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load fee ledger", ex.getMessage());
        }
    }

    private void showCreateFeeDialog() {
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Warning", "No Residents", "Register residents first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Hostel Fee Obligation");
        dialog.setHeaderText("Issue academic fee charge for student.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<Student> sBox = new ComboBox<>(FXCollections.observableArrayList(students));
        sBox.setValue(students.get(0));

        TextField descField = new TextField("Hostel & Mess Fee — Semester 2");
        TextField amountField = new TextField("45000.00");
        DatePicker duePicker = new DatePicker(LocalDate.now().plusMonths(1));

        grid.add(new Label("Resident:"), 0, 0); grid.add(sBox, 1, 0);
        grid.add(new Label("Description / Term:"), 0, 1); grid.add(descField, 1, 1);
        grid.add(new Label("Amount (₹):"), 0, 2); grid.add(amountField, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3); grid.add(duePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    BigDecimal amount = new BigDecimal(amountField.getText().trim());
                    feeService.createFeeObligation(sBox.getValue().getStudentRecordId(), descField.getText(), amount, duePicker.getValue());
                    loadFees();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Fee Created", "Fee obligation registered.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Validation Error", "Failed to create fee", ex.getMessage());
                }
            }
        });
    }

    private void showRecordPaymentDialog() {
        FeeRecord selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Fee Selected", "Select a fee obligation from the table to record payment.");
            return;
        }

        if (selected.getStatus() == FeeStatus.PAID) {
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Fully Paid", "Notice", "This fee obligation has already been fully settled.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Payment for " + selected.getStudentName());
        dialog.setHeaderText("Outstanding Balance: ₹ " + selected.getOutstandingAmount());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        TextField amountField = new TextField(selected.getOutstandingAmount().toString());
        ComboBox<String> methodBox = new ComboBox<>(FXCollections.observableArrayList("UPI / NetBanking", "Bank Transfer (NEFT/RTGS)", "Demand Draft", "Cash Receipt"));
        methodBox.setValue("UPI / NetBanking");

        TextField refField = new TextField("TXN-" + System.currentTimeMillis() % 1000000);
        DatePicker pDatePicker = new DatePicker(LocalDate.now());

        grid.add(new Label("Payment Amount (₹):"), 0, 0); grid.add(amountField, 1, 0);
        grid.add(new Label("Payment Method:"), 0, 1); grid.add(methodBox, 1, 1);
        grid.add(new Label("Reference / Receipt No:"), 0, 2); grid.add(refField, 1, 2);
        grid.add(new Label("Payment Date:"), 0, 3); grid.add(pDatePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    BigDecimal payAmount = new BigDecimal(amountField.getText().trim());
                    int staffId = SessionManager.getCurrentUser().getId();
                    feeService.recordPayment(selected.getId(), payAmount, pDatePicker.getValue(), methodBox.getValue(), refField.getText(), staffId);
                    loadFees();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Payment Recorded",
                            "Payment of ₹" + payAmount + " recorded with reference " + refField.getText());
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Payment Error", "Could not record payment", ex.getMessage());
                }
            }
        });
    }

    private void showPaymentHistoryDialog() {
        FeeRecord selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No Fee Selected", "Select a fee record to view its payment receipts.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Payment Receipts — " + selected.getStudentName());
        dialog.setHeaderText("Receipts for " + selected.getDescription());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<Payment> pTable = new TableView<>();
        TableColumn<Payment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getPaymentDate())));

        TableColumn<Payment, String> amtCol = new TableColumn<>("Amount (₹)");
        amtCol.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().getAmount()));

        TableColumn<Payment, String> mCol = new TableColumn<>("Method");
        mCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentMethod()));

        TableColumn<Payment, String> refCol = new TableColumn<>("Reference Number");
        refCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReferenceNumber()));
        refCol.setPrefWidth(160);

        pTable.getColumns().addAll(dateCol, amtCol, mCol, refCol);
        pTable.setItems(FXCollections.observableArrayList(feeService.getPaymentsForFee(selected.getId())));
        pTable.setPrefHeight(200);

        dialog.getDialogPane().setContent(pTable);
        dialog.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
