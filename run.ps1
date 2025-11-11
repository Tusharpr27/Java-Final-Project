# Certificate Generation System - PowerShell Startup Script
# This script runs the Spring Boot application with the correct Java version

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Certificate Generation System" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set Java 23 for compatibility (Java 24 has compiler issues)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:PATH = "C:\Program Files\Java\jdk-23\bin;" + $env:PATH

Write-Host "Using Java from: $env:JAVA_HOME" -ForegroundColor Yellow
java -version
Write-Host ""

Write-Host "Building the application..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Starting the application..." -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Access the application at: " -NoNewline
    Write-Host "http://localhost:8080" -ForegroundColor Green
    Write-Host ""
    
    # Start the application as a separate process
    $process = Start-Process java -ArgumentList "-jar","target\certificate-generation-system-1.0.0.jar" -NoNewWindow -PassThru
    Write-Host "Application started with PID: $($process.Id)" -ForegroundColor Yellow
    Write-Host "To stop the application, use: Stop-Process -Id $($process.Id)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Waiting for application to start..." -ForegroundColor Cyan
    Start-Sleep -Seconds 8
    Write-Host "Application should now be running at http://localhost:8080" -ForegroundColor Green
} else {
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
}
