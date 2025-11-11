# Troubleshooting Guide

## Common Issues and Solutions

### 🔧 Build & Installation Issues

#### Issue: "Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag"
**Symptoms:** 
```
Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:compile
Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

**Root Cause:** Java 24 is too new and incompatible with the Maven compiler plugin.

**Solution (Recommended):**
1. **Use Java 23** (or Java 17/21 LTS versions):
   ```powershell
   # Temporary fix (PowerShell)
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
   $env:PATH = "C:\Program Files\Java\jdk-23\bin;" + $env:PATH
   ```

2. **OR use the provided run scripts** (already configured):
   ```powershell
   .\run.ps1    # PowerShell
   run.bat      # Batch file
   ```

3. **Permanent fix:** Download and install Java 17 LTS from:
   - https://adoptium.net/temurin/releases/?version=17
   - OR Java 21: https://adoptium.net/temurin/releases/?version=21

#### Issue: "Maven command not found"
**Solution:**
- This project includes Maven Wrapper - no separate Maven installation needed
- Use `.\mvnw.cmd` instead of `mvn` on Windows
- Use `./mvnw` instead of `mvn` on Linux/Mac

#### Issue: "Java version mismatch"
**Symptoms:** 
```
Unsupported class file major version XX
```

**Solution:**
1. Check Java version: `java -version`
2. Ensure Java 17, 21, or 23 is installed (NOT Java 24)
3. Set JAVA_HOME:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
   ```

#### Issue: "Build failed - dependency download errors"
**Solution:**
```powershell
# Clear Maven cache and rebuild
.\mvnw.cmd clean
.\mvnw.cmd dependency:purge-local-repository
.\mvnw.cmd clean install
```

---

### 🚀 Runtime Issues

#### Issue: "Port 8080 already in use"
**Symptoms:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solution Option 1:** Change the port
1. Edit `src/main/resources/application.yml`
2. Change:
   ```yaml
   server:
     port: 8081
   ```

**Solution Option 2:** Kill the process using port 8080
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (use PID from above)
taskkill /PID <process_id> /F
```

#### Issue: "Application starts but crashes immediately"
**Solution:**
1. Check logs in console
2. Verify H2 database isn't locked:
   ```powershell
   # Delete lock file if exists
   del data\certificates.mv.db.lock
   ```
3. Check disk space (ensure at least 100MB free)

#### Issue: "Cannot access H2 console"
**Solution:**
1. Ensure H2 console is enabled in `application.yml`
2. Access: http://localhost:8080/h2-console
3. Use these settings:
   - JDBC URL: `jdbc:h2:file:./data/certificates`
   - Username: `sa`
   - Password: (leave blank)

---

### 📧 Email Issues

#### Issue: "Emails not sending"
**Symptoms:**
```
Failed to send email: Authentication failed
```

**Solution for Gmail:**
1. Enable 2-Factor Authentication on your Google account
2. Generate App Password:
   - Go to https://myaccount.google.com/apppasswords
   - Select "Mail" and "Windows Computer"
   - Copy the 16-character password
3. Update `application.yml`:
   ```yaml
   spring:
     mail:
       username: your-email@gmail.com
       password: xxxx xxxx xxxx xxxx  # App password
   ```

**Solution for other SMTP:**
```yaml
spring:
  mail:
    host: smtp.yourprovider.com
    port: 587  # or 465 for SSL
    username: your-email
    password: your-password
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

#### Issue: "Email sent but not received"
**Checklist:**
- [ ] Check spam/junk folder
- [ ] Verify recipient email is correct
- [ ] Check SMTP server logs
- [ ] Test with a different email provider

---

### 📄 PDF Generation Issues

#### Issue: "PDF not generated or corrupted"
**Solution:**
1. Check `certificates/` directory exists
2. Verify write permissions
3. Check disk space
4. Review console for iText errors

#### Issue: "QR code not appearing on certificate"
**Solution:**
1. Check `certificates/qr/` directory exists
2. Verify ZXing library is in dependencies
3. Check console for QR generation errors

#### Issue: "Background image not showing"
**Solution:**
1. Verify template background path is correct
2. Supported formats: PDF, SVG, PNG, JPEG
3. Check file permissions
4. File size should be reasonable (<10MB)

---

### 📊 CSV/Excel Import Issues

#### Issue: "CSV import fails"
**Common causes:**
- Incorrect column names
- Missing required columns (name, course)
- Invalid date format
- Special characters in data

**Solution:**
1. Download the sample CSV template
2. Ensure column names match:
   - `name` or `recipient_name` ✓
   - `email` or `recipient_email`
   - `course` or `course_name` ✓
3. Use UTF-8 encoding
4. Date format: `YYYY-MM-DD` or `MM/DD/YYYY`

**Example valid CSV:**
```csv
name,email,course,achievement,date
John Doe,john@example.com,Java 101,Excellence,2025-11-09
Jane Smith,jane@example.com,Python,Outstanding,2025-11-09
```

#### Issue: "Excel import fails"
**Solution:**
1. Save as `.xlsx` format (not `.xls`)
2. Ensure first row contains headers
3. Remove empty rows
4. No merged cells in data area

---

### 🔍 Verification Issues

#### Issue: "Certificate verification fails for valid certificate"
**Solution:**
1. Check certificate ID format (CERT-XXXX-XXXX)
2. Verify database connection
3. Check certificate status (must be ACTIVE)
4. Case sensitivity - use exact ID

#### Issue: "QR code scan doesn't work"
**Checklist:**
- [ ] QR code generated successfully
- [ ] Verification URL is accessible
- [ ] Network connectivity
- [ ] QR scanner app is working

---

### 💾 Database Issues

#### Issue: "Database locked"
**Symptoms:**
```
org.h2.jdbc.JdbcSQLException: Database may be already in use
```

**Solution:**
```powershell
# Close all H2 console windows
# Delete lock file
del data\certificates.mv.db.lock
# Restart application
```

#### Issue: "Data loss after restart"
**Solution:**
1. Verify database is file-based (not in-memory)
2. Check `application.yml`:
   ```yaml
   datasource:
     url: jdbc:h2:file:./data/certificates  # Not jdbc:h2:mem:
   ```

#### Issue: "Cannot see data in H2 console"
**Solution:**
1. Ensure application is running
2. Don't connect to console while app is running (file lock)
3. Stop app first, then open H2 console

---

### 🌐 Web UI Issues

#### Issue: "Page not loading or 404 errors"
**Solution:**
1. Verify application started successfully
2. Check console for errors
3. Try: http://localhost:8080 (not https)
4. Clear browser cache
5. Try incognito/private mode

#### Issue: "Form submission fails"
**Browser Console Errors:**
1. Press F12 to open Developer Tools
2. Check Console tab for errors
3. Check Network tab for failed requests

**Common fixes:**
- Disable browser extensions
- Check JavaScript is enabled
- Verify API endpoints are accessible

#### Issue: "Template/styling broken"
**Solution:**
1. Check internet connection (CDN resources)
2. Bootstrap CSS/JS loading from CDN
3. Clear browser cache
4. Check browser compatibility (use modern browser)

---

### 📦 File Storage Issues

#### Issue: "Cannot download certificates"
**Solution:**
1. Verify file exists in `certificates/` folder
2. Check file permissions
3. Verify file path in database
4. Check storage path configuration

#### Issue: "Template upload fails"
**Checklist:**
- [ ] File size < 10MB
- [ ] Supported format (PDF, SVG, PNG, JPEG)
- [ ] Correct file extension
- [ ] Templates directory exists and writable

---

### 🧪 Testing Issues

#### Issue: "Tests fail during build"
**Solution:**
```powershell
# Skip tests to build quickly
mvn clean install -DskipTests

# Run tests separately
mvn test
```

#### Issue: "Specific test fails"
**Solution:**
1. Check test dependencies
2. Review error messages
3. Ensure test database is clean
4. Check mocked services

---

### 🔒 Security & Permissions

#### Issue: "Access denied errors"
**Windows Solution:**
```powershell
# Run as Administrator if needed
# Right-click run.bat → Run as administrator
```

#### Issue: "Cannot create directories"
**Solution:**
1. Check folder permissions
2. Verify disk isn't full
3. Antivirus might block - add exception
4. Run from a location you have write access

---

### ⚡ Performance Issues

#### Issue: "Slow certificate generation"
**Solutions:**
1. Reduce background image size
2. Check available memory
3. Optimize template complexity
4. For batch: Process in smaller chunks

#### Issue: "Application slow to start"
**Solutions:**
1. Normal startup time: 10-30 seconds
2. Increase JVM memory:
   ```powershell
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx1024m"
   ```

---

### 🆘 Emergency Recovery

#### Complete Reset
```powershell
# Stop application (Ctrl+C)
# Delete generated data
del /s /q certificates\*
del /s /q data\*
del /s /q templates\*

# Rebuild and restart
mvn clean install
mvn spring-boot:run
```

#### Export Database Before Reset
```sql
-- Connect to H2 console
-- Run this SQL:
SCRIPT TO 'backup.sql';

-- To restore later:
RUNSCRIPT FROM 'backup.sql';
```

---

### 📞 Getting Help

#### Check Logs
```powershell
# Application logs are in console output
# For production, check:
logs\application.log
```

#### Enable Debug Logging
Edit `application.yml`:
```yaml
logging:
  level:
    com.certificate: DEBUG
    org.springframework: DEBUG
```

#### Collect Diagnostic Info
```powershell
# Java version
java -version

# Maven version
mvn -version

# Check running processes
netstat -ano | findstr :8080

# Disk space
dir

# Check application.yml
type src\main\resources\application.yml
```

---

### ✅ Pre-flight Checklist

Before running the application:
- [ ] Java 17+ installed
- [ ] Maven 3.6+ installed
- [ ] Port 8080 available
- [ ] At least 500MB disk space
- [ ] Write permissions in project directory
- [ ] SMTP credentials configured (if using email)

---

### 🎯 Still Having Issues?

1. Check **README.md** for complete setup instructions
2. Review **QUICKSTART.md** for basic usage
3. Check **ARCHITECTURE.md** for system design
4. Review error messages carefully
5. Search for specific error messages online
6. Check Spring Boot documentation: https://spring.io/projects/spring-boot

---

**Last Updated:** November 9, 2025
