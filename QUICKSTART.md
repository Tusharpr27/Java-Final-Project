# Quick Start Guide

## Get Started in 5 Minutes

### 1. Build and Run
```powershell
# Navigate to project directory
cd C:\Users\shukl\OneDrive\Desktop\JavaProject

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### 2. Access the Application
Open your browser and go to: **http://localhost:8080**

### 3. Generate Your First Certificate

#### Option A: Single Certificate (Manual)
1. Click **"Generate"** in the navigation
2. Fill in:
   - Recipient Name: `John Doe`
   - Course Name: `Java Programming`
   - Email: `john@example.com` (optional)
3. Click **"Generate Certificate"**
4. Download your PDF!

#### Option B: Batch Import (Multiple Certificates)
1. Click **"Batch Import"** in the navigation
2. Download the sample CSV template
3. Open in Excel/Notepad and add your data:
   ```csv
   name,email,course,achievement,date
   John Doe,john@example.com,Python 101,Excellence,2025-11-09
   Jane Smith,jane@example.com,Web Dev,Outstanding,2025-11-09
   ```
4. Save and upload the file
5. Click **"Import & Generate Certificates"**
6. All certificates are created instantly!

### 4. Verify a Certificate
1. Click **"Verify"** in the navigation
2. Enter the Certificate ID (e.g., `CERT-1A2B-3C4D`)
3. View the validation results

### 5. View All Certificates
Click **"Certificates"** to see the complete log of all issued certificates.

## Email Configuration (Optional)

To enable email delivery:

1. Open `src/main/resources/application.yml`
2. Update the email settings:
   ```yaml
   spring:
     mail:
       username: your-email@gmail.com
       password: your-app-password
   ```
3. For Gmail:
   - Enable 2-Factor Authentication
   - Generate an App Password at: https://myaccount.google.com/apppasswords
   - Use the 16-character app password

4. Restart the application

## Sample CSV File

Create a file named `certificates.csv`:

```csv
name,email,course,achievement,date,issuer,instructor
Alice Johnson,alice@example.com,Advanced Java,Java Mastery,2025-11-09,Tech Academy,Dr. Williams
Bob Martinez,bob@example.com,Spring Boot,Spring Expert,2025-11-09,Tech Academy,Prof. Davis
Carol White,carol@example.com,Microservices,Architecture Pro,2025-11-09,Tech Academy,Dr. Miller
David Lee,david@example.com,Cloud Computing,Cloud Champion,2025-11-09,Tech Academy,Prof. Anderson
```

Upload this file via the **Batch Import** page!

## Troubleshooting

**Problem**: Application won't start
- **Solution**: Ensure Java 17 is installed (`java -version`)

**Problem**: Port 8080 already in use
- **Solution**: Change port in `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

**Problem**: Can't find generated certificates
- **Solution**: Check the `./certificates` folder in your project directory

## Next Steps

- ✅ Create custom templates in **Template Management**
- ✅ Upload custom backgrounds (PDF, PNG, SVG)
- ✅ Set up email delivery
- ✅ Integrate QR code verification

## API Testing (Optional)

Test the REST API using curl or Postman:

```powershell
# Generate a certificate
curl -X POST http://localhost:8080/api/certificates `
  -H "Content-Type: application/json" `
  -d '{
    "recipientName": "Test User",
    "courseName": "API Testing Course",
    "recipientEmail": "test@example.com",
    "sendEmail": false
  }'

# Verify a certificate
curl http://localhost:8080/api/certificates/verify/CERT-1234-5678

# List all certificates
curl http://localhost:8080/api/certificates
```

## Support

Need help? Check the main **README.md** for detailed documentation.

---

**🎓 Happy Certificate Generating!**
