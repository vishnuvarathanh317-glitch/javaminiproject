package com.hostelmanagement.ui;

import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.service.AuthService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * First-Run Administrator Setup Wizard (Section 5 & 14).
 * Creates the initial administrative account without hardcoding default passwords.
 */
public class FirstRunSetupView {

    private final StackPane root;
    private final AuthService authService = new AuthService();

    public FirstRunSetupView() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0f172a;");

        VBox card = new VBox(16);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(480);
        card.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Initial Administrator Setup");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Configure your hostel system master administrator account.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 500; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Chief Warden Prof. Raman");

        TextField usernameField = new TextField();
        usernameField.setPromptText("e.g. admin");

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. admin@hostel.edu");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter a strong password (min 6 characters)");

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Re-enter password to confirm");

        Button createBtn = new Button("Create Administrator & Continue");
        createBtn.getStyleClass().add("btn-primary");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        createBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            String pass = passField.getText();
            String confirm = confirmPassField.getText();

            if (pass == null || !pass.equals(confirm)) {
                errorLabel.setText("Passwords do not match.");
                errorLabel.setVisible(true);
                return;
            }

            try {
                authService.createInitialAdmin(usernameField.getText(), pass, nameField.getText(), emailField.getText());
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Setup Complete",
                        "Administrator Account Created",
                        "Master administrator account created successfully. Please log in.");
                ViewNavigator.showLogin();
            } catch (ValidationException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText("Setup error: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> ViewNavigator.showLogin());

        card.getChildren().addAll(
                title, subtitle, errorLabel,
                new Label("Full Name:"), nameField,
                new Label("Username:"), usernameField,
                new Label("Email Address:"), emailField,
                new Label("Password:"), passField,
                new Label("Confirm Password:"), confirmPassField,
                createBtn, backBtn
        );

        root.getChildren().add(card);
    }

    public StackPane getView() {
        return root;
    }
}
