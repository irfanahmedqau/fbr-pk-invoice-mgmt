@echo off
REM Stops the Spring Boot backend and Angular frontend (Windows).
REM Kills whatever process is listening on each port, regardless of how it was started.
setlocal

set BACKEND_PORT=8081
set FRONTEND_PORT=4200

echo Stopping backend (port %BACKEND_PORT%)...
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":%BACKEND_PORT% " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%P >nul 2>&1
)

echo Stopping frontend (port %FRONTEND_PORT%)...
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":%FRONTEND_PORT% " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%P >nul 2>&1
)

echo Done.
endlocal
