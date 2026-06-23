@echo off
REM Starts the Spring Boot backend and Angular frontend together (Windows).
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set BACKEND_DIR=%SCRIPT_DIR%backend
set FRONTEND_DIR=%SCRIPT_DIR%frontend
set BACKEND_PORT=8081
set FRONTEND_PORT=4200

REM ---------------------------------------------------------------------
REM spring-boot-maven-plugin needs Java 17+ to run. Detect the active
REM java's version and, if too old, look for a newer JDK on this machine
REM without touching the user's permanent JAVA_HOME.
REM ---------------------------------------------------------------------

set "JAVA_BIN=java"
if defined JAVA_HOME set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"

set JAVA_VER_RAW=
for /f "tokens=3" %%g in ('"%JAVA_BIN%" -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER_RAW=%%g
set JAVA_VER_RAW=%JAVA_VER_RAW:"=%

set JAVA_MAJOR=0
for /f "delims=. tokens=1,2" %%a in ("%JAVA_VER_RAW%") do (
    if "%%a"=="1" (set JAVA_MAJOR=%%b) else (set JAVA_MAJOR=%%a)
)

if %JAVA_MAJOR% LSS 17 (
    set "FOUND_JDK="
    for /d %%D in (
        "%ProgramFiles%\Eclipse Adoptium\jdk-1[7-9]*" "%ProgramFiles%\Eclipse Adoptium\jdk-2*"
        "%ProgramFiles%\Java\jdk-1[7-9]*" "%ProgramFiles%\Java\jdk-2*"
        "%ProgramFiles%\Microsoft\jdk-1[7-9]*" "%ProgramFiles%\Microsoft\jdk-2*"
    ) do (
        if not defined FOUND_JDK if exist "%%D\bin\java.exe" set "FOUND_JDK=%%D"
    )
    if defined FOUND_JDK (
        set "JAVA_HOME=%FOUND_JDK%"
        echo Using JDK at %JAVA_HOME%
    ) else (
        echo ERROR: Java 17+ is required to run the backend but was not found.
        echo Install a JDK 17+ from https://adoptium.net and try again.
        pause
        exit /b 1
    )
)

echo Starting backend (Spring Boot) on port %BACKEND_PORT%...
start "Backend - Spring Boot" cmd /k "set JAVA_HOME=%JAVA_HOME%&& set PATH=%JAVA_HOME%\bin;%PATH%&& cd /d "%BACKEND_DIR%" && mvnw.cmd spring-boot:run"

if not exist "%FRONTEND_DIR%\node_modules" (
    echo Installing frontend dependencies...
    pushd "%FRONTEND_DIR%"
    call npm install
    popd
)

echo Starting frontend (Angular) on port %FRONTEND_PORT%...
start "Frontend - Angular" cmd /k "cd /d "%FRONTEND_DIR%" && npm start"

echo.
echo Backend:  http://localhost:%BACKEND_PORT%
echo Frontend: http://localhost:%FRONTEND_PORT%
echo Each service is running in its own window. Run stop.bat to shut both down.
endlocal
