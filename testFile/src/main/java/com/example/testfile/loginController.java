package com.example.testfile;

import java.io.IOException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class loginController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    public void login(ActionEvent event) throws IOException {
        if (username.getText().isEmpty() || password.getText().isEmpty()) {
            show("Login Instructions", "Please enter both email/username and password.");
            return;
        }

        String role = UserRoleSession.getSelectedRole();
        String displayName = AuthStore.loginAndGetDisplayName(role, username.getText().trim(), password.getText());

        if (displayName == null) {
            show("Login Failed", "No matching account found for selected role. Check role/credentials or signup first.");
        } else {
            try {
                FXMLLoader loader =
                        new FXMLLoader(HelloApplication.class.getResource("/com/example/testfile/user-home.fxml"));

                Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

                URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }

                UserHomeController controller = loader.getController();
                controller.setUserInfo(role, displayName);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException | RuntimeException ex) {
                show("Window Open Failed", "Could not open student/teacher profile window. Please try again.");
            }
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
        FXMLLoader loader =
                new FXMLLoader(HelloApplication.class.getResource("/com/example/testfile/role.fxml"));

        Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void openSignUp(ActionEvent event) throws IOException {
        FXMLLoader loader =
                new FXMLLoader(HelloApplication.class.getResource("/com/example/testfile/signup.fxml"));

        Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    private void openScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader =
                new FXMLLoader(HelloApplication.class.getResource(fxmlPath));

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
}