package com.hostelmanagement.ui;

import com.hostelmanagement.enums.UserRole;
import com.hostelmanagement.model.Notice;
import com.hostelmanagement.model.User;
import com.hostelmanagement.security.SessionManager;
import com.hostelmanagement.service.NoticeService;
import com.hostelmanagement.util.DateUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Notice & Announcement Board (Module I).
 * Publishes announcements with audience targeting and expiry date management.
 */
public class NoticeBoardView {

    private final VBox root;
    private final NoticeService noticeService = new NoticeService();
    private final VBox cardsContainer = new VBox(12);

    public NoticeBoardView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        User currentUser = SessionManager.getCurrentUser();
        boolean canPublish = currentUser.getRole() != UserRole.STUDENT;

        // ── TOP BAR ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Hostel Announcements & Circulars");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (canPublish) {
            Button publishBtn = new Button("📢 + Publish New Circular");
            publishBtn.getStyleClass().add("btn-primary");
            publishBtn.setOnAction(e -> showPublishDialog());
            topBar.getChildren().addAll(title, spacer, publishBtn);
        } else {
            topBar.getChildren().addAll(title, spacer);
        }

        // ── NOTICES CONTAINER ──
        cardsContainer.setStyle("-fx-padding: 4px;");
        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(topBar, scrollPane);
        loadNotices();
    }

    private void loadNotices() {
        cardsContainer.getChildren().clear();
        User currentUser = SessionManager.getCurrentUser();
        String audience = currentUser.getRole() == UserRole.STUDENT ? "STUDENTS" : "ALL";

        try {
            List<Notice> list = noticeService.getActiveNotices(audience);
            if (list.isEmpty()) {
                Label empty = new Label("No active announcements at this time.");
                empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 20px;");
                cardsContainer.getChildren().add(empty);
                return;
            }

            for (Notice n : list) {
                VBox card = new VBox(8);
                card.getStyleClass().add("card");

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label nTitle = new Label(n.getTitle());
                nTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Label audTag = new Label("Audience: " + n.getAudience());
                audTag.getStyleClass().addAll("badge", "badge-blue");

                header.getChildren().addAll(nTitle, sp, audTag);

                Label body = new Label(n.getBody());
                body.setWrapText(true);
                body.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155; -fx-line-spacing: 3px;");

                HBox footer = new HBox(16);
                footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

                String poster = n.getPostedByName() != null ? n.getPostedByName() : "Hostel Authority";
                Label postInfo = new Label("Posted by: " + poster + " on " + DateUtil.formatDate(n.getPublishDate()));
                Label expInfo = new Label("Expires: " + (n.getExpiryDate() != null ? DateUtil.formatDate(n.getExpiryDate()) : "No Expiry"));

                footer.getChildren().addAll(postInfo, expInfo);
                card.getChildren().addAll(header, body, new Separator(), footer);
                cardsContainer.getChildren().add(card);
            }
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load notices", ex.getMessage());
        }
    }

    private void showPublishDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Publish Campus Notice");
        dialog.setHeaderText("Issue a circular to hostel residents or staff.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        TextField titleField = new TextField();
        titleField.setPromptText("Enter circular title...");

        ComboBox<String> audBox = new ComboBox<>();
        audBox.getItems().addAll("ALL", "STUDENTS", "WARDENS");
        audBox.setValue("ALL");

        DatePicker expPicker = new DatePicker(LocalDate.now().plusWeeks(2));

        TextArea bodyArea = new TextArea();
        bodyArea.setPrefRowCount(4);
        bodyArea.setPromptText("Enter complete announcement text...");

        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Target Audience:"), 0, 1); grid.add(audBox, 1, 1);
        grid.add(new Label("Expiry Date:"), 0, 2); grid.add(expPicker, 1, 2);
        grid.add(new Label("Notice Content:"), 0, 3); grid.add(bodyArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    int staffUserId = SessionManager.getCurrentUser().getId();
                    noticeService.publishNotice(titleField.getText(), bodyArea.getText(), audBox.getValue(), expPicker.getValue(), staffUserId);
                    loadNotices();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Published", "Notice Published", "Your notice is now live on the board.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Validation Error", "Could not publish notice", ex.getMessage());
                }
            }
        });
    }

    public VBox getView() {
        return root;
    }
}
