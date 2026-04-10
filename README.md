# Edu-Verse (JavaFX Project)

## Student Information
- Name: `Your Name`
- ID: `Your ID`
- Course/Section: `Your Course/Section`

## Project Overview
Edu-Verse is a JavaFX-based educational platform with separate Student and Teacher roles.  
It includes login/signup, role-based home screens, course/chapter browsing, notices, messaging, and study material upload/download.

## Tech Stack
- Java 21
- JavaFX 21.0.6
- Maven

## Features
- Role-based authentication (Student/Teacher)
- Dashboard for each role
- Course and chapter selection
- Notice board
- Messaging system with unread count
- PDF material upload and access
- Optional dual-window mode for side-by-side usage

## Project Structure
```text
testFile/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
└── src/
    └── main/
        ├── java/com/example/testfile/
        └── resources/com/example/testfile/
```

## Prerequisites
- JDK 21 installed
- Internet connection (first Maven dependency download)

## How to Run
From the `testFile` folder:

```bash
mvn clean compile
mvn javafx:run
```

### Run in Dual Window Mode (Optional)
```bash
mvn javafx:run -Djavafx.run.args="--dual"
```

## Build a Windows EXE
From the `testFile` folder, run:

```bat
build-exe.bat
```

Output:
- Portable app image created in: `dist\Edu-Verse\Edu-Verse.exe`
- Self-contained executable with bundled Java runtime
- No Java installation required on target machine

Notes:
- First build takes 5-10 minutes (downloads dependencies)
- Requires Java 21 JDK (not JRE)
- If `dist` is not created, you likely ran only `mvnw.cmd`; run `build-exe.bat` instead

## Main Entry Point
- `com.example.testfile.HelloApplication`

## Data Files
At runtime, the app creates/uses:
- `student_accounts.txt`
- `teacher_accounts.txt`
- `messages.txt`
- `notices.txt`
- `uploaded_materials.txt`
- `uploaded_materials/`
