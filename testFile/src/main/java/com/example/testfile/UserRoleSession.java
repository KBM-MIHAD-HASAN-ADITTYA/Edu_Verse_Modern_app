package com.example.testfile;

public final class UserRoleSession {

    private static String selectedRole = "student";

    private UserRoleSession() {
    }

    public static void setSelectedRole(String role) {
        if (role == null || role.isBlank()) {
            selectedRole = "student";
            return;
        }
        selectedRole = role.trim().toLowerCase();
    }

    public static String getSelectedRole() {
        return selectedRole;
    }
}
