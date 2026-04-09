package com.example.testfile;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WelcomeController {

    @FXML
    private Button noticeBoardBtn;

    private void openScene(Stage stage, String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader =
                new FXMLLoader(HelloApplication.class.getResource(fxmlPath));

    Scene scene = new Scene(loader.load(), width, height);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
    }

    @FXML
    public void openLogin(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        openScene(stage, "/com/example/testfile/role.fxml", HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);
    }

    @FXML
    public void openHome(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        openScene(stage, "/com/example/testfile/welcome.fxml", HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);
    }

    @FXML
    public void openAbout(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        openScene(stage, "/com/example/testfile/about.fxml", HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);
    }

    @FXML
    public void openCourses(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        openScene(stage, "/com/example/testfile/courses.fxml", HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);
    }

    @FXML
    public void openNoticeBoard(ActionEvent event) {
        try {
            List<NoticeStore.Notice> notices = NoticeStore.getAllActiveNotices();

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UTILITY);
            popup.initModality(Modality.NONE);
            popup.setTitle("📢 Notice Board");

            Stage parentStage = (Stage) noticeBoardBtn.getScene().getWindow();
            popup.initOwner(parentStage);

            Label titleLabel = new Label("📢 Notice Board");
            titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 20px; -fx-text-fill: #d97706;");

            Label subtitleLabel = new Label("Important announcements from teachers");
            subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #717b98; -fx-font-style: italic;");

            VBox noticesBox = new VBox(10);
            noticesBox.setPadding(new Insets(10));

            if (notices.isEmpty()) {
                Label noNoticeLabel = new Label("No notices at the moment.");
                noNoticeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #717b98;");
                noticesBox.getChildren().add(noNoticeLabel);
            } else {
                for (NoticeStore.Notice notice : notices) {
                    VBox noticeCard = createNoticeCard(notice, false, null, noticesBox);
                    noticesBox.getChildren().add(noticeCard);
                }
            }

            ScrollPane scrollPane = new ScrollPane(noticesBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(350);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                    "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 8 24; -fx-background-radius: 8;");
            closeBtn.setOnAction(e -> popup.close());

            VBox content = new VBox(12, titleLabel, subtitleLabel, scrollPane, closeBtn);
            content.setPadding(new Insets(20));
            content.setAlignment(Pos.TOP_CENTER);
            content.setStyle("-fx-background-color: linear-gradient(to bottom, #fffef8, #fff8e6); -fx-background-radius: 12;");

            Scene scene = new Scene(content, 420, 480);
            popup.setScene(scene);

            double parentX = parentStage.getX();
            double parentY = parentStage.getY();
            double parentW = parentStage.getWidth();
            double parentH = parentStage.getHeight();
            popup.setX(parentX + (parentW - 420) / 2);
            popup.setY(parentY + (parentH - 480) / 2);

            popup.show();

        } catch (IOException e) {
            showAlert("Error", "Could not load notices.");
        }
    }

    private VBox createNoticeCard(NoticeStore.Notice notice, boolean isTeacher, String currentTeacher, VBox noticesBox) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 16, 12, 16));
        
        long daysRemaining = NoticeStore.getDaysRemaining(notice);
        
        if (daysRemaining <= 1) {
            card.setStyle("-fx-background-color: linear-gradient(to bottom, #fff5f5, #ffe8e8); " +
                    "-fx-background-radius: 12; -fx-border-color: #ff6b6b; -fx-border-radius: 12; -fx-border-width: 1.5;");
        } else {
            card.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #fffef5); " +
                    "-fx-background-radius: 12; -fx-border-color: #ffeeba; -fx-border-radius: 12; -fx-border-width: 1.5;");
        }

        // Header with title and days remaining
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("📌 " + notice.title());
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: #2d3748;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        String daysText = daysRemaining == 0 ? "Expires today!" : daysRemaining + " day(s) left";
        Label daysLabel = new Label(daysText);
        daysLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (daysRemaining <= 1 ? "#e74c3c" : "#717b98") + "; " +
                "-fx-padding: 2 6; -fx-background-color: " + (daysRemaining <= 1 ? "#fee2e2" : "#f0f4f8") + "; " +
                "-fx-background-radius: 8;");

        header.getChildren().addAll(titleLabel, daysLabel);

        // Message
        Label messageLabel = new Label(notice.message());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");

        // Footer with teacher info
        Label teacherLabel = new Label("— " + notice.teacher());
        teacherLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0; -fx-font-style: italic;");

        card.getChildren().addAll(header, messageLabel, teacherLabel);

        return card;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }
}