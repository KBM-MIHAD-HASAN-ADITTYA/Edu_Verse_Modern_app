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

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class CoursesController {

    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox mainContentBox;
    @FXML private HBox courseCardsContainer;
    @FXML private HBox courseCardsRow2;
    @FXML private HBox courseCardsRow3;
    @FXML private VBox chapterContentPane;
    @FXML private Label selectedCourseTitle;
    @FXML private FlowPane chapterSlotsPane;
    @FXML private ScrollPane chapterScrollPane;

    private VBox selectedCard = null;
    private String currentCourseName = null;
    private final Map<String, Path> itemToFile = new HashMap<>();

    private void openScene(Stage stage, String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
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
    public void initialize() {
        // Initially hide the chapter content pane
        if (chapterContentPane != null) {
            chapterContentPane.setVisible(false);
            chapterContentPane.setManaged(false);
        }
    }

    @FXML
    public void closeChapterPane() {
        // Hide the chapter content pane
        if (chapterContentPane != null) {
            chapterContentPane.setVisible(false);
            chapterContentPane.setManaged(false);
        }
        // Deselect the current card
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("course-card-selected");
            selectedCard = null;
        }
    }

    private VBox findCardById(String cardId) {
        // Search in all rows
        VBox card = (VBox) courseCardsContainer.lookup("#" + cardId);
        if (card == null && courseCardsRow2 != null) {
            card = (VBox) courseCardsRow2.lookup("#" + cardId);
        }
        if (card == null && courseCardsRow3 != null) {
            card = (VBox) courseCardsRow3.lookup("#" + cardId);
        }
        // Fallback to scene lookup
        if (card == null && mainScrollPane != null) {
            card = (VBox) mainScrollPane.getScene().lookup("#" + cardId);
        }
        return card;
    }

    public void selectCourse(VBox card, String courseName, String[] chapters) {
        // Deselect previous card
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("course-card-selected");
        }
        
        // Select new card
        selectedCard = card;
        currentCourseName = courseName;
        if (card != null) {
            card.getStyleClass().add("course-card-selected");
        }
        
        // Update chapter content
        selectedCourseTitle.setText(courseName + " - Subjects");
        chapterSlotsPane.getChildren().clear();
        
        for (String chapter : chapters) {
            VBox chapterSlot = createChapterSlot(courseName, chapter);
            chapterSlotsPane.getChildren().add(chapterSlot);
        }
        
        // Show chapter pane
        chapterContentPane.setVisible(true);
        chapterContentPane.setManaged(true);
        
        // Scroll to top to show the chapter content
        if (mainScrollPane != null) {
            mainScrollPane.setVvalue(0);
        }
    }

    private VBox createChapterSlot(String courseName, String chapterName) {
        VBox slot = new VBox(8);
        slot.getStyleClass().add("chapter-slot");
        slot.setPrefWidth(200);
        slot.setPrefHeight(100);
        
        Label icon = new Label("📄");
        icon.setStyle("-fx-font-size: 24px;");
        
        Label name = new Label(chapterName);
        name.getStyleClass().add("chapter-slot-title");
        name.setWrapText(true);
        
        // Check if PDFs are available for this chapter
        String classKey = courseName + " - " + chapterName;
        int pdfCount = countPdfsForClass(classKey);
        
        Label status = new Label(pdfCount > 0 ? pdfCount + " PDF(s) Available" : "No PDFs yet");
        status.getStyleClass().add("chapter-slot-status");
        if (pdfCount > 0) {
            status.setStyle("-fx-text-fill: #27ae60;");
        }
        
        slot.getChildren().addAll(icon, name, status);
        
        // Click to open PDFs popup
        slot.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                openChapterPdfsPopup(courseName, chapterName);
            }
        });
        
        // Hover effect
        slot.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), slot);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        slot.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), slot);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        return slot;
    }
    
    private int countPdfsForClass(String classKey) {
        try {
            List<MaterialStore.MaterialRecord> records = MaterialStore.listByClass(classKey);
            return records.size();
        } catch (IOException e) {
            return 0;
        }
    }
    
    private void openChapterPdfsPopup(String courseName, String chapterName) {
        String classKey = courseName + " - " + chapterName;
        
        try {
            List<MaterialStore.MaterialRecord> records = MaterialStore.listByClass(classKey);
            itemToFile.clear();
            
            if (records.isEmpty()) {
                showAlert("No PDFs", "No PDFs uploaded for this subject yet.\nTeachers can upload PDFs from the Teacher Dashboard.");
                return;
            }
            
            ListView<String> listView = new ListView<>();
            listView.setPrefHeight(280);
            listView.setPrefWidth(420);
            
            for (MaterialStore.MaterialRecord record : records) {
                String row = "📄 " + record.materialName() + "  |  👨‍🏫 " + record.teacher();
                listView.getItems().add(row);
                itemToFile.put(row, record.filePath());
            }
            
            showPdfPopup(chapterName + " - Materials", listView);
            
        } catch (IOException e) {
            showAlert("Load Error", "Could not load subject materials.");
        }
    }
    
    private void showPdfPopup(String title, ListView<String> listView) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.UTILITY);
        popup.initModality(Modality.NONE);
        popup.setTitle(title);

        Stage parentStage = (Stage) courseCardsContainer.getScene().getWindow();
        popup.initOwner(parentStage);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 20px; -fx-text-fill: #1e63dc;");

        Label hintLabel = new Label("Double-click to open • Select for actions");
        hintLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-text-fill: #717b98; -fx-font-style: italic;");

        listView.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #e4ebf7; -fx-border-width: 1.5; -fx-padding: 6;");

        Button openBtn = new Button("📖 Open");
        openBtn.setStyle("-fx-background-color: linear-gradient(to right, #2369df, #1f63d8); " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 10 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        Button downloadBtn = new Button("⬇ Download");
        downloadBtn.setStyle("-fx-background-color: linear-gradient(to right, #35c887, #2db87a); " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 10 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: #5d708f; " +
                "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; " +
                "-fx-border-color: #dce5f3; -fx-border-radius: 8; -fx-border-width: 1;");

        openBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Path file = itemToFile.get(selected);
                openPdf(file);
            } else {
                showAlert("Select PDF", "Please select a PDF from the list first.");
            }
        });

        downloadBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Path file = itemToFile.get(selected);
                downloadPdf(file, popup);
            } else {
                showAlert("Select PDF", "Please select a PDF to download.");
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

        HBox buttonBox = new HBox(12, openBtn, downloadBtn, closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(14, titleLabel, hintLabel, listView, buttonBox);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f8faff); -fx-background-radius: 16;");

        Scene scene = new Scene(content);
        popup.setScene(scene);
        popup.setWidth(480);
        popup.setHeight(450);

        double parentX = parentStage.getX();
        double parentY = parentStage.getY();
        double parentW = parentStage.getWidth();
        double parentH = parentStage.getHeight();
        popup.setX(parentX + (parentW - 480) / 2);
        popup.setY(parentY + (parentH - 450) / 2);

        popup.show();
    }
    
    private void openPdf(Path file) {
        if (file == null || !Files.exists(file)) {
            showAlert("Open Failed", "Selected file is not available.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file.toFile());
                return;
            }
            new ProcessBuilder("cmd", "/c", "start", "", file.toAbsolutePath().toString()).start();
        } catch (IOException e) {
            showAlert("Open Failed", "Could not open selected PDF. Ensure a PDF viewer is installed.");
        }
    }
    
    private void downloadPdf(Path file, Stage popup) {
        if (file == null || !Files.exists(file)) {
            showAlert("Download Failed", "Selected file is not available.");
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
                showAlert("Download Complete", "PDF saved to: " + destination.getName());
            } catch (IOException e) {
                showAlert("Download Failed", "Could not save PDF: " + e.getMessage());
            }
        }
    }
    
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }

    @FXML
    public void selectClass910() {
        VBox card = findCardById("class910Card");
        selectCourse(card, "cls9-10", new String[]{
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
    }

    @FXML
    public void selectClass1112() {
        VBox card = findCardById("class1112Card");
        selectCourse(card, "cls11-12", new String[]{
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
    }

    @FXML
    public void selectAdmission() {
        VBox card = findCardById("admissionCard");
        selectCourse(card, "Admission", new String[]{
            "Bangla",
            "English",
            "Math",
            "ICT",
            "Biology",
            "Physics",
            "Chemistry"
        });
    }

    @FXML
    public void selectBSC() {
        VBox card = findCardById("bscCard");
        selectCourse(card, "BSC", new String[]{
            "CSE 106",
            "CSE 108",
            "CSE 101",
            "CSE 102",
            "EEE 164",
            "Math 143"
        });
    }

    @FXML
    public void selectMasters() {
        VBox card = findCardById("mastersCard");
        selectCourse(card, "Masters", new String[]{
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
    }

    @FXML
    public void selectBCS() {
        VBox card = findCardById("bcsCard");
        selectCourse(card, "BCS", new String[]{
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
    }

    @FXML
    public void selectBBA() {
        VBox card = findCardById("bbaCard");
        selectCourse(card, "BBA", new String[]{
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
}
