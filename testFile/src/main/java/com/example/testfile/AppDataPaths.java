package com.example.testfile;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AppDataPaths {

    private static final Path WORKING_DIR = Path.of("").toAbsolutePath().normalize();
    private static final Path PARENT_DIR = WORKING_DIR.getParent();

    private static final Path BASE_DIR = detectBaseDir();

    private AppDataPaths() {
    }

    public static Path resolve(String relativePath) {
        return BASE_DIR.resolve(relativePath).normalize();
    }

    private static Path detectBaseDir() {
        if (looksLikeDataRoot(WORKING_DIR)) {
            return WORKING_DIR;
        }

        if (PARENT_DIR != null && looksLikeDataRoot(PARENT_DIR)) {
            return PARENT_DIR;
        }

        return WORKING_DIR;
    }

    private static boolean looksLikeDataRoot(Path candidate) {
        return Files.exists(candidate.resolve("student_accounts.txt"))
                || Files.exists(candidate.resolve("teacher_accounts.txt"))
                || Files.exists(candidate.resolve("uploaded_materials.txt"))
                || Files.exists(candidate.resolve("uploaded_materials"));
    }
}
