package com.hostelmanagement.ui;

import com.hostelmanagement.model.User;
import com.hostelmanagement.security.SessionManager;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Central View Navigator and Screen Coordinator.
 * Controls scenes, transitions, and alerts across the desktop app.
 */
public class ViewNavigator {

    private static Stage primaryStage;
    private static MainLayout mainLayout;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showLogin() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 900, 620);
        applyStylesheet(scene);
        primaryStage.setTitle("Smart Hostel Management System — Login");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showFirstRunSetup() {
        FirstRunSetupView setupView = new FirstRunSetupView();
        Scene scene = new Scene(setupView.getView(), 900, 640);
        applyStylesheet(scene);
        primaryStage.setTitle("Smart Hostel Management System — Administrator Setup");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showMainApp() {
        mainLayout = new MainLayout();
        Scene scene = new Scene(mainLayout.getRoot(), 1150, 720);
        applyStylesheet(scene);
        primaryStage.setTitle("Smart Hostel Management System — " + SessionManager.getCurrentUser().getFullName());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Default landing view depends on user role
        switch (SessionManager.getCurrentUser().getRole()) {
            case ADMIN -> setContent(new AdminDashboardView().getView(), "Administrator Dashboard");
            case WARDEN -> setContent(new WardenDashboardView().getView(), "Warden Dashboard");
            case STUDENT -> setContent(new StudentDashboardView().getView(), "Student Portal");
            default -> setContent(new AdminDashboardView().getView(), "Dashboard");
        }
    }

    public static void setContent(Pane content, String title) {
        if (mainLayout != null) {
            mainLayout.setContent(content, title);
        }
    }

    public static void applyStylesheet(Scene scene) {
        try {
            String css = ViewNavigator.class.getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Note: Stylesheet /css/style.css not found or loaded.");
        }
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (primaryStage != null && primaryStage.getScene() != null) {
            try {
                String css = ViewNavigator.class.getResource("/css/style.css").toExternalForm();
                alert.getDialogPane().getStylesheets().add(css);
            } catch (Exception ignored) {}
        }
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
