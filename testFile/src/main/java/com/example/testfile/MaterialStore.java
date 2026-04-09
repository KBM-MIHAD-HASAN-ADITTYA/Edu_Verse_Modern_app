package com.example.testfile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MaterialStore {

    private static final Path STORAGE_DIR = AppDataPaths.resolve("uploaded_materials");
    private static final Path META_FILE = AppDataPaths.resolve("uploaded_materials.txt");

    private MaterialStore() {
    }

    public static synchronized MaterialRecord saveMaterial(
            String teacher,
            String className,
            String materialName,
            Path sourcePdf
    ) throws IOException {
        ensureStorage();

        String safeTeacher = normalizeText(teacher);
        String safeClass = normalizeText(className);
        String safeMaterial = normalizeText(materialName);

        String baseName = sanitizeFilePart(safeMaterial) + "_" + sanitizeFilePart(safeTeacher);
        Path destination = uniquePdfPath(baseName);

        Files.copy(sourcePdf, destination, StandardCopyOption.REPLACE_EXISTING);

        String line = safeClass + "\t" + safeTeacher + "\t" + safeMaterial + "\t"
                + destination.toString() + System.lineSeparator();
        Files.writeString(
                META_FILE,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        return new MaterialRecord(safeClass, safeTeacher, safeMaterial, destination);
    }

    public static synchronized List<MaterialRecord> listByTeacher(String teacher) throws IOException {
        String normalizedTeacher = normalizeText(teacher);
        List<MaterialRecord> records = readAll();
        List<MaterialRecord> filtered = new ArrayList<>();

        for (MaterialRecord record : records) {
            if (record.teacher().equalsIgnoreCase(normalizedTeacher)) {
                filtered.add(record);
            }
        }

        filtered.sort(Comparator.comparing(MaterialRecord::className)
                .thenComparing(MaterialRecord::materialName));
        return filtered;
    }

    public static synchronized List<MaterialRecord> listByClass(String className) throws IOException {
        String normalizedClass = normalizeText(className);
        List<MaterialRecord> records = readAll();
        List<MaterialRecord> filtered = new ArrayList<>();

        for (MaterialRecord record : records) {
            if (record.className().equalsIgnoreCase(normalizedClass)) {
                filtered.add(record);
            }
        }

        filtered.sort(Comparator.comparing(MaterialRecord::materialName)
                .thenComparing(MaterialRecord::teacher));
        return filtered;
    }

    public static synchronized List<String> listClassNames() throws IOException {
        List<MaterialRecord> records = readAll();
        Set<String> classSet = new LinkedHashSet<>();

        records.stream()
                .sorted(Comparator.comparing(MaterialRecord::className))
                .forEach(record -> classSet.add(record.className()));

        return new ArrayList<>(classSet);
    }

    public static synchronized List<MaterialRecord> listAll() throws IOException {
        List<MaterialRecord> records = readAll();
        records.sort(Comparator.comparing(MaterialRecord::className)
                .thenComparing(MaterialRecord::materialName));
        return records;
    }

    public static synchronized boolean deleteMaterial(String teacher, Path filePath) throws IOException {
        ensureStorage();
        List<MaterialRecord> records = readAll();
        List<MaterialRecord> remaining = new ArrayList<>();
        boolean deleted = false;

        for (MaterialRecord record : records) {
            if (record.teacher().equalsIgnoreCase(teacher.trim()) &&
                record.filePath().equals(filePath)) {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                deleted = true;
            } else {
                remaining.add(record);
            }
        }

        if (deleted) {
            StringBuilder sb = new StringBuilder();
            for (MaterialRecord record : remaining) {
                sb.append(record.className()).append("\t")
                  .append(record.teacher()).append("\t")
                  .append(record.materialName()).append("\t")
                  .append(record.filePath().toString())
                  .append(System.lineSeparator());
            }
            Files.writeString(META_FILE, sb.toString(), StandardCharsets.UTF_8);
        }

        return deleted;
    }

    private static List<MaterialRecord> readAll() throws IOException {
        ensureStorage();
        List<String> lines = Files.readAllLines(META_FILE, StandardCharsets.UTF_8);
        List<MaterialRecord> records = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\\t", 4);
            if (parts.length != 4) {
                continue;
            }

            records.add(new MaterialRecord(
                    parts[0],
                    parts[1],
                    parts[2],
                    resolveStoredPath(parts[3])
            ));
        }

        return records;
    }

    private static Path uniquePdfPath(String baseName) throws IOException {
        int counter = 0;
        while (true) {
            String candidateName = counter == 0
                    ? baseName + ".pdf"
                    : baseName + "_" + counter + ".pdf";
            Path candidate = STORAGE_DIR.resolve(candidateName);
            if (!Files.exists(candidate)) {
                return candidate;
            }
            counter++;
        }
    }

    private static String sanitizeFilePart(String text) {
        String cleaned = normalizeText(text).replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank()) {
            return "file";
        }
        return cleaned;
    }

    private static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }

    private static void ensureStorage() throws IOException {
        if (!Files.exists(STORAGE_DIR)) {
            Files.createDirectories(STORAGE_DIR);
        }
        if (!Files.exists(META_FILE)) {
            Files.createFile(META_FILE);
        }
    }

    private static Path resolveStoredPath(String rawPath) {
        Path parsed = Path.of(rawPath);
        if (parsed.isAbsolute()) {
            return parsed;
        }
        return AppDataPaths.resolve(rawPath);
    }

    public record MaterialRecord(String className, String teacher, String materialName, Path filePath) {
    }
}
