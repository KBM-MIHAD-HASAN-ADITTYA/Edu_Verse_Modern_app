package com.example.testfile;

import javafx.application.Application;

/**
 * Launcher that opens TWO windows side by side on the screen.
 * - Left window: Teacher can login and upload materials
 * - Right window: Student can login and view materials
 *
 * Both windows operate independently with separate sessions.
 * Changes made by teacher (uploading PDFs) will be visible to student
 * after refreshing the class materials.
 */
public class DualWindowLauncher {
    public static void main(String[] args) {
        // Enable dual-window mode
        HelloApplication.setDualWindowMode(true);
        Application.launch(HelloApplication.class, args);
    }
}
