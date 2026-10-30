package com.hostelmanagement;

import com.hostelmanagement.database.DatabaseInitializer;
import com.hostelmanagement.service.AuthService;
import com.hostelmanagement.ui.ViewNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Smart Hostel Management System — Application Main Entry Point.
 *
 * CSE203: Object-Oriented Programming Using Java Mini Project.
 * Initializes the database migrations safely and routes to Setup Wizard or Login.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ViewNavigator.setPrimaryStage(primaryStage);

        // 1. Run Automated Database Bootstrap & Migrations
        try {
            new DatabaseInitializer().initialize();
        } catch (Exception ex) {
            System.err.println("Database bootstrap warning: " + ex.getMessage());
            Platform.runLater(() -> {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Database Connection Notice",
                        "MySQL Server Connectivity",
                        "Could not connect to MySQL server or initialize smart_hostel_db.\n"
                        + "Please verify that MySQL service is running and credentials in application.properties are configured.\n\n"
                        + "Technical details: " + ex.getMessage());
            });
        }

        // 2. Check if first-run setup is required
        try {
            AuthService authService = new AuthService();
            if (!authService.hasAnyAdmin()) {
                ViewNavigator.showFirstRunSetup();
            } else {
                ViewNavigator.showLogin();
            }
        } catch (Exception ex) {
            // If checking admin failed (e.g. database not reachable yet), show login screen with helpful hints
            ViewNavigator.showLogin();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
