package com.hostelmanagement.ui;

import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.model.User;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern Application Shell.
 * Provides responsive sidebar with role-based access, top bar, and content container.
 */
public class MainLayout {

    private final BorderPane root;
    private final Label pageTitleLabel;
    private final VBox sidebarNav;
    private final StackPane contentArea;
    private final List<Button> navButtons = new ArrayList<>();
    private final AuthService authService = new AuthService();

    public MainLayout() {
        root = new BorderPane();

        // ── TOP HEADER ──
        HBox topHeader = new HBox(15);
        topHeader.getStyleClass().add("top-header");
        topHeader.setAlignment(Pos.CENTER_LEFT);

        pageTitleLabel = new Label("Dashboard");
        pageTitleLabel.getStyleClass().add("view-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        User currentUser = SessionManager.getCurrentUser();
        VBox userProfile = new VBox(2);
        userProfile.setAlignment(Pos.CENTER_RIGHT);

        Label userName = new Label(currentUser != null ? currentUser.getFullName() : "User");
        userName.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");

        Label userRole = new Label(currentUser != null ? currentUser.getRole().name() : "");
        userRole.getStyleClass().addAll("badge", getBadgeStyle(currentUser != null ? currentUser.getRole() : null));

        userProfile.getChildren().addAll(userName, userRole);

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("btn-secondary");
        logoutBtn.setOnAction(e -> {
            if (ViewNavigator.showConfirmation("Logout", "Confirm Logout", "Are you sure you want to end your session?")) {
                authService.logout();
                ViewNavigator.showLogin();
            }
        });

        topHeader.getChildren().addAll(pageTitleLabel, spacer, userProfile, logoutBtn);
        root.setTop(topHeader);

        // ── SIDEBAR ──
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        VBox brandBox = new VBox(4);
        brandBox.getStyleClass().add("sidebar-brand");
        Label brandTitle = new Label("Smart Hostel");
        brandTitle.getStyleClass().add("app-title");
        Label brandSub = new Label("CSE203 Mini Project");
        brandSub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        brandBox.getChildren().addAll(brandTitle, brandSub);

        sidebarNav = new VBox(6);
        populateNavButtons();

        sidebar.getChildren().addAll(brandBox, sidebarNav);
        root.setLeft(sidebar);

        // ── CENTER CONTENT PANE ──
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-pane");

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        root.setCenter(scrollPane);
    }

    private void populateNavButtons() {
        sidebarNav.getChildren().clear();
        navButtons.clear();

        UserRole role = SessionManager.getCurrentUser().getRole();

        // Dashboard button based on role
        if (role == UserRole.ADMIN) {
            addNavButton("📊 Dashboard", () -> ViewNavigator.setContent(new AdminDashboardView().getView(), "Administrator Dashboard"));
            addNavButton("👥 Students", () -> ViewNavigator.setContent(new StudentManagementView().getView(), "Student Records"));
            addNavButton("🏢 Hostels & Rooms", () -> ViewNavigator.setContent(new HostelManagementView().getView(), "Hostel Hierarchy & Capacity"));
            addNavButton("🛏️ Bed Allocations", () -> ViewNavigator.setContent(new AllocationView().getView(), "Smart Room Allocation (AI)"));
            addNavButton("📝 Complaints", () -> ViewNavigator.setContent(new ComplaintManagementView().getView(), "Complaint Management (AI)"));
            addNavButton("📅 Leave Requests", () -> ViewNavigator.setContent(new LeaveManagementView().getView(), "Student Leave Approvals"));
            addNavButton("📋 Attendance", () -> ViewNavigator.setContent(new AttendanceView().getView(), "Hostel Attendance"));
            addNavButton("💳 Fee Records", () -> ViewNavigator.setContent(new FeeManagementView().getView(), "Hostel Fees & Payments"));
            addNavButton("📢 Notices", () -> ViewNavigator.setContent(new NoticeBoardView().getView(), "Campus Notices"));
            addNavButton("📈 Reports & Export", () -> ViewNavigator.setContent(new ReportsView().getView(), "Reports & Analytics"));
            addNavButton("⚙️ User Accounts", () -> ViewNavigator.setContent(new UserManagementView().getView(), "User Accounts & Security"));
        } else if (role == UserRole.WARDEN) {
            addNavButton("📊 Dashboard", () -> ViewNavigator.setContent(new WardenDashboardView().getView(), "Warden Dashboard"));
            addNavButton("👥 Students", () -> ViewNavigator.setContent(new StudentManagementView().getView(), "Hostel Residents"));
            addNavButton("🛏️ Bed Allocations", () -> ViewNavigator.setContent(new AllocationView().getView(), "Smart Bed Allotment"));
            addNavButton("📝 Complaints", () -> ViewNavigator.setContent(new ComplaintManagementView().getView(), "Complaints & Maintenance"));
            addNavButton("📅 Leave Approvals", () -> ViewNavigator.setContent(new LeaveManagementView().getView(), "Leave Applications"));
            addNavButton("📋 Daily Attendance", () -> ViewNavigator.setContent(new AttendanceView().getView(), "Roll-Call Attendance"));
            addNavButton("📢 Notices", () -> ViewNavigator.setContent(new NoticeBoardView().getView(), "Hostel Announcements"));
            addNavButton("📈 Reports", () -> ViewNavigator.setContent(new ReportsView().getView(), "Hostel Operational Reports"));
        } else if (role == UserRole.STUDENT) {
            addNavButton("🏠 My Profile & Bed", () -> ViewNavigator.setContent(new StudentDashboardView().getView(), "Student Portal"));
            addNavButton("📅 Apply for Leave", () -> ViewNavigator.setContent(new LeaveManagementView().getView(), "My Leave Applications"));
            addNavButton("📝 File Complaint", () -> ViewNavigator.setContent(new ComplaintManagementView().getView(), "My Complaints (AI Supported)"));
            addNavButton("💳 My Hostel Fees", () -> ViewNavigator.setContent(new FeeManagementView().getView(), "Fee Status & Receipts"));
            addNavButton("📢 Hostel Notices", () -> ViewNavigator.setContent(new NoticeBoardView().getView(), "Notice Board"));
        }
    }

    private void addNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> {
            for (Button b : navButtons) {
                b.getStyleClass().remove("nav-button-active");
            }
            btn.getStyleClass().add("nav-button-active");
            action.run();
        });
        navButtons.add(btn);
        sidebarNav.getChildren().add(btn);

        // Highlight first button initially
        if (navButtons.size() == 1) {
            btn.getStyleClass().add("nav-button-active");
        }
    }

    public void setContent(Pane content, String title) {
        pageTitleLabel.setText(title);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    public BorderPane getRoot() {
        return root;
    }

    private String getBadgeStyle(UserRole role) {
        if (role == null) return "badge-blue";
        return switch (role) {
            case ADMIN -> "badge-purple";
            case WARDEN -> "badge-blue";
            case STUDENT -> "badge-green";
        };
    }
}
