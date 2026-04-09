package com.example.testfile;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    public static final String CSS_PATH = "/com/example/testfile/style.css";
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 650;

    // Flag to enable dual-window mode (two windows side by side)
    private static boolean dualWindowMode = false;

    @Override
    public void start(Stage stage) throws IOException {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        if (dualWindowMode) {
            // Create two normal-sized windows side by side
            double windowWidth = WINDOW_WIDTH;
            double windowHeight = WINDOW_HEIGHT;
            
            // Calculate positions to center both windows on screen
            double totalWidth = windowWidth * 2 + 20; // 20px gap between windows
            double startX = screenBounds.getMinX() + (screenBounds.getWidth() - totalWidth) / 2;
            double startY = screenBounds.getMinY() + (screenBounds.getHeight() - windowHeight) / 2;

            // Window 1 - Left side (for Teacher)
            Stage teacherStage = createWindow("Edu-Verse", windowWidth, windowHeight);
            teacherStage.setX(startX);
            teacherStage.setY(startY);
            teacherStage.show();

            // Window 2 - Right side (for Student)
            Stage studentStage = createWindow("Edu-Verse", windowWidth, windowHeight);
            studentStage.setX(startX + windowWidth + 20);
            studentStage.setY(startY);
            studentStage.show();

        } else {
            // Single window mode (original behavior)
            FXMLLoader fxmlLoader = new FXMLLoader(
                    HelloApplication.class.getResource("/com/example/testfile/welcome.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);

            URL cssUrl = HelloApplication.class.getResource(CSS_PATH);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setTitle("Edu-Verse");
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Creates a new window (Stage) with the welcome screen.
     */
    private Stage createWindow(String title, double width, double height) throws IOException {
        Stage newStage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("/com/example/testfile/welcome.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);

        URL cssUrl = HelloApplication.class.getResource(CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        newStage.setTitle(title);
        newStage.setScene(scene);
        return newStage;
    }

    public static void main(String[] args) {
        // Check for command-line argument to enable dual-window mode
        for (String arg : args) {
            if ("--dual".equalsIgnoreCase(arg) || "-d".equalsIgnoreCase(arg)) {
                dualWindowMode = true;
                break;
            }
        }
        launch(args);
    }

    /**
     * Enable dual-window mode programmatically (call before launch).
     */
    public static void setDualWindowMode(boolean enabled) {
        dualWindowMode = enabled;
    }
}