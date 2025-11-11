@echo off
REM Certificate Generation System - Windows Startup Script
REM This script builds and runs the Spring Boot application

echo ========================================
echo Certificate Generation System
echo ========================================
echo.

REM Set Java 23 for compatibility (Java 24 has compiler issues)
set "JAVA_HOME=C:\Program Files\Java\jdk-23"
set "PATH=C:\Program Files\Java\jdk-23\bin;%PATH%"

echo Using Java from: %JAVA_HOME%
java -version
echo.

echo ========================================
echo Starting the application...
echo ========================================
echo.
echo Access the application at: http://localhost:8080
echo Press Ctrl+C to stop the server
echo.

call mvnw.cmd spring-boot:run

pause
