package com.example.testfile;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Enable dual-window mode by default for this launcher
        // You can also pass --dual or -d as command line argument
        
        // Check if dual mode is requested via args
        boolean dualMode = false;
        for (String arg : args) {
            if ("--dual".equalsIgnoreCase(arg) || "-d".equalsIgnoreCase(arg)) {
                dualMode = true;
                break;
            }
        }
        
        HelloApplication.setDualWindowMode(dualMode);
        Application.launch(HelloApplication.class, args);
    }
}
