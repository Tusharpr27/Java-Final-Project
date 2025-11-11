package com.certificate.service;

import com.certificate.model.Certificate;
import com.certificate.model.CertificateTemplate;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Core PDF Generation Service
 * Functionality #1: Core Generation Engine
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    @Value("${certificate.storage.path}")
    private String storagePath;

    @Value("${certificate.verification.base-url}")
    private String verificationBaseUrl;

    private static final int QR_CODE_SIZE = 150;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    /**
     * Generate PDF certificate with QR code
     */
    public String generateCertificatePdf(Certificate certificate, CertificateTemplate template) throws IOException {
        try {
            // Ensure storage directory exists
            Path certificateDir = Paths.get(storagePath);
            if (!Files.exists(certificateDir)) {
                Files.createDirectories(certificateDir);
                log.info("Created certificate storage directory: {}", certificateDir);
            }

            // Generate unique filename
            String fileName = certificate.getCertificateId() + ".pdf";
            String filePath = Paths.get(storagePath, fileName).toString();

            // Generate QR code first
            String qrCodePath = generateQRCode(certificate.getCertificateId());

            // Create PDF
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc)) {

                // Set page size to A4 landscape
                pdfDoc.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4.rotate());

                // Add background if exists (skip if file doesn't exist)
                if (template != null && template.getBackgroundPath() != null) {
                    try {
                        addBackgroundImage(document, template.getBackgroundPath());
                    } catch (Exception e) {
                        log.warn("Could not add background image, continuing without it: {}", e.getMessage());
                    }
                }

                // Add certificate content
                addCertificateContent(document, certificate);

                // Add QR code
                if (qrCodePath != null) {
                    addQRCodeToDocument(document, qrCodePath);
                }
            }

            log.info("Certificate PDF generated: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("Error generating PDF for certificate {}", certificate.getCertificateId(), e);
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Add background image to PDF
     */
    private void addBackgroundImage(Document document, String backgroundPath) throws IOException {
        File bgFile = new File(backgroundPath);
        if (bgFile.exists()) {
            Image background = new Image(ImageDataFactory.create(backgroundPath));
            background.setFixedPosition(0, 0);
            background.scaleToFit(document.getPdfDocument().getDefaultPageSize().getWidth(),
                                  document.getPdfDocument().getDefaultPageSize().getHeight());
            document.add(background);
        }
    }

    /**
     * Add certificate text content
     */
    private void addCertificateContent(Document document, Certificate certificate) throws IOException {
        PdfFont font = PdfFontFactory.createFont();
        PdfFont boldFont = PdfFontFactory.createFont(
            com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

        // Certificate Title
        Paragraph title = new Paragraph("CERTIFICATE OF ACHIEVEMENT")
            .setFont(boldFont)
            .setFontSize(32)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(100)
            .setFontColor(new DeviceRgb(0, 51, 102));
        document.add(title);

        // Presented to
        Paragraph presentedTo = new Paragraph("This certificate is proudly presented to")
            .setFont(font)
            .setFontSize(16)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30);
        document.add(presentedTo);

        // Recipient Name
        Paragraph recipientName = new Paragraph(certificate.getRecipientName())
            .setFont(boldFont)
            .setFontSize(36)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20)
            .setFontColor(new DeviceRgb(0, 102, 204));
        document.add(recipientName);

        // Achievement
        String achievementText = certificate.getAchievementTitle() != null 
            ? certificate.getAchievementTitle()
            : "For successfully completing " + certificate.getCourseName();
        
        Paragraph achievement = new Paragraph(achievementText)
            .setFont(font)
            .setFontSize(18)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30);
        document.add(achievement);

        // Completion Date
        if (certificate.getCompletionDate() != null) {
            Paragraph completionDate = new Paragraph(
                "Completed on " + certificate.getCompletionDate().format(DATE_FORMATTER))
                .setFont(font)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
            document.add(completionDate);
        }

        // Issuer/Instructor
        if (certificate.getIssuerName() != null || certificate.getInstructorName() != null) {
            String issuerText = certificate.getInstructorName() != null 
                ? certificate.getInstructorName() 
                : certificate.getIssuerName();
            
            Paragraph issuer = new Paragraph("___________________\n" + issuerText)
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(60);
            document.add(issuer);
        }

        // Certificate ID
        Paragraph certId = new Paragraph("Certificate ID: " + certificate.getCertificateId())
            .setFont(font)
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(40)
            .setFontColor(new DeviceRgb(128, 128, 128));
        document.add(certId);
    }

    /**
     * Add QR code to document
     */
    private void addQRCodeToDocument(Document document, String qrCodePath) throws IOException {
        Image qrImage = new Image(ImageDataFactory.create(qrCodePath));
        qrImage.setFixedPosition(50, 50);
        qrImage.scaleToFit(QR_CODE_SIZE, QR_CODE_SIZE);
        document.add(qrImage);
    }

    /**
     * Generate QR code for certificate verification
     */
    public String generateQRCode(String certificateId) {
        try {
            Path qrDir = Paths.get(storagePath, "qr");
            if (!Files.exists(qrDir)) {
                Files.createDirectories(qrDir);
            }

            String verificationUrl = verificationBaseUrl + "/" + certificateId;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(verificationUrl, BarcodeFormat.QR_CODE, 
                                                      QR_CODE_SIZE, QR_CODE_SIZE);

            String qrFileName = certificateId + "_qr.png";
            Path qrFilePath = Paths.get(storagePath, "qr", qrFileName);
            
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrFilePath);
            
            log.info("QR code generated: {}", qrFilePath);
            return qrFilePath.toString();
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for certificate {}", certificateId, e);
            return null;
        }
    }

    /**
     * Generate PNG version of certificate
     */
    public String generateCertificatePng(String pdfPath) {
        // This is a simplified version - in production, you'd use a library like Apache PDFBox
        // to convert PDF to PNG
        log.info("PNG generation would convert: {}", pdfPath);
        return pdfPath.replace(".pdf", ".png");
    }
}
