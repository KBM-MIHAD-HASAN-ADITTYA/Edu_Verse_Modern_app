package com.example.testfile;

import java.io.IOException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class roleController {

    @FXML
    private VBox teacherCard;

    @FXML
    private VBox studentCard;

    @FXML
    private StackPane teacherCheck;

    @FXML
    private StackPane studentCheck;

    private String selectedRole = "teacher";

    @FXML
    private void initialize() {
        updateSelectionUI();
    }

    private void openScene(ActionEvent event, String fxmlPath) throws IOException {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        FXMLLoader loader =
                new FXMLLoader(HelloApplication.class.getResource(fxmlPath));

        Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
    }

    private void openLogin(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/login.fxml");
    }

    public void openHome(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/welcome.fxml");
    }

    public void openCourses(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/courses.fxml");
    }

    public void openAbout(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/about.fxml");
    }

    private void updateSelectionUI() {
        if (teacherCard == null || studentCard == null || teacherCheck == null || studentCheck == null) {
            return;
        }

        boolean teacherSelected = "teacher".equalsIgnoreCase(selectedRole);

        teacherCard.getStyleClass().remove("role-option-card-selected");
        studentCard.getStyleClass().remove("role-option-card-selected");

        teacherCheck.getStyleClass().remove("role-check-hidden");
        studentCheck.getStyleClass().remove("role-check-hidden");

        if (teacherSelected) {
            teacherCard.getStyleClass().add("role-option-card-selected");
            studentCheck.getStyleClass().add("role-check-hidden");
        } else {
            studentCard.getStyleClass().add("role-option-card-selected");
            teacherCheck.getStyleClass().add("role-check-hidden");
        }
    }

    @FXML
    public void chooseTeacher() {
        selectedRole = "teacher";
        updateSelectionUI();
    }

    @FXML
    public void chooseStudent() {
        selectedRole = "student";
        updateSelectionUI();
    }

    @FXML
    public void continueWithRole(ActionEvent event) throws IOException {
        UserRoleSession.setSelectedRole(selectedRole);
        openLogin(event);
    }

    public void student(ActionEvent event) throws IOException {
        UserRoleSession.setSelectedRole("student");
        openLogin(event);
    }

    public void teacher(ActionEvent event) throws IOException {
        UserRoleSession.setSelectedRole("teacher");
        openLogin(event);
    }
}