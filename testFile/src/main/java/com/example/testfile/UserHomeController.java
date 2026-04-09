package com.example.testfile;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class UserHomeController {

    @FXML
    private Label idLabel;

    @FXML
    private VBox teacherPane;

    @FXML
    private VBox studentPane;

    @FXML
    private TextField materialNameField;

    @FXML
    private Label selectedPdfLabel;

    @FXML
    private ComboBox<String> teacherClassSelector;

    @FXML
    private ComboBox<String> classSelector;

    @FXML
    private ComboBox<String> courseSelector;

    @FXML
    private ComboBox<String> chapterSelector;

    @FXML
    private ComboBox<String> quickUploadClassSelector;

    @FXML
    private Label chapterPdfLabel;

    @FXML
    private Button chatBtn;

    @FXML
    private Label unreadBadge;

    @FXML
    private Button noticeIconBtn;

    @FXML
    private VBox studentNoticePreview;

    @FXML
    private VBox noticePreviewList;

    private final Map<String, Path> teacherItemToFile = new HashMap<>();
    private final Map<String, Path> studentItemToFile = new HashMap<>();
    private final Map<String, String> itemToTeacher = new HashMap<>();

    // Course and chapter data
    private final Map<String, String[]> courseChapters = new HashMap<>();

    private String username;
    private String userRole;
    private Path selectedPdfPath;
    private Path chapterPdfPath;

    private void initializeCourseData() {
        courseChapters.put("cls9-10", new String[] {
                "Bangla",
                "English",
                "Math",
                "BGS",
                "H.Math",
                "ICT",
                "Biology",
                "Physics",
                "Chemistry"
        });
        courseChapters.put("cls11-12", new String[] {
                "Bangla",
                "English",
                "Math",
                "BGS",
                "H.Math",
                "ICT",
                "Biology",
                "Physics",
                "Chemistry"
        });
        courseChapters.put("Admission", new String[] {
                "Bangla",
                "English",
                "Math",
                "ICT",
                "Biology",
                "Physics",
                "Chemistry"
        });
        courseChapters.put("BSC", new String[] {
                "CSE 106",
                "CSE 108",
                "CSE 101",
                "CSE 102",
                "EEE 164",
                "Math 143"
        });
        courseChapters.put("Masters", new String[] {
                "Research Methodology",
                "Advanced Statistics",
                "Thesis Writing",
                "Advanced Physics",
                "Advanced Chemistry",
                "Advanced Mathematics",
                "Specialized Elective 1",
                "Specialized Elective 2",
                "Seminar & Presentation",
                "Dissertation"
        });
        courseChapters.put("BCS", new String[] {
                "Bangladesh Affairs",
                "International Affairs",
                "General Science & Technology",
                "Mathematical Reasoning",
                "Mental Ability",
                "Bangla Language & Literature",
                "English Language & Literature",
                "Geography & Environment",
                "Ethics & Good Governance"
        });
        courseChapters.put("BBA", new String[] {
                "Principles of Management",
                "Financial Accounting",
                "Marketing Management",
                "Business Statistics",
                "Microeconomics",
                "Macroeconomics",
                "Business Law",
                "Human Resource Management"
        });
    }

    public void setUserInfo(String role, String username) {
        this.username = username;
        this.userRole = role;
        initializeCourseData();

        String roleText = "student".equalsIgnoreCase(role) ? "Student" : "Teacher";
        idLabel.setText(roleText + " ID: " + username);

        boolean isTeacher = "teacher".equalsIgnoreCase(role);
        teacherPane.setVisible(isTeacher);
        teacherPane.setManaged(isTeacher);

        studentPane.setVisible(!isTeacher);
        studentPane.setManaged(!isTeacher);

        if (isTeacher) {
            refreshTeacherClassOptions();
            selectedPdfLabel.setText("No PDF selected");
            if (chapterPdfLabel != null) {
                chapterPdfLabel.setText("No file");
            }
            initializeCourseSelector();
            initializeQuickUploadSelector();

            // Notice icon is now always visible for teachers in the Quick Actions section
            if (noticeIconBtn != null) {
                noticeIconBtn.setVisible(true);
                noticeIconBtn.setManaged(true);
            }

            // Check for expiring notices
            checkExpiringNotices();
        } else {
            refreshClassOptions();

            // Hide notice icon for students (they use the notice board instead)
            if (noticeIconBtn != null) {
                noticeIconBtn.setVisible(false);
                noticeIconBtn.setManaged(false);
            }

            // Load notice preview for students
            loadStudentNoticePreview();
        }

        // Check for unread messages and update badge
        checkUnreadMessages();
    }

    private void checkExpiringNotices() {
        try {
            java.util.List<NoticeStore.Notice> expiringNotices = NoticeStore.getExpiringNotices(username);
            if (!expiringNotices.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("The following notices will expire tomorrow:\n\n");
                for (NoticeStore.Notice notice : expiringNotices) {
                    sb.append("• ").append(notice.title()).append("\n");
                }
                sb.append("\nGo to Notice Board to extend or remove them.");

                Platform.runLater(() -> {
                    show("⚠️ Notices Expiring Soon", sb.toString());
                });
            }
        } catch (IOException e) {
            // Silently handle
        }
    }

    private void checkUnreadMessages() {
        try {
            boolean isTeacher = "teacher".equalsIgnoreCase(userRole);
            int unreadCount = MessageStore.getUnreadCount(username, isTeacher);

            if (unreadBadge != null) {
                if (unreadCount > 0) {
                    unreadBadge.setText(String.valueOf(unreadCount));
                    unreadBadge.setVisible(true);
                    unreadBadge.setManaged(true);

                    // Show notification alert
                    Platform.runLater(() -> {
                        show("📬 New Messages",
                                "You have " + unreadCount + " unread message(s). Click the chat icon to view.");
                    });
                } else {
                    unreadBadge.setVisible(false);
                    unreadBadge.setManaged(false);
                }
            }
        } catch (IOException e) {
            // Silently handle - badge just won't show
        }
    }

    private void loadStudentNoticePreview() {
        if (noticePreviewList == null)
            return;

        noticePreviewList.getChildren().clear();

        try {
            List<NoticeStore.Notice> activeNotices = NoticeStore.getAllActiveNotices();

            if (activeNotices.isEmpty()) {
                Label noNotices = new Label("No notices at this time");
                noNotices.setStyle("-fx-font-size: 11px; -fx-text-fill: #717b98; -fx-font-style: italic;");
                noticePreviewList.getChildren().add(noNotices);
            } else {
                // Show up to 3 latest notices as preview
                int count = Math.min(3, activeNotices.size());
                for (int i = 0; i < count; i++) {
                    NoticeStore.Notice notice = activeNotices.get(i);
                    HBox noticeItem = createNoticePreviewItem(notice);
                    noticePreviewList.getChildren().add(noticeItem);
                }

                if (activeNotices.size() > 3) {
                    Label moreLabel = new Label("+" + (activeNotices.size() - 3) + " more notices...");
                    moreLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #1e63dc; -fx-font-style: italic;");
                    noticePreviewList.getChildren().add(moreLabel);
                }
            }
        } catch (IOException e) {
            Label errorLabel = new Label("Could not load notices");
            errorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            noticePreviewList.getChildren().add(errorLabel);
        }
    }

    private HBox createNoticePreviewItem(NoticeStore.Notice notice) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 6; -fx-padding: 8 10; " +
                "-fx-border-color: #e4ebf7; -fx-border-radius: 6;");

        Label icon = new Label("📌");
        icon.setStyle("-fx-font-size: 12px;");

        VBox info = new VBox(2);
        Label titleLabel = new Label(notice.title());
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 11px; -fx-text-fill: #2d3748;");
        titleLabel.setMaxWidth(200);
        titleLabel.setEllipsisString("...");

        Label teacherLabel = new Label("by " + notice.teacher());
        teacherLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #717b98;");

        info.getChildren().addAll(titleLabel, teacherLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        item.getChildren().addAll(icon, info);
        return item;
    }

    @FXML
    public void openStudentNoticeBoard() {
        Stage noticeBoard = new Stage();
        noticeBoard.initStyle(StageStyle.UTILITY);
        noticeBoard.initModality(Modality.NONE);
        noticeBoard.setTitle("📢 Notice Board");

        Stage parentStage = (Stage) idLabel.getScene().getWindow();
        noticeBoard.initOwner(parentStage);

        VBox mainContainer = new VBox(16);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f0f5ff);");

        Label titleLabel = new Label("📢 Notice Board");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 22px; -fx-text-fill: #1e4d8c;");

        Label subtitleLabel = new Label("Stay updated with announcements from your teachers");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5d708f;");

        VBox noticesList = new VBox(10);
        noticesList.setPadding(new Insets(10));

        ScrollPane noticesScroll = new ScrollPane(noticesList);
        noticesScroll.setFitToWidth(true);
        noticesScroll.setPrefHeight(380);
        noticesScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Load all notices
        try {
            List<NoticeStore.Notice> activeNotices = NoticeStore.getAllActiveNotices();

            if (activeNotices.isEmpty()) {
                Label emptyLabel = new Label("📭 No notices at this time");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8898aa; -fx-font-style: italic;");
                noticesList.getChildren().add(emptyLabel);
            } else {
                for (NoticeStore.Notice notice : activeNotices) {
                    VBox noticeCard = createStudentNoticeCard(notice);
                    noticesList.getChildren().add(noticeCard);
                }
            }
        } catch (IOException e) {
            Label errorLabel = new Label("Could not load notices");
            errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c;");
            noticesList.getChildren().add(errorLabel);
        }

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: linear-gradient(to right, #6c5ce7, #5b4cdb); " +
                "-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            noticesList.getChildren().clear();
            try {
                List<NoticeStore.Notice> activeNotices = NoticeStore.getAllActiveNotices();
                if (activeNotices.isEmpty()) {
                    Label emptyLabel = new Label("📭 No notices at this time");
                    emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8898aa; -fx-font-style: italic;");
                    noticesList.getChildren().add(emptyLabel);
                } else {
                    for (NoticeStore.Notice notice : activeNotices) {
                        VBox noticeCard = createStudentNoticeCard(notice);
                        noticesList.getChildren().add(noticeCard);
                    }
                }
                loadStudentNoticePreview(); // Also refresh the preview
            } catch (IOException ex) {
                Label errorLabel = new Label("Could not load notices");
                errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c;");
                noticesList.getChildren().add(errorLabel);
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 8 20; -fx-background-radius: 8;");
        closeBtn.setOnAction(e -> noticeBoard.close());

        HBox buttonBox = new HBox(12, refreshBtn, closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, noticesScroll, buttonBox);

        Scene scene = new Scene(mainContainer, 480, 540);
        noticeBoard.setScene(scene);

        double parentX = parentStage.getX();
        double parentY = parentStage.getY();
        double parentW = parentStage.getWidth();
        double parentH = parentStage.getHeight();
        noticeBoard.setX(parentX + (parentW - 480) / 2);
        noticeBoard.setY(parentY + (parentH - 540) / 2);

        noticeBoard.show();
    }

    private VBox createStudentNoticeCard(NoticeStore.Notice notice) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; " +
                "-fx-border-color: #e4ebf7; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // Title row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("📌");
        icon.setStyle("-fx-font-size: 16px;");

        Label titleLabel = new Label(notice.title());
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 15px; -fx-text-fill: #1e4d8c;");
        titleLabel.setWrapText(true);

        titleRow.getChildren().addAll(icon, titleLabel);

        // Message
        Label messageLabel = new Label(notice.message());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568; -fx-line-spacing: 2;");

        // Footer info
        HBox footerRow = new HBox(16);
        footerRow.setAlignment(Pos.CENTER_LEFT);
        footerRow.setStyle("-fx-padding: 6 0 0 0;");

        Label teacherLabel = new Label("👨‍🏫 " + notice.teacher());
        teacherLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e63dc;");

        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), notice.expiryDate());
        String expiryText = daysLeft <= 1 ? "⚠️ Expires soon" : "📅 " + daysLeft + " days left";
        Label expiryLabel = new Label(expiryText);
        expiryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (daysLeft <= 1 ? "#e74c3c" : "#717b98") + ";");

        Label dateLabel = new Label("📆 " + notice.createdDate().toString());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");

        footerRow.getChildren().addAll(teacherLabel, expiryLabel, dateLabel);

        card.getChildren().addAll(titleRow, messageLabel, footerRow);
        return card;
    }

    private void initializeCourseSelector() {
        if (courseSelector != null) {
            courseSelector.getItems().setAll(courseChapters.keySet());
            courseSelector.setOnAction(e -> {
                String selectedCourse = courseSelector.getSelectionModel().getSelectedItem();
                if (selectedCourse != null && chapterSelector != null) {
                    String[] chapters = courseChapters.get(selectedCourse);
                    if (chapters != null) {
                        chapterSelector.getItems().setAll(chapters);
                        chapterSelector.getSelectionModel().clearSelection();
                    }
                }
            });
        }
    }

    private void initializeQuickUploadSelector() {
        if (quickUploadClassSelector != null) {
            quickUploadClassSelector.getItems().setAll(courseChapters.keySet());
        }
    }

    @FXML
    public void viewCourseChapters() {
        String selectedCourse = courseSelector != null ? courseSelector.getSelectionModel().getSelectedItem() : null;
        if (selectedCourse == null) {
            show("Select Course", "Please select a course first.");
            return;
        }

        try {
            List<MaterialStore.MaterialRecord> allRecords = MaterialStore.listAll();
            teacherItemToFile.clear();
            itemToTeacher.clear();

            ListView<String> listView = new ListView<>();
            listView.setPrefHeight(320);
            listView.setPrefWidth(480);

            String[] subjects = courseChapters.get(selectedCourse);
            int count = 0;

            for (String subject : subjects) {
                String classKey = selectedCourse + " - " + subject;
                boolean hasUploads = false;

                for (MaterialStore.MaterialRecord record : allRecords) {
                    if (record.className().equalsIgnoreCase(classKey)) {
                        String uploaderTag = record.teacher().equalsIgnoreCase(username) ? " (You)" : "";
                        String row = "📄 " + record.materialName() + "  |  📖 " + subject + "  |  👨‍🏫 "
                                + record.teacher() + uploaderTag;
                        listView.getItems().add(row);
                        teacherItemToFile.put(row, record.filePath());
                        itemToTeacher.put(row, record.teacher());
                        hasUploads = true;
                        count++;
                    }
                }

                if (!hasUploads) {
                    String emptyRow = "📁 " + subject + "  |  No PDFs uploaded yet";
                    listView.getItems().add(emptyRow);
                }
            }

            showPdfPopup(selectedCourse + " - Subject Materials", listView, teacherItemToFile, true);

        } catch (IOException e) {
            show("Load Error", "Could not load course materials.");
        }
    }

    @FXML
    public void choosePdfForChapter() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF for Chapter");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = (Stage) idLabel.getScene().getWindow();
        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            chapterPdfPath = selected.toPath();
            if (chapterPdfLabel != null) {
                chapterPdfLabel.setText(selected.getName());
            }
        }
    }

    @FXML
    public void uploadToChapter() {
        String selectedCourse = courseSelector != null ? courseSelector.getSelectionModel().getSelectedItem() : null;
        String selectedSubject = chapterSelector != null ? chapterSelector.getSelectionModel().getSelectedItem() : null;

        if (selectedCourse == null) {
            show("Select Course", "Please select a course first.");
            return;
        }

        if (selectedSubject == null) {
            show("Select Subject", "Please select a subject to upload to.");
            return;
        }

        if (chapterPdfPath == null || !Files.exists(chapterPdfPath)) {
            show("Select PDF", "Please choose a PDF file first using the 'PDF' button.");
            return;
        }

        String className = selectedCourse + " - " + selectedSubject;
        String materialName = chapterPdfPath.getFileName().toString().replace(".pdf", "");

        try {
            MaterialStore.saveMaterial(username, className, materialName, chapterPdfPath);
            show("Upload Successful", "PDF uploaded to " + selectedSubject + " in " + selectedCourse);

            chapterPdfPath = null;
            if (chapterPdfLabel != null) {
                chapterPdfLabel.setText("No file");
            }

            refreshTeacherClassOptions();
            refreshClassOptions();
        } catch (IOException e) {
            show("Upload Failed", "Failed to upload PDF. Please try again.");
        }
    }

    @FXML
    public void choosePdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = (Stage) idLabel.getScene().getWindow();
        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            selectedPdfPath = selected.toPath();
            selectedPdfLabel.setText(selected.getName());
        }
    }

    @FXML
    public void uploadPdf() {
        String className = quickUploadClassSelector != null
                ? quickUploadClassSelector.getSelectionModel().getSelectedItem()
                : null;
        String materialName = materialNameField.getText() == null ? "" : materialNameField.getText().trim();

        if (className == null || className.isBlank()) {
            show("Select Section", "Please select a section (cls9-10, cls11-12, or Admission).");
            return;
        }

        if (materialName.isBlank()) {
            show("Upload Instructions", "Please provide a PDF display name.");
            return;
        }

        if (selectedPdfPath == null || !Files.exists(selectedPdfPath)) {
            show("Upload Instructions", "Please choose a valid PDF file first.");
            return;
        }

        try {
            MaterialStore.saveMaterial(username, className, materialName, selectedPdfPath);
            show("Upload Successful", "PDF uploaded successfully to " + className + ".");

            materialNameField.clear();
            quickUploadClassSelector.getSelectionModel().clearSelection();
            selectedPdfPath = null;
            selectedPdfLabel.setText("No PDF selected");

            refreshTeacherClassOptions();
            refreshClassOptions();
        } catch (IOException e) {
            show("Upload Failed", "Failed to upload PDF. Please try again.");
        }
    }

    @FXML
    public void showTeacherClassMaterials() {
        String selectedClass = teacherClassSelector.getSelectionModel().getSelectedItem();
        if (selectedClass == null || selectedClass.isBlank()) {
            show("Class Selection", "Please select a class first.");
            return;
        }

        try {
            List<MaterialStore.MaterialRecord> allRecords = MaterialStore.listAll();
            teacherItemToFile.clear();
            itemToTeacher.clear();

            ListView<String> listView = new ListView<>();
            listView.setPrefHeight(280);
            listView.setPrefWidth(420);

            int count = 0;
            for (MaterialStore.MaterialRecord record : allRecords) {
                if (record.className().equalsIgnoreCase(selectedClass)) {
                    String uploaderTag = record.teacher().equalsIgnoreCase(username) ? " (You)" : "";
                    String row = "📄 " + record.materialName() + "  |  👨‍🏫 " + record.teacher() + uploaderTag;
                    listView.getItems().add(row);
                    teacherItemToFile.put(row, record.filePath());
                    itemToTeacher.put(row, record.teacher());
                    count++;
                }
            }

            if (count == 0) {
                show("No Materials", "No PDFs found for class: " + selectedClass);
                return;
            }

            showPdfPopup("All PDFs - " + selectedClass, listView, teacherItemToFile, true);

        } catch (IOException e) {
            show("Load Error", "Could not load class materials.");
        }
    }

    @FXML
    public void showAllTeacherUploads() {
        try {
            List<MaterialStore.MaterialRecord> records = MaterialStore.listAll();
            teacherItemToFile.clear();
            itemToTeacher.clear();

            ListView<String> listView = new ListView<>();
            listView.setPrefHeight(320);
            listView.setPrefWidth(450);

            for (MaterialStore.MaterialRecord record : records) {
                String uploaderTag = record.teacher().equalsIgnoreCase(username) ? " (You)" : "";
                String row = "📚 " + record.className() + "  |  📄 " + record.materialName() + "  |  👨‍🏫 "
                        + record.teacher() + uploaderTag;
                listView.getItems().add(row);
                teacherItemToFile.put(row, record.filePath());
                itemToTeacher.put(row, record.teacher());
            }

            if (records.isEmpty()) {
                show("No Materials", "No PDFs uploaded yet.");
                return;
            }

            showPdfPopup("All Uploaded PDFs", listView, teacherItemToFile, true);

        } catch (IOException e) {
            show("Load Error", "Could not load uploaded PDFs.");
        }
    }

    @FXML
    public void showClassMaterials() {
        String selectedClass = classSelector.getSelectionModel().getSelectedItem();
        if (selectedClass == null || selectedClass.isBlank()) {
            show("Class Selection", "Please select a class first.");
            return;
        }

        try {
            List<MaterialStore.MaterialRecord> records = MaterialStore.listByClass(selectedClass);
            studentItemToFile.clear();
            itemToTeacher.clear();

            ListView<String> listView = new ListView<>();
            listView.setPrefHeight(300);
            listView.setPrefWidth(430);

            for (MaterialStore.MaterialRecord record : records) {
                String row = "📄 " + record.materialName() + "  |  👨‍🏫 " + record.teacher();
                listView.getItems().add(row);
                studentItemToFile.put(row, record.filePath());
                itemToTeacher.put(row, record.teacher());
            }

            if (records.isEmpty()) {
                show("No Materials", "No PDFs found for class: " + selectedClass);
                return;
            }

            showPdfPopup("Materials for " + selectedClass, listView, studentItemToFile, false);

        } catch (IOException e) {
            show("Load Error", "Could not load class materials.");
        }
    }

    private void showPdfPopup(String title, ListView<String> listView, Map<String, Path> itemToFile) {
        showPdfPopup(title, listView, itemToFile, false);
    }

    private void showPdfPopup(String title, ListView<String> listView, Map<String, Path> itemToFile,
            boolean isTeacherView) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.UTILITY);
        popup.initModality(Modality.NONE);
        popup.setTitle(title);

        Stage parentStage = (Stage) idLabel.getScene().getWindow();
        popup.initOwner(parentStage);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 22px; -fx-text-fill: #1e63dc;");

        Label hintLabel = new Label("Double-click to open • Select for actions");
        hintLabel.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-text-fill: #717b98; -fx-font-style: italic;");

        // Store original items for filtering
        List<String> allItems = new java.util.ArrayList<>(listView.getItems());
        allItems.sort(String.CASE_INSENSITIVE_ORDER);
        listView.getItems().setAll(allItems);

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search PDFs by name...");
        searchField.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #dce5f3; -fx-border-width: 1.5; -fx-padding: 8 12; -fx-font-size: 13px;");
        searchField.setPrefWidth(350);

        // Reload button
        Button reloadBtn = new Button("🔄");
        reloadBtn.setStyle("-fx-background-color: linear-gradient(to right, #6c5ce7, #5b4cdb); " +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 12; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        reloadBtn.setOnAction(e -> {
            try {
                List<MaterialStore.MaterialRecord> records = isTeacherView ? MaterialStore.listAll()
                        : MaterialStore.listAll();
                listView.getItems().clear();
                itemToFile.clear();
                itemToTeacher.clear();
                allItems.clear();

                String selectedClass = isTeacherView ? (teacherClassSelector.getSelectionModel().getSelectedItem())
                        : (classSelector.getSelectionModel().getSelectedItem());

                for (MaterialStore.MaterialRecord record : records) {
                    boolean include = true;
                    if (selectedClass != null && !selectedClass.isBlank() && !title.contains("All Uploaded")) {
                        include = record.className().equalsIgnoreCase(selectedClass);
                    }

                    if (include) {
                        String row;
                        if (title.contains("All Uploaded")) {
                            String uploaderTag = record.teacher().equalsIgnoreCase(username) ? " (You)" : "";
                            row = "📚 " + record.className() + "  |  📄 " + record.materialName() + "  |  👨‍🏫 "
                                    + record.teacher() + uploaderTag;
                        } else if (isTeacherView) {
                            String uploaderTag = record.teacher().equalsIgnoreCase(username) ? " (You)" : "";
                            row = "📄 " + record.materialName() + "  |  👨‍🏫 " + record.teacher() + uploaderTag;
                        } else {
                            row = "📄 " + record.materialName() + "  |  👨‍🏫 " + record.teacher();
                        }
                        allItems.add(row);
                        itemToFile.put(row, record.filePath());
                        itemToTeacher.put(row, record.teacher());
                    }
                }

                allItems.sort(String.CASE_INSENSITIVE_ORDER);
                listView.getItems().setAll(allItems);
                searchField.clear();
                show("Reloaded", "PDF list refreshed successfully.");
            } catch (IOException ex) {
                show("Reload Error", "Could not reload PDFs.");
            }
        });

        // Search functionality with auto-filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                listView.getItems().setAll(allItems);
            } else {
                String search = newVal.toLowerCase();
                List<String> filtered = new java.util.ArrayList<>();
                for (String item : allItems) {
                    if (item.toLowerCase().contains(search)) {
                        filtered.add(item);
                    }
                }
                filtered.sort(String.CASE_INSENSITIVE_ORDER);
                listView.getItems().setAll(filtered);
            }
        });

        HBox searchBox = new HBox(10, searchField, reloadBtn);
        searchBox.setAlignment(Pos.CENTER);

        listView.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #e4ebf7; -fx-border-width: 1.5; -fx-padding: 6;");

        Button openBtn = new Button("📖 Open");
        openBtn.setStyle("-fx-background-color: linear-gradient(to right, #2369df, #1f63d8); " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 10 20; "
                +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(35, 105, 223, 0.3), 8, 0.1, 0, 2);");

        Button downloadBtn = new Button("⬇ Download");
        downloadBtn.setStyle("-fx-background-color: linear-gradient(to right, #35c887, #2db87a); " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 10 20; "
                +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(45, 184, 122, 0.3), 8, 0.1, 0, 2);");

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-background-color: linear-gradient(to right, #e74c3c, #c0392b); " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 10 20; "
                +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.3), 8, 0.1, 0, 2);");
        deleteBtn.setVisible(isTeacherView);
        deleteBtn.setManaged(isTeacherView);

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; "
                +
                "-fx-border-color: #dce5f3; -fx-border-radius: 8; -fx-border-width: 1;");

        openBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Path file = itemToFile.get(selected);
                openPdf(file);
            } else {
                show("Select PDF", "Please select a PDF from the list first.");
            }
        });

        downloadBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Path file = itemToFile.get(selected);
                downloadPdf(file, popup);
            } else {
                show("Select PDF", "Please select a PDF to download.");
            }
        });

        deleteBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Path file = itemToFile.get(selected);
                String uploader = itemToTeacher.get(selected);

                if (uploader != null && uploader.equalsIgnoreCase(username)) {
                    try {
                        boolean deleted = MaterialStore.deleteMaterial(username, file);
                        if (deleted) {
                            listView.getItems().remove(selected);
                            allItems.remove(selected);
                            itemToFile.remove(selected);
                            itemToTeacher.remove(selected);
                            show("Deleted", "PDF deleted successfully.");
                            refreshTeacherClassOptions();
                            refreshClassOptions();
                        } else {
                            show("Delete Failed", "Could not delete the PDF.");
                        }
                    } catch (IOException ex) {
                        show("Delete Error", "Error deleting PDF: " + ex.getMessage());
                    }
                } else {
                    show("Cannot Delete", "You can only delete your own uploads.");
                }
            } else {
                show("Select PDF", "Please select a PDF to delete.");
            }
        });

        closeBtn.setOnAction(e -> popup.close());

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Path file = itemToFile.get(selected);
                    openPdf(file);
                }
            }
        });

        HBox buttonBox = new HBox(12, openBtn, downloadBtn);
        if (isTeacherView) {
            buttonBox.getChildren().add(deleteBtn);
        }
        buttonBox.getChildren().add(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(14, titleLabel, hintLabel, searchBox, listView, buttonBox);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8faff); -fx-background-radius: 16;");

        Scene scene = new Scene(content);
        popup.setScene(scene);
        popup.setWidth(520);
        popup.setHeight(520);

        double parentX = parentStage.getX();
        double parentY = parentStage.getY();
        double parentW = parentStage.getWidth();
        double parentH = parentStage.getHeight();
        popup.setX(parentX + (parentW - 520) / 2);
        popup.setY(parentY + (parentH - 520) / 2);

        popup.show();
    }

    private void downloadPdf(Path file, Stage popup) {
        if (file == null || !Files.exists(file)) {
            show("Download Failed", "Selected file is not available.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save PDF");
        chooser.setInitialFileName(file.getFileName().toString());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File destination = chooser.showSaveDialog(popup);
        if (destination != null) {
            try {
                Files.copy(file, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                show("Download Complete", "PDF saved to: " + destination.getName());
            } catch (IOException e) {
                show("Download Failed", "Could not save PDF: " + e.getMessage());
            }
        }
    }

    private void refreshTeacherClassOptions() {
        if (teacherClassSelector == null) {
            return;
        }
        try {
            List<String> classes = MaterialStore.listClassNames();
            String previous = teacherClassSelector.getSelectionModel().getSelectedItem();

            teacherClassSelector.getItems().setAll(classes);
            if (previous != null && classes.contains(previous)) {
                teacherClassSelector.getSelectionModel().select(previous);
            }
        } catch (IOException e) {
            show("Load Error", "Could not load class options.");
        }
    }

    private void refreshClassOptions() {
        if (classSelector == null) {
            return;
        }
        try {
            List<String> classes = MaterialStore.listClassNames();
            String previous = classSelector.getSelectionModel().getSelectedItem();

            classSelector.getItems().setAll(classes);
            if (previous != null && classes.contains(previous)) {
                classSelector.getSelectionModel().select(previous);
            }
        } catch (IOException e) {
            show("Load Error", "Could not load class options.");
        }
    }

    private void openPdf(Path file) {
        if (file == null || !Files.exists(file)) {
            show("Open Failed", "Selected file is not available.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file.toFile());
                return;
            }

            new ProcessBuilder("cmd", "/c", "start", "", file.toAbsolutePath().toString()).start();
        } catch (IOException e) {
            show("Open Failed", "Could not open selected PDF window. Ensure a PDF viewer is installed.");
        }
    }

    private void show(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/example/testfile/login.fxml"));

        Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    private void openScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));

        Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void openHome(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/welcome.fxml");
    }

    @FXML
    public void openCourses(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/courses.fxml");
    }

    @FXML
    public void openAbout(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/about.fxml");
    }

    @FXML
    public void openLogin(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/role.fxml");
    }

    @FXML
    public void openChat(ActionEvent event) {
        boolean isTeacher = "teacher".equalsIgnoreCase(userRole);

        if (isTeacher) {
            showTeacherChatList();
        } else {
            showStudentChatList();
        }
    }

    private void showStudentChatList() {
        try {
            List<String> teachers = MessageStore.getTeacherList();

            if (teachers.isEmpty()) {
                show("No Teachers", "No teachers available to chat with.");
                return;
            }

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UTILITY);
            popup.initModality(Modality.NONE);
            popup.setTitle("💬 Chat with Teachers");

            Stage parentStage = (Stage) idLabel.getScene().getWindow();
            popup.initOwner(parentStage);

            Label titleLabel = new Label("💬 Select a Teacher to Chat");
            titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 18px; -fx-text-fill: #1e63dc;");

            VBox teacherListBox = new VBox(8);
            teacherListBox.setPadding(new Insets(10));

            for (String teacher : teachers) {
                HBox teacherItem = createChatListItem(teacher, popup, false);
                teacherListBox.getChildren().add(teacherItem);
            }

            ScrollPane scrollPane = new ScrollPane(teacherListBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                    "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 8 20; -fx-background-radius: 8;");
            closeBtn.setOnAction(e -> popup.close());

            VBox content = new VBox(14, titleLabel, scrollPane, closeBtn);
            content.setPadding(new Insets(20));
            content.setAlignment(Pos.TOP_CENTER);
            content.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8faff); -fx-background-radius: 12;");

            Scene scene = new Scene(content, 350, 420);
            popup.setScene(scene);

            double parentX = parentStage.getX();
            double parentY = parentStage.getY();
            double parentW = parentStage.getWidth();
            double parentH = parentStage.getHeight();
            popup.setX(parentX + (parentW - 350) / 2);
            popup.setY(parentY + (parentH - 420) / 2);

            popup.show();

        } catch (IOException e) {
            show("Error", "Could not load teacher list.");
        }
    }

    private void showTeacherChatList() {
        try {
            List<String> students = MessageStore.getStudentsWhoMessaged(username);

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UTILITY);
            popup.initModality(Modality.NONE);
            popup.setTitle("💬 Student Messages");

            Stage parentStage = (Stage) idLabel.getScene().getWindow();
            popup.initOwner(parentStage);

            Label titleLabel = new Label("💬 Student Conversations");
            titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 18px; -fx-text-fill: #1e63dc;");

            VBox studentListBox = new VBox(8);
            studentListBox.setPadding(new Insets(10));

            if (students.isEmpty()) {
                Label noMsgLabel = new Label("No students have messaged you yet.");
                noMsgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #717b98;");
                studentListBox.getChildren().add(noMsgLabel);
            } else {
                for (String student : students) {
                    HBox studentItem = createChatListItem(student, popup, true);
                    studentListBox.getChildren().add(studentItem);
                }
            }

            ScrollPane scrollPane = new ScrollPane(studentListBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                    "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 8 20; -fx-background-radius: 8;");
            closeBtn.setOnAction(e -> popup.close());

            HBox buttonBox = new HBox(10, closeBtn);
            buttonBox.setAlignment(Pos.CENTER);

            VBox content = new VBox(14, titleLabel, scrollPane, buttonBox);
            content.setPadding(new Insets(20));
            content.setAlignment(Pos.TOP_CENTER);
            content.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8faff); -fx-background-radius: 12;");

            Scene scene = new Scene(content, 400, 450);
            popup.setScene(scene);

            double parentX = parentStage.getX();
            double parentY = parentStage.getY();
            double parentW = parentStage.getWidth();
            double parentH = parentStage.getHeight();
            popup.setX(parentX + (parentW - 400) / 2);
            popup.setY(parentY + (parentH - 450) / 2);

            popup.show();

        } catch (IOException e) {
            show("Error", "Could not load student messages.");
        }
    }

    private HBox createChatListItem(String otherUser, Stage listPopup, boolean isTeacherView) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 14, 10, 14));
        item.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-border-color: #e4ebf7; -fx-border-radius: 10;");

        Label avatar = new Label(isTeacherView ? "👨‍🎓" : "👨‍🏫");
        avatar.setStyle("-fx-font-size: 24px;");

        VBox info = new VBox(2);
        Label nameLabel = new Label(otherUser);
        nameLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: #2d3748;");

        Label roleLabel = new Label(isTeacherView ? "Student" : "Teacher");
        roleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #717b98;");

        info.getChildren().addAll(nameLabel, roleLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Check for unread messages
        try {
            boolean hasUnread = MessageStore.hasUnreadFrom(username, otherUser);
            if (hasUnread) {
                item.setStyle("-fx-background-color: #fff8e6; -fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-border-color: #ffd666; -fx-border-radius: 10;");
                Label unreadDot = new Label("●");
                unreadDot.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                item.getChildren().addAll(avatar, info, unreadDot);
            } else {
                item.getChildren().addAll(avatar, info);
            }
        } catch (IOException e) {
            item.getChildren().addAll(avatar, info);
        }

        item.setOnMouseClicked(e -> {
            listPopup.close();
            openConversation(otherUser);
        });

        item.setOnMouseEntered(e -> {
            if (!item.getStyle().contains("#fff8e6")) {
                item.setStyle("-fx-background-color: #eef4ff; -fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-border-color: #c5d9f2; -fx-border-radius: 10;");
            }
        });

        item.setOnMouseExited(e -> {
            try {
                boolean hasUnread = MessageStore.hasUnreadFrom(username, otherUser);
                if (hasUnread) {
                    item.setStyle("-fx-background-color: #fff8e6; -fx-background-radius: 10; -fx-cursor: hand; " +
                            "-fx-border-color: #ffd666; -fx-border-radius: 10;");
                } else {
                    item.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 10; -fx-cursor: hand; " +
                            "-fx-border-color: #e4ebf7; -fx-border-radius: 10;");
                }
            } catch (IOException ex) {
                item.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-border-color: #e4ebf7; -fx-border-radius: 10;");
            }
        });

        return item;
    }

    private void openConversation(String otherUser) {
        try {
            // Mark messages as read
            MessageStore.markAsRead(username, otherUser);
            checkUnreadMessages(); // Update badge

            Stage chatPopup = new Stage();
            chatPopup.initStyle(StageStyle.UTILITY);
            chatPopup.initModality(Modality.NONE);
            chatPopup.setTitle("Chat with " + otherUser);

            Stage parentStage = (Stage) idLabel.getScene().getWindow();
            chatPopup.initOwner(parentStage);

            // Header
            boolean isTeacher = "teacher".equalsIgnoreCase(userRole);
            Label headerLabel = new Label((isTeacher ? "👨‍🎓 " : "👨‍🏫 ") + otherUser);
            headerLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 16px; -fx-text-fill: #1e63dc;");

            // Messages container
            VBox messagesBox = new VBox(6);
            messagesBox.setPadding(new Insets(10));

            ScrollPane messagesScroll = new ScrollPane(messagesBox);
            messagesScroll.setFitToWidth(true);
            messagesScroll.setPrefHeight(320);
            messagesScroll
                    .setStyle("-fx-background-color: #f5f7fa; -fx-background: #f5f7fa; -fx-background-radius: 8;");

            // Load existing messages
            List<MessageStore.ChatMessage> messages = MessageStore.getConversation(username, otherUser);
            for (MessageStore.ChatMessage msg : messages) {
                HBox msgBox = createMessageBubble(msg, msg.from().equalsIgnoreCase(username),
                        messagesBox, otherUser, messagesScroll);
                messagesBox.getChildren().add(msgBox);
            }

            // Auto-scroll to bottom
            Platform.runLater(() -> messagesScroll.setVvalue(1.0));

            // Input area
            TextField inputField = new TextField();
            inputField.setPromptText("Type a message...");
            inputField.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 20; " +
                    "-fx-border-color: #d1e4fa; -fx-border-radius: 20; -fx-padding: 8 14;");
            HBox.setHgrow(inputField, Priority.ALWAYS);

            Button sendBtn = new Button("Send");
            sendBtn.setStyle("-fx-background-color: linear-gradient(to right, #1e63dc, #3b82f6); " +
                    "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-padding: 8 16; " +
                    "-fx-background-radius: 20; -fx-cursor: hand;");

            Runnable sendMessage = () -> {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    try {
                        boolean fromStudent = !isTeacher;
                        MessageStore.sendMessage(username, otherUser, text, fromStudent);

                        // Add message to UI
                        MessageStore.ChatMessage newMsg = new MessageStore.ChatMessage(
                                username, otherUser, text,
                                java.time.LocalDateTime.now()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                fromStudent, false);
                        HBox msgBox = createMessageBubble(newMsg, true, messagesBox, otherUser, messagesScroll);
                        messagesBox.getChildren().add(msgBox);

                        inputField.clear();
                        Platform.runLater(() -> messagesScroll.setVvalue(1.0));
                    } catch (IOException ex) {
                        show("Send Failed", "Could not send message.");
                    }
                }
            };

            sendBtn.setOnAction(e -> sendMessage.run());
            inputField.setOnAction(e -> sendMessage.run());

            HBox inputBox = new HBox(10, inputField, sendBtn);
            inputBox.setAlignment(Pos.CENTER);
            inputBox.setPadding(new Insets(10, 0, 0, 0));

            // Refresh button
            Button refreshBtn = new Button("🔄 Refresh");
            refreshBtn.setStyle("-fx-background-color: #f0f7ff; -fx-text-fill: #1e63dc; " +
                    "-fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6;");
            refreshBtn.setOnAction(e -> {
                try {
                    MessageStore.markAsRead(username, otherUser);
                    refreshConversation(messagesBox, otherUser, messagesScroll);
                    checkUnreadMessages();
                } catch (IOException ex) {
                    show("Refresh Failed", "Could not refresh messages.");
                }
            });

            HBox headerBox = new HBox(10, headerLabel, new Region(), refreshBtn);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(headerBox.getChildren().get(1), Priority.ALWAYS);

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                    "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 12px; -fx-padding: 6 16; -fx-background-radius: 6;");
            closeBtn.setOnAction(e -> chatPopup.close());

            VBox content = new VBox(10, headerBox, messagesScroll, inputBox, closeBtn);
            content.setPadding(new Insets(16));
            content.setAlignment(Pos.TOP_CENTER);
            content.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8faff); -fx-background-radius: 12;");

            Scene scene = new Scene(content, 400, 500);
            chatPopup.setScene(scene);

            double parentX = parentStage.getX();
            double parentY = parentStage.getY();
            double parentW = parentStage.getWidth();
            double parentH = parentStage.getHeight();
            chatPopup.setX(parentX + (parentW - 400) / 2);
            chatPopup.setY(parentY + (parentH - 500) / 2);

            chatPopup.show();

        } catch (IOException e) {
            show("Error", "Could not open conversation.");
        }
    }

    private HBox createMessageBubble(MessageStore.ChatMessage msg, boolean isSent,
            VBox messagesBox, String otherUser, ScrollPane messagesScroll) {
        HBox box = new HBox(4);
        box.setPadding(new Insets(4, 8, 4, 8));

        VBox bubbleContainer = new VBox(2);
        bubbleContainer.setMaxWidth(250);

        // Message text
        Label msgText = new Label(msg.message());
        msgText.setWrapText(true);
        msgText.setStyle("-fx-font-size: 13px;");

        // Time label
        Label timeLabel = new Label(msg.timestamp().substring(11, 16)); // HH:mm
        timeLabel
                .setStyle("-fx-font-size: 9px; -fx-text-fill: " + (isSent ? "rgba(255,255,255,0.7)" : "#65676b") + ";");

        VBox bubble = new VBox(2);
        bubble.getChildren().addAll(msgText, timeLabel);
        bubble.setMaxWidth(220);

        // Spacer to push bubble to left or right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (isSent) {
            // Sent messages: RIGHT side with Messenger blue color
            bubble.setStyle("-fx-background-color: #0084ff; " +
                    "-fx-background-radius: 18 18 4 18; -fx-padding: 10 14;");
            msgText.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
            bubble.setAlignment(Pos.CENTER_RIGHT);
            timeLabel.setAlignment(Pos.CENTER_RIGHT);

            // Edit and Delete buttons for own messages only
            HBox actionBtns = new HBox(2);
            actionBtns.setAlignment(Pos.CENTER_RIGHT);

            Button editBtn = new Button("✏");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #717b98; " +
                    "-fx-font-size: 10px; -fx-padding: 2 4; -fx-cursor: hand;");
            editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #e8f4fd; -fx-text-fill: #1e63dc; " +
                    "-fx-font-size: 10px; -fx-padding: 2 4; -fx-cursor: hand; -fx-background-radius: 4;"));
            editBtn.setOnMouseExited(
                    e -> editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #717b98; " +
                            "-fx-font-size: 10px; -fx-padding: 2 4; -fx-cursor: hand;"));

            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #717b98; " +
                    "-fx-font-size: 10px; -fx-padding: 2 4; -fx-cursor: hand;");
            deleteBtn.setOnMouseEntered(
                    e -> deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #e74c3c; " +
                            "-fx-font-size: 10px; -fx-padding: 2 4; -fx-cursor: hand; -fx-background-radius: 4;"));
            deleteBtn.setOnMouseExited(
                    e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #717b98; " +
                            "-fx-font-size: 10px; -fx-padding: 2 4; -fx-cursor: hand;"));

            // Edit action
            editBtn.setOnAction(e -> {
                showEditDialog(msg, messagesBox, otherUser, messagesScroll);
            });

            // Delete action
            deleteBtn.setOnAction(e -> {
                try {
                    boolean deleted = MessageStore.deleteMessage(msg.from(), msg.to(), msg.timestamp());
                    if (deleted) {
                        refreshConversation(messagesBox, otherUser, messagesScroll);
                    } else {
                        show("Delete Failed", "Could not delete message.");
                    }
                } catch (IOException ex) {
                    show("Error", "Could not delete message.");
                }
            });

            actionBtns.getChildren().addAll(editBtn, deleteBtn);
            bubbleContainer.getChildren().addAll(bubble, actionBtns);
            bubbleContainer.setAlignment(Pos.CENTER_RIGHT);

            // Spacer FIRST, then bubble (pushes bubble to RIGHT)
            box.getChildren().addAll(spacer, bubbleContainer);

        } else {
            // Received messages: LEFT side with Messenger gray color
            bubble.setStyle("-fx-background-color: #e4e6eb; " +
                    "-fx-background-radius: 18 18 18 4; -fx-padding: 10 14;");
            msgText.setStyle("-fx-font-size: 13px; -fx-text-fill: #050505;");
            bubble.setAlignment(Pos.CENTER_LEFT);
            timeLabel.setAlignment(Pos.CENTER_LEFT);
            bubbleContainer.getChildren().add(bubble);
            bubbleContainer.setAlignment(Pos.CENTER_LEFT);

            // Bubble FIRST, then spacer (pushes bubble to LEFT)
            box.getChildren().addAll(bubbleContainer, spacer);
        }

        return box;
    }

    private void showEditDialog(MessageStore.ChatMessage msg, VBox messagesBox, String otherUser,
            ScrollPane messagesScroll) {
        Stage editPopup = new Stage();
        editPopup.initStyle(StageStyle.UTILITY);
        editPopup.initModality(Modality.APPLICATION_MODAL);
        editPopup.setTitle("Edit Message");

        Stage parentStage = (Stage) idLabel.getScene().getWindow();
        editPopup.initOwner(parentStage);

        Label titleLabel = new Label("✏️ Edit Message");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 16px; -fx-text-fill: #1e63dc;");

        TextField editField = new TextField(msg.message());
        editField.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; " +
                "-fx-border-color: #d1e4fa; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 13px;");
        editField.setPrefWidth(280);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #1e63dc, #3b82f6); " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-padding: 8 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                "-fx-font-family: 'Segoe UI Semibold'; -fx-padding: 8 20; -fx-background-radius: 8;");

        saveBtn.setOnAction(e -> {
            String newText = editField.getText().trim();
            if (!newText.isEmpty()) {
                try {
                    boolean edited = MessageStore.editMessage(msg.from(), msg.to(), msg.timestamp(), newText);
                    if (edited) {
                        editPopup.close();
                        refreshConversation(messagesBox, otherUser, messagesScroll);
                    } else {
                        show("Edit Failed", "Could not edit message.");
                    }
                } catch (IOException ex) {
                    show("Error", "Could not edit message.");
                }
            }
        });

        cancelBtn.setOnAction(e -> editPopup.close());

        HBox btnBox = new HBox(10, saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(14, titleLabel, editField, btnBox);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8faff); -fx-background-radius: 12;");

        Scene scene = new Scene(content, 340, 160);
        editPopup.setScene(scene);

        double parentX = parentStage.getX();
        double parentY = parentStage.getY();
        double parentW = parentStage.getWidth();
        double parentH = parentStage.getHeight();
        editPopup.setX(parentX + (parentW - 340) / 2);
        editPopup.setY(parentY + (parentH - 160) / 2);

        editPopup.show();
    }

    private void refreshConversation(VBox messagesBox, String otherUser, ScrollPane messagesScroll) {
        try {
            messagesBox.getChildren().clear();
            List<MessageStore.ChatMessage> refreshedMsgs = MessageStore.getConversation(username, otherUser);
            for (MessageStore.ChatMessage msg : refreshedMsgs) {
                HBox msgBox = createMessageBubble(msg, msg.from().equalsIgnoreCase(username),
                        messagesBox, otherUser, messagesScroll);
                messagesBox.getChildren().add(msgBox);
            }
            Platform.runLater(() -> messagesScroll.setVvalue(1.0));
        } catch (IOException ex) {
            show("Refresh Failed", "Could not refresh messages.");
        }
    }

    @FXML
    private void openTeacherNoticeManager() {
        Stage noticeManager = new Stage();
        noticeManager.initStyle(StageStyle.UTILITY);
        noticeManager.initModality(Modality.APPLICATION_MODAL);
        noticeManager.setTitle("📢 Notice Board Manager");

        Stage parentStage = (Stage) idLabel.getScene().getWindow();
        noticeManager.initOwner(parentStage);

        VBox mainContainer = new VBox(16);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f0f5ff);");

        Label titleLabel = new Label("📢 Notice Board Manager");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 20px; -fx-text-fill: #1e4d8c;");

        // Add new notice section
        VBox addSection = new VBox(10);
        addSection.setPadding(new Insets(14));
        addSection.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; " +
                "-fx-border-color: #d1e4fa; -fx-border-radius: 10;");

        Label addLabel = new Label("➕ Add New Notice");
        addLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: #1e63dc;");

        TextField noticeTitleField = new TextField();
        noticeTitleField.setPromptText("Notice Title");
        noticeTitleField.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e8f0; -fx-border-radius: 8; -fx-padding: 8 12;");

        TextArea noticeMessageArea = new TextArea();
        noticeMessageArea.setPromptText("Notice Message...");
        noticeMessageArea.setPrefRowCount(3);
        noticeMessageArea.setWrapText(true);
        noticeMessageArea.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e8f0; -fx-border-radius: 8; -fx-padding: 8;");

        ComboBox<String> durationSelector = new ComboBox<>();
        durationSelector.getItems().addAll("7 Days", "30 Days");
        durationSelector.setValue("7 Days");
        durationSelector.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 8;");

        Button addNoticeBtn = new Button("Add Notice");
        addNoticeBtn.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-padding: 8 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        HBox addControls = new HBox(10, durationSelector, addNoticeBtn);
        addControls.setAlignment(Pos.CENTER_LEFT);

        addSection.getChildren().addAll(addLabel, noticeTitleField, noticeMessageArea, addControls);

        // Existing notices section
        VBox noticesSection = new VBox(10);
        noticesSection.setPadding(new Insets(14));
        noticesSection.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; " +
                "-fx-border-color: #d1e4fa; -fx-border-radius: 10;");

        Label existingLabel = new Label("📋 Your Notices");
        existingLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: #1e63dc;");

        VBox noticesList = new VBox(8);
        ScrollPane noticesScroll = new ScrollPane(noticesList);
        noticesScroll.setFitToWidth(true);
        noticesScroll.setPrefHeight(200);
        noticesScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        noticesSection.getChildren().addAll(existingLabel, noticesScroll);

        // Load existing notices
        Runnable loadNotices = () -> {
            noticesList.getChildren().clear();
            try {
                List<NoticeStore.Notice> allNotices = NoticeStore.getAllActiveNotices();
                List<NoticeStore.Notice> myNotices = allNotices.stream()
                        .filter(n -> n.teacher().equalsIgnoreCase(username))
                        .toList();

                if (myNotices.isEmpty()) {
                    Label emptyLabel = new Label("No notices yet");
                    emptyLabel.setStyle("-fx-text-fill: #8898aa; -fx-font-style: italic;");
                    noticesList.getChildren().add(emptyLabel);
                } else {
                    for (NoticeStore.Notice notice : myNotices) {
                        VBox noticeCard = createNoticeCard(notice, noticesList, noticeManager);
                        noticesList.getChildren().add(noticeCard);
                    }
                }
            } catch (IOException e) {
                show("Error", "Could not load notices.");
            }
        };

        loadNotices.run();

        // Add notice button action
        addNoticeBtn.setOnAction(e -> {
            String title = noticeTitleField.getText().trim();
            String message = noticeMessageArea.getText().trim();
            int days = durationSelector.getValue().startsWith("7") ? 7 : 30;

            if (title.isEmpty() || message.isEmpty()) {
                show("Missing Info", "Please enter both title and message.");
                return;
            }

            try {
                NoticeStore.addNotice(username, title, message, days);
                noticeTitleField.clear();
                noticeMessageArea.clear();
                loadNotices.run();
                show("Success", "Notice added successfully!");
            } catch (IOException ex) {
                show("Error", "Could not add notice.");
            }
        });

        mainContainer.getChildren().addAll(titleLabel, addSection, noticesSection);

        Scene scene = new Scene(mainContainer, 450, 520);
        noticeManager.setScene(scene);

        double parentX = parentStage.getX();
        double parentY = parentStage.getY();
        double parentW = parentStage.getWidth();
        double parentH = parentStage.getHeight();
        noticeManager.setX(parentX + (parentW - 450) / 2);
        noticeManager.setY(parentY + (parentH - 520) / 2);

        noticeManager.show();
    }

    private VBox createNoticeCard(NoticeStore.Notice notice, VBox noticesList, Stage parentPopup) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));

        // Check if expiring soon (within 1 day)
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), notice.expiryDate());
        boolean expiringSoon = daysLeft <= 1;

        String borderColor = expiringSoon ? "#ef4444" : "#e0e8f0";
        card.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 8; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        Label titleLabel = new Label("📌 " + notice.title());
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-text-fill: #1e4d8c;");

        Label messageLabel = new Label(notice.message());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5d708f;");

        String expiryText = expiringSoon ? "⚠️ Expires: " + notice.expiryDate().toString() + " (SOON!)"
                : "📅 Expires: " + notice.expiryDate().toString() + " (" + daysLeft + " days left)";
        Label expiryLabel = new Label(expiryText);
        expiryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (expiringSoon ? "#ef4444" : "#8898aa") + ";");

        Button extendBtn = new Button("Extend +7 days");
        extendBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand;");

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand;");

        extendBtn.setOnAction(e -> {
            try {
                NoticeStore.extendNotice(notice.teacher(), notice.title(), notice.createdDate(), 7);
                refreshNoticesList(noticesList, parentPopup);
                show("Extended", "Notice extended by 7 days.");
            } catch (IOException ex) {
                show("Error", "Could not extend notice.");
            }
        });

        deleteBtn.setOnAction(e -> {
            try {
                NoticeStore.deleteNotice(notice.teacher(), notice.title(), notice.createdDate());
                refreshNoticesList(noticesList, parentPopup);
                show("Deleted", "Notice deleted successfully.");
            } catch (IOException ex) {
                show("Error", "Could not delete notice.");
            }
        });

        HBox btnBox = new HBox(8, extendBtn, deleteBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(titleLabel, messageLabel, expiryLabel, btnBox);
        return card;
    }

    private void refreshNoticesList(VBox noticesList, Stage parentPopup) {
        noticesList.getChildren().clear();
        try {
            List<NoticeStore.Notice> allNotices = NoticeStore.getAllActiveNotices();
            List<NoticeStore.Notice> myNotices = allNotices.stream()
                    .filter(n -> n.teacher().equalsIgnoreCase(username))
                    .toList();

            if (myNotices.isEmpty()) {
                Label emptyLabel = new Label("No notices yet");
                emptyLabel.setStyle("-fx-text-fill: #8898aa; -fx-font-style: italic;");
                noticesList.getChildren().add(emptyLabel);
            } else {
                for (NoticeStore.Notice notice : myNotices) {
                    VBox noticeCard = createNoticeCard(notice, noticesList, parentPopup);
                    noticesList.getChildren().add(noticeCard);
                }
            }
        } catch (IOException e) {
            show("Error", "Could not refresh notices.");
        }
    }
}
