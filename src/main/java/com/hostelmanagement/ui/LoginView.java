package com.hostelmanagement.ui;

import com.hostelmanagement.exception.AuthenticationException;
import com.hostelmanagement.model.User;
import com.hostelmanagement.service.AuthService;
import com.hostelmanagement.service.SampleDataService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Modern Login Screen with credentials verification, demo data setup,
 * and first-run administrator wizard link.
 */
public class LoginView {

    private final StackPane root;
    private final AuthService authService = new AuthService();
    private final SampleDataService sampleDataService = new SampleDataService();

    public LoginView() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0f172a;");

        VBox card = new VBox(16);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Smart Hostel System");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Sign in to access your hostel account");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 500; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #334155;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #334155;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        Runnable performLogin = () -> {
            errorLabel.setVisible(false);
            try {
                authService.login(usernameField.getText(), passwordField.getText());
                ViewNavigator.showMainApp();
            } catch (AuthenticationException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText("Login error: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        };

        loginBtn.setOnAction(e -> performLogin.run());
        passwordField.setOnAction(e -> performLogin.run());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        Separator sep = new Separator();

        // Helper tools for examiner/student demo
        VBox helperBox = new VBox(8);
        helperBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label helperTitle = new Label("💡 Demo & Evaluation Shortcuts");
        helperTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1e293b;");

        Label hint1 = new Label("• Admin: (create in Setup Wizard below)");
        hint1.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label hint2 = new Label("• Warden: warden_sharma / Warden@123");
        hint2.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label hint3 = new Label("• Student: arun_kumar / Student@123");
        hint3.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        HBox demoActions = new HBox(10);
        demoActions.setAlignment(Pos.CENTER);

        Button seedDemoBtn = new Button("Load Sample College Data");
        seedDemoBtn.getStyleClass().add("btn-secondary");
        seedDemoBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6px 10px;");
        seedDemoBtn.setOnAction(e -> {
            try {
                if (sampleDataService.isSampleDataPresent()) {
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Demo Data", "Sample Data Exists",
                            "Demonstration hostel blocks, wardens, and students are already loaded in MySQL.");
                } else {
                    sampleDataService.initializeSampleDataIfEmpty();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Demo Data", "Success",
                            "Loaded sample campus hostel, blocks A & B, rooms, warden_sharma, and student accounts.");
                }
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Demo Data Error", "Failed to load sample data", ex.getMessage());
            }
        });

        Button adminSetupBtn = new Button("Admin Setup Wizard");
        adminSetupBtn.getStyleClass().add("btn-secondary");
        adminSetupBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6px 10px;");
        adminSetupBtn.setOnAction(e -> ViewNavigator.showFirstRunSetup());

        demoActions.getChildren().addAll(seedDemoBtn, adminSetupBtn);
        helperBox.getChildren().addAll(helperTitle, hint1, hint2, hint3, demoActions);

        card.getChildren().addAll(title, subtitle, errorLabel, userLabel, usernameField, passLabel, passwordField, loginBtn, sep, helperBox);
        root.getChildren().add(card);
    }

    public StackPane getView() {
        return root;
    }
}
