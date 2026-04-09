@echo off
setlocal

cd /d "%~dp0"

echo ==========================================
echo Building Edu-Verse Windows EXE
echo ==========================================

where java >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Java not found. Install JDK 21 and try again.
  exit /b 1
)

jpackage --version >nul 2>nul
if errorlevel 1 (
  echo [ERROR] jpackage not found.
  echo Use JDK 16+ (recommended: JDK 21).
  exit /b 1
)

if exist "dist" rmdir /s /q "dist"
if exist "target\installer-input" rmdir /s /q "target\installer-input"
mkdir "target\installer-input"

echo [1/3] Compiling and packaging JAR...
call mvnw.cmd -q -DskipTests clean package
if errorlevel 1 (
  echo [ERROR] Maven build failed.
  exit /b 1
)

for %%f in (target\*.jar) do (
  set "APP_JAR=%%~nxf"
  goto :jar_found
)

echo [ERROR] No JAR found in target folder.
exit /b 1

:jar_found
copy /y "target\%APP_JAR%" "target\installer-input\%APP_JAR%" >nul

echo [2/3] Creating EXE installer...
jpackage ^
  --type exe ^
  --name "Edu-Verse" ^
  --dest "dist" ^
  --input "target\installer-input" ^
  --main-jar "%APP_JAR%" ^
  --main-class "com.example.testfile.Launcher" ^
  --win-shortcut ^
  --win-menu

if errorlevel 1 (
  echo [WARN] EXE creation failed. This usually means WiX Toolset is missing.
  echo [3/3] Creating portable app image instead...
  jpackage ^
    --type app-image ^
    --name "Edu-Verse" ^
    --dest "dist" ^
    --input "target\installer-input" ^
    --main-jar "%APP_JAR%" ^
    --main-class "com.example.testfile.Launcher"

  if errorlevel 1 (
    echo [ERROR] Packaging failed.
    exit /b 1
  )

  echo Done. Portable app created in: dist\Edu-Verse\
  echo Run: dist\Edu-Verse\Edu-Verse.exe
  exit /b 0
)

echo Done. Installer created in dist folder.
exit /b 0

