# System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│  Web Browser                    │  REST API Clients             │
│  - Chrome/Firefox/Edge          │  - Postman                    │
│  - Mobile Browsers              │  - curl                       │
│  - Desktop Browsers             │  - Third-party Apps           │
└─────────────────────────────────────────────────────────────────┘
                              ↓↑ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
├─────────────────────────────────────────────────────────────────┤
│  Web Controllers          │  REST API Controllers               │
│  - WebController          │  - CertificateController            │
│  - Thymeleaf Views        │  - TemplateController               │
│  - HTML/CSS/JS            │  - JSON Responses                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓↑
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                              │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Certificate  │  │  Template    │  │    Email     │          │
│  │   Service    │  │   Service    │  │   Service    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │ PDF Gen      │  │ Batch Import │                            │
│  │   Service    │  │   Service    │                            │
│  └──────────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓↑
┌─────────────────────────────────────────────────────────────────┐
│                   DATA ACCESS LAYER                             │
├─────────────────────────────────────────────────────────────────┤
│  Repositories (Spring Data JPA)                                 │
│  - CertificateRepository                                        │
│  - CertificateTemplateRepository                                │
└─────────────────────────────────────────────────────────────────┘
                              ↓↑
┌─────────────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│  H2 Database                  │  File System                    │
│  - certificates table         │  - PDF Files                    │
│  - certificate_templates      │  - QR Codes                     │
│  - Audit data                 │  - Template Backgrounds         │
└─────────────────────────────────────────────────────────────────┘
```

## Component Interaction Flow

### 1. Single Certificate Generation Flow

```
User → Web UI → WebController → CertificateService
                                        ↓
                    ┌───────────────────┴────────────────────┐
                    ↓                   ↓                    ↓
            TemplateService    PdfGenerationService   EmailService
                    ↓                   ↓                    ↓
            Get Template         Generate PDF         Send Email
                    ↓                   ↓                    ↓
            Return Template      Save to Disk         SMTP Server
                    ↓                   ↓                    
                Database ←──── Save Certificate ────→ Recipient
```

### 2. Batch Import Flow

```
User → Upload CSV/Excel → CertificateController
                                ↓
                        BatchImportService
                                ↓
                    Parse File & Map Columns
                                ↓
                    Create CertificateRequest List
                                ↓
                        CertificateService
                                ↓
                    For Each Certificate:
                                ↓
        ┌───────────────────────┼───────────────────┐
        ↓                       ↓                   ↓
  Generate PDF          Save to Database      Send Email
        ↓                       ↓                   ↓
  File System              H2 Database        Recipients
```

### 3. Verification Flow

```
User/QR Scan → Verification Page → CertificateController
                                            ↓
                                    /api/certificates/verify/{id}
                                            ↓
                                    CertificateService
                                            ↓
                                    CertificateRepository
                                            ↓
                                       Database Query
                                            ↓
                        ┌───────────────────┴────────────────┐
                        ↓                                    ↓
                  Valid & Active                    Invalid/Revoked
                        ↓                                    ↓
                Display Details                     Display Error
```

## Technology Stack Layers

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND                             │
│  - HTML5, CSS3, JavaScript                              │
│  - Bootstrap 5.3.0                                      │
│  - Thymeleaf Template Engine                            │
│  - jQuery 3.7.0                                         │
│  - DataTables 1.13.6                                    │
└─────────────────────────────────────────────────────────┘
                         ↓↑
┌─────────────────────────────────────────────────────────┐
│                    BACKEND                              │
│  - Java 17                                              │
│  - Spring Boot 3.2.0                                    │
│  - Spring Web MVC                                       │
│  - Spring Data JPA                                      │
│  - Spring Mail                                          │
│  - Hibernate ORM                                        │
└─────────────────────────────────────────────────────────┘
                         ↓↑
┌─────────────────────────────────────────────────────────┐
│                   LIBRARIES                             │
│  - iText 8.0.2 (PDF Generation)                         │
│  - ZXing 3.5.2 (QR Codes)                               │
│  - OpenCSV 5.9 (CSV Processing)                         │
│  - Apache POI 5.2.5 (Excel Processing)                  │
│  - Lombok (Boilerplate Reduction)                       │
└─────────────────────────────────────────────────────────┘
                         ↓↑
┌─────────────────────────────────────────────────────────┐
│                   DATABASE                              │
│  - H2 Database (Embedded)                               │
│  - File-based Persistence                               │
│  - JPA/Hibernate Entity Management                      │
└─────────────────────────────────────────────────────────┘
```

## Security Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 SECURITY LAYERS                         │
├─────────────────────────────────────────────────────────┤
│  1. Certificate ID Security                             │
│     - UUID-based generation                             │
│     - Non-sequential, non-guessable                     │
│     - Format: CERT-XXXX-XXXX                            │
├─────────────────────────────────────────────────────────┤
│  2. Verification System                                 │
│     - QR Code with embedded URL                         │
│     - Public verification endpoint                      │
│     - Status validation (Active/Revoked)                │
├─────────────────────────────────────────────────────────┤
│  3. File System Security                                │
│     - Isolated storage directories                      │
│     - Unique filenames                                  │
│     - No directory traversal                            │
├─────────────────────────────────────────────────────────┤
│  4. Email Security                                      │
│     - STARTTLS encryption                               │
│     - SMTP authentication                               │
│     - Environment variable configuration                │
├─────────────────────────────────────────────────────────┤
│  5. Input Validation                                    │
│     - Jakarta Bean Validation                           │
│     - File type validation                              │
│     - Size limits (10MB uploads)                        │
└─────────────────────────────────────────────────────────┘
```

## Data Flow - Certificate Lifecycle

```
1. CREATION
   ┌──────────────┐
   │ User Request │
   └──────┬───────┘
          ↓
   ┌──────────────┐      ┌──────────────┐
   │  Validation  │ ───→ │ Generate ID  │
   └──────┬───────┘      └──────┬───────┘
          ↓                     ↓
   ┌──────────────┐      ┌──────────────┐
   │ Select       │      │ Generate     │
   │ Template     │      │ QR Code      │
   └──────┬───────┘      └──────┬───────┘
          ↓                     ↓
   ┌──────────────┐      ┌──────────────┐
   │ Generate     │ ───→ │ Save to      │
   │ PDF          │      │ Database     │
   └──────┬───────┘      └──────┬───────┘
          ↓                     ↓
   ┌──────────────┐      ┌──────────────┐
   │ Save File    │      │ Send Email   │
   └──────────────┘      └──────────────┘

2. VERIFICATION
   ┌──────────────┐
   │ Scan QR Code │
   └──────┬───────┘
          ↓
   ┌──────────────┐
   │ Extract ID   │
   └──────┬───────┘
          ↓
   ┌──────────────┐
   │ Query DB     │
   └──────┬───────┘
          ↓
   ┌──────────────┐      ┌──────────────┐
   │ Found &      │      │ Not Found or │
   │ Active       │      │ Revoked      │
   └──────┬───────┘      └──────┬───────┘
          ↓                     ↓
   ┌──────────────┐      ┌──────────────┐
   │ Display      │      │ Display      │
   │ Details      │      │ Error        │
   └──────────────┘      └──────────────┘

3. REVOCATION
   ┌──────────────┐
   │ Admin Action │
   └──────┬───────┘
          ↓
   ┌──────────────┐
   │ Find by ID   │
   └──────┬───────┘
          ↓
   ┌──────────────┐
   │ Update Status│
   │ to REVOKED   │
   └──────┬───────┘
          ↓
   ┌──────────────┐
   │ Save to DB   │
   └──────────────┘
```

## Directory Structure

```
JavaProject/
├── certificates/           # Generated PDF files
│   ├── CERT-XXXX-XXXX.pdf
│   └── qr/                # QR code images
│       └── CERT-XXXX-XXXX_qr.png
├── templates/             # Uploaded template backgrounds
│   ├── background1.png
│   └── background2.pdf
├── data/                  # H2 Database files
│   ├── certificates.mv.db
│   └── certificates.trace.db
├── logs/                  # Application logs
│   └── application.log
└── src/                   # Source code
    ├── main/
    └── test/
```

## Scalability Considerations

### Current Architecture (Single Server)
```
┌─────────────────────┐
│   Spring Boot App   │
│  ┌──────────────┐   │
│  │   Web UI     │   │
│  ├──────────────┤   │
│  │   REST API   │   │
│  ├──────────────┤   │
│  │  Services    │   │
│  ├──────────────┤   │
│  │  Database    │   │
│  └──────────────┘   │
│   Local Storage     │
└─────────────────────┘
```

### Future Scalable Architecture
```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Load        │───→│ Spring Boot  │───→│   Database   │
│  Balancer    │    │  Instance 1  │    │  (MySQL/     │
└──────────────┘    └──────────────┘    │  PostgreSQL) │
       ↓            ┌──────────────┐    └──────────────┘
       └──────────→ │ Spring Boot  │           ↓
                    │  Instance 2  │    ┌──────────────┐
                    └──────────────┘    │ Cloud Storage│
                           ↓            │ (S3/Azure)   │
                    ┌──────────────┐    └──────────────┘
                    │ Email Queue  │
                    │ (RabbitMQ)   │
                    └──────────────┘
```

---

This architecture supports all 5 core functionalities with a clean separation of concerns, making it easy to maintain, test, and scale.
