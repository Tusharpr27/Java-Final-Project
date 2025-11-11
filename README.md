# Certificate Generation System

A comprehensive Java-based certificate generation and management system built with Spring Boot.

## Features

### ✅ Functionality 1: Core Generation Engine
- **Template-Based Generation**: Uses customizable templates as a base for certificates
- **Dynamic Field Population**: Supports the following merge fields:
  - `{{recipient_name}}`
  - `{{course_name}}` / `{{achievement_title}}`
  - `{{completion_date}}`
  - `{{issuer_name}}` / `{{instructor_name}}`
  - `{{certificate_id}}`
- **Output Formats**:
  - PDF (primary format for printing and sharing)
  - PNG/JPEG (optional, for web viewing)

### ✅ Functionality 2: Template Management
- **Template Library**: Pre-built professional templates
- **Template Uploader**: Upload custom backgrounds (PDF, SVG, PNG, JPEG)
- **Visual Template Editor**: 
  - Add/remove text fields
  - Position fields with drag-and-drop
  - Customize fonts, sizes, and colors
  - Add signatures and logos

### ✅ Functionality 3: Recipient & Data Management
- **Manual Single Entry**: Web form for creating individual certificates
- **Batch Import**: 
  - CSV file upload and processing
  - Excel file upload (.xlsx, .xls)
  - Automatic column mapping
  - Bulk certificate generation
- **Issued Certificate Log**: Dashboard showing all generated certificates with details

### ✅ Functionality 4: Issuing & Delivery
- **Direct Download**: Immediate PDF download for administrators
- **Email Delivery**:
  - Automated email sending with attachments
  - Customizable email templates
  - Batch email delivery for CSV/Excel imports
  - Professional HTML email formatting

### ✅ Functionality 5: Verification & Security
- **Unique Certificate IDs**: Automatically generated (format: CERT-XXXX-XXXX)
- **QR Code**: Each certificate includes a QR code linking to verification
- **Public Verification Page**: Web page to validate certificate authenticity

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: H2 (embedded, file-based)
- **PDF Generation**: iText 8
- **QR Code**: ZXing (Google)
- **CSV/Excel**: OpenCSV, Apache POI
- **Frontend**: Thymeleaf, Bootstrap 5
- **Email**: Spring Mail (SMTP)

## Project Structure

```
src/main/java/com/certificate/
├── model/                      # Entity classes
│   ├── Certificate.java
│   ├── CertificateTemplate.java
│   └── TemplateField.java
├── repository/                 # JPA repositories
│   ├── CertificateRepository.java
│   └── CertificateTemplateRepository.java
├── service/                    # Business logic
│   ├── CertificateService.java
│   ├── PdfGenerationService.java
│   ├── EmailService.java
│   ├── BatchImportService.java
│   └── TemplateService.java
├── controller/                 # REST & Web controllers
│   ├── CertificateController.java
│   ├── TemplateController.java
│   └── WebController.java
├── dto/                        # Data transfer objects
│   ├── CertificateRequest.java
│   ├── CertificateResponse.java
│   └── BatchCertificateRequest.java
└── config/                     # Configuration
    └── DataInitializer.java
```

## Installation & Setup

### Prerequisites
- **Java 23** (Java 24 has compatibility issues with the Maven compiler plugin)
  - If you don't have Java 23, download from: https://www.oracle.com/java/technologies/downloads/
  - Alternative: Java 17 or 21 (LTS versions) also work
- Maven 3.9+ (Maven Wrapper included - no separate installation needed)

### Quick Start (Windows)

**Option 1: Using PowerShell (Recommended)**
```powershell
.\run.ps1
```

**Option 2: Using Batch File**
```cmd
run.bat
```

**Option 3: Manual Run**
```powershell
# Set Java 23
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:PATH = "C:\Program Files\Java\jdk-23\bin;" + $env:PATH

# Run the application
.\mvnw.cmd spring-boot:run
```

### First Time Setup

1. **Clone or extract the project**
   ```bash
   cd JavaProject
   ```

2. **Configure Email (Optional)**
   Edit `src/main/resources/application.yml`:
   ```yaml
   spring:
     mail:
       username: your-email@gmail.com
       password: your-app-password
   ```
   
   For Gmail, create an [App Password](https://support.google.com/accounts/answer/185833).

3. **Access the application**
   - Web UI: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:file:./data/certificates`
     - Username: `sa`
     - Password: (leave blank)

## Usage Guide

### 1. Generate Single Certificate
1. Navigate to **Generate** page
2. Fill in recipient details
3. Select a template (optional)
4. Check "Send via email" if desired
5. Click "Generate Certificate"
6. Download the generated PDF

### 2. Batch Import
1. Navigate to **Batch Import** page
2. Download the sample CSV template
3. Fill in your data (columns: name, email, course, achievement, date, issuer, instructor)
4. Upload the CSV or Excel file
5. System generates all certificates automatically
6. Emails are sent if recipient emails are provided

### 3. Verify Certificate
1. Navigate to **Verify** page
2. Enter the Certificate ID (found on the certificate)
3. View certificate details and validation status

### 4. Manage Templates
1. Navigate to **Templates** page
2. Create new templates
3. Upload custom backgrounds
4. Set default template

## API Endpoints

### Certificates
- `POST /api/certificates` - Generate single certificate
- `POST /api/certificates/batch` - Generate multiple certificates
- `POST /api/certificates/import/csv` - Import from CSV
- `POST /api/certificates/import/excel` - Import from Excel
- `GET /api/certificates` - List all certificates
- `GET /api/certificates/{id}` - Get certificate by ID
- `GET /api/certificates/{id}/download` - Download certificate PDF
- `GET /api/certificates/verify/{certificateId}` - Verify certificate

### Templates
- `GET /api/templates` - List all templates
- `POST /api/templates` - Create new template
- `POST /api/templates/{id}/background` - Upload template background
- `PUT /api/templates/{id}/set-default` - Set as default template
- `DELETE /api/templates/{id}` - Delete template

## CSV File Format

Your CSV file should have these columns (column names are case-insensitive):

```csv
name,email,course,achievement,date,issuer,instructor
John Doe,john@example.com,Python Programming,Excellence in Python,2025-11-09,Tech Institute,Dr. Smith
Jane Smith,jane@example.com,Web Development,Outstanding Skills,2025-11-09,Tech Institute,Prof. Johnson
```

**Required columns**: `name` (or `recipient_name`), `course` (or `course_name`)

**Optional columns**: `email`, `achievement`, `date`, `issuer`, `instructor`

## Configuration

All configuration is in `src/main/resources/application.yml`:

```yaml
certificate:
  storage:
    path: ./certificates          # Where PDFs are stored
  template:
    path: ./templates             # Where templates are stored
  verification:
    base-url: http://localhost:8080/verify  # Verification URL for QR codes
  email:
    from: noreply@certificates.com
```

## File Storage

Generated files are stored in:
- **Certificates**: `./certificates/*.pdf`
- **QR Codes**: `./certificates/qr/*.png`
- **Templates**: `./templates/*`
- **Database**: `./data/certificates.mv.db`

## Security Features

1. **Unique Certificate IDs**: Non-guessable format (CERT-XXXX-XXXX)
2. **QR Code**: Embedded in each certificate for quick verification
3. **Public Verification**: Anyone can verify authenticity via web interface
4. **Certificate Status**: Active, Revoked, or Expired
5. **Audit Trail**: All certificates logged with timestamps

## Development

### Running Tests
```bash
mvn test
```

### Building JAR
```bash
mvn clean package
java -jar target/certificate-generation-system-1.0.0.jar
```

## Troubleshooting

### Email Not Sending
- Verify SMTP credentials in `application.yml`
- For Gmail, enable 2-factor auth and create App Password
- Check firewall settings for port 587

### PDF Generation Errors
- Ensure `./certificates` directory is writable
- Check iText library version compatibility

### Database Locked
- Close H2 console before running application
- Delete `./data/certificates.mv.db.lock` if exists

## Future Enhancements

- [ ] Blockchain-based certificate verification
- [ ] Multi-language support
- [ ] Advanced template visual editor
- [ ] Certificate expiration dates
- [ ] Analytics dashboard
- [ ] API authentication (OAuth2)
- [ ] Docker containerization
- [ ] Cloud storage integration (AWS S3, Azure Blob)

## License

This project is provided as-is for educational and commercial use.

## Support

For issues or questions, please create an issue in the repository.

---

**Version**: 1.0.0  
**Last Updated**: November 9, 2025
