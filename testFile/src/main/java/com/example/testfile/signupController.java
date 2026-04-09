package com.example.testfile;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class signupController {

    // Email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @FXML
    private TextField displayName;

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    public void signup() {
        if (displayName.getText().isEmpty() || email.getText().isEmpty() 
                || password.getText().isEmpty() || confirmPassword.getText().isEmpty()) {
            show("Signup Instructions", "Please fill username, email, password, and confirm password.");
            return;
        }

        if (displayName.getText().contains(",")) {
            show("Invalid Username", "Username cannot contain comma (,). Please choose another username.");
            return;
        }

        if (email.getText().contains(",")) {
            show("Invalid Email", "Email cannot contain comma (,). Please enter a valid email.");
            return;
        }

        // Validate email format
        if (!isValidEmail(email.getText().trim())) {
            show("Invalid Email", "Please enter a valid email address (e.g., user@example.com).");
            return;
        }

        if (!password.getText().equals(confirmPassword.getText())) {
            show("Password Mismatch", "Password and confirm password must be the same.");
            return;
        }

        try {
            String role = UserRoleSession.getSelectedRole();
            boolean saved = AuthStore.signup(role, displayName.getText().trim(), email.getText().trim(), password.getText());
            if (!saved) {
                show("Signup Failed", "Username or email already exists for this role. Try another username or email.");
                return;
            }
            showAndWait("Signup Successful", "Account created successfully. Please login now.");
            openLoginFromCurrentWindow();
        } catch (IOException e) {
            show("Signup Error", "Could not save data or open the login window. Please try again.");
        }
    }

    private boolean isValidEmail(String emailText) {
        return EMAIL_PATTERN.matcher(emailText).matches();
    }

    private void show(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }

    private void showAndWait(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void openLoginFromCurrentWindow() throws IOException {
        FXMLLoader loader =
                new FXMLLoader(HelloApplication.class.getResource("/com/example/testfile/login.fxml"));

        Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = (Stage) displayName.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader =
                new FXMLLoader(HelloApplication.class.getResource("/com/example/testfile/login.fxml"));

        Scene scene = new Scene(loader.load(), HelloApplication.WINDOW_WIDTH, HelloApplication.WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(HelloApplication.CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void goRoleBack(ActionEvent event) throws IOException {
        openScene(event, "/com/example/testfile/role.fxml");
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
