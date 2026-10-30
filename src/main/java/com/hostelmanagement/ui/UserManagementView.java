package com.hostelmanagement.ui;

import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.model.User;
import com.hostelmanagement.model.Warden;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.security.PasswordUtil;
import com.hostelmanagement.service.AuthService;
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
 * System User & Staff Account Management Screen.
 * Administrator capability to manage user roles, staff accounts, and credentials.
 */
public class UserManagementView {

    private final VBox root;
    private final UserRepository userRepository = new UserRepository();
    private final TableView<User> table = new TableView<>();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ComboBox<String> roleFilterBox = new ComboBox<>();

    public UserManagementView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        // ── TOP BAR ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("System Users & Authentication Accounts");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        roleFilterBox.getItems().addAll("ALL ROLES", "ADMIN", "WARDEN", "STUDENT");
        roleFilterBox.setValue("ALL ROLES");
        roleFilterBox.setOnAction(e -> loadUsers());

        Button addWardenBtn = new Button("+ Provision Warden Account");
        addWardenBtn.getStyleClass().add("btn-primary");
        addWardenBtn.setOnAction(e -> showAddWardenDialog());

        topBar.getChildren().addAll(title, spacer, new Label("Role:"), roleFilterBox, addWardenBtn);

        // ── TABLE ──
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setPrefWidth(50);

        TableColumn<User, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        userCol.setPrefWidth(140);

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        nameCol.setPrefWidth(180);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().name()));
        roleCol.setPrefWidth(110);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail() != null ? d.getValue().getEmail() : "—"));
        emailCol.setPrefWidth(180);

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Active" : "Inactive"));
        statusCol.setPrefWidth(90);

        TableColumn<User, String> createdCol = new TableColumn<>("Created Date");
        createdCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDateTime(d.getValue().getCreatedAt())));
        createdCol.setPrefWidth(140);

        table.getColumns().addAll(idCol, userCol, nameCol, roleCol, emailCol, statusCol, createdCol);
        table.setItems(userList);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── BOTTOM ACTIONS ──
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        Button toggleBtn = new Button("Toggle Active / Inactive");
        toggleBtn.getStyleClass().add("btn-secondary");
        toggleBtn.setOnAction(e -> handleToggleStatus());

        Button resetPassBtn = new Button("Reset Password");
        resetPassBtn.getStyleClass().add("btn-secondary");
        resetPassBtn.setOnAction(e -> handleResetPassword());

        bottomBar.getChildren().addAll(toggleBtn, resetPassBtn);

        root.getChildren().addAll(topBar, table, bottomBar);
        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        String filter = roleFilterBox.getValue();
        UserRole r = "ALL ROLES".equals(filter) ? null : UserRole.valueOf(filter);
        try {
            List<User> list = userRepository.findAll(r);
            userList.addAll(list);
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users", ex.getMessage());
        }
    }

    private void showAddWardenDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Provision Hostel Warden Account");
        dialog.setHeaderText("Create login credentials for hostel warden.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        TextField nameField = new TextField();
        TextField userField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        PasswordField passField = new PasswordField();
        passField.setText("Warden@123");

        grid.add(new Label("Full Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Username:"), 0, 1); grid.add(userField, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3); grid.add(phoneField, 1, 3);
        grid.add(new Label("Password:"), 0, 4); grid.add(passField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    Warden warden = new Warden(userField.getText().trim(), nameField.getText().trim(), emailField.getText().trim());
                    warden.setPhone(phoneField.getText().trim());
                    warden.setPasswordHash(PasswordUtil.hashPassword(passField.getText()));
                    warden.setActive(true);
                    userRepository.insert(warden);
                    loadUsers();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Warden Created", "Warden account provisioned.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not create warden", ex.getMessage());
                }
            }
        });
    }

    private void handleToggleStatus() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No User Selected", "Please select a user to update.");
            return;
        }

        try {
            selected.setActive(!selected.isActive());
            userRepository.update(selected);
            table.refresh();
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not update status", ex.getMessage());
        }
    }

    private void handleResetPassword() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection", "No User Selected", "Please select a user to reset password.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("ChangeMe@123");
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Set new password for " + selected.getUsername() + ":");
        dialog.setContentText("New Password:");

        dialog.showAndWait().ifPresent(newPass -> {
            if (newPass.length() < 6) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Password too short", "Password must be at least 6 characters.");
                return;
            }
            try {
                userRepository.updatePassword(selected.getId(), PasswordUtil.hashPassword(newPass));
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Password Reset",
                        "Password reset successfully for " + selected.getUsername());
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not reset password", ex.getMessage());
            }
        });
    }

    public VBox getView() {
        return root;
    }
}
