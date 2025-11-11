@echo off
REM Quick Build Script - No tests

echo Building Certificate Generation System (skipping tests)...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful! JAR file created in target/
echo.
echo To run the application:
echo   java -jar target\certificate-generation-system-1.0.0.jar
echo.
pause
