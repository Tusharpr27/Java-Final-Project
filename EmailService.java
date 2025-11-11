package com.certificate.service;

import com.certificate.model.Certificate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Email Service for certificate delivery
 * Functionality #4: Issuing & Delivery
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${certificate.email.from}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    /**
     * Send certificate via email
     */
    @Async
    public void sendCertificateEmail(Certificate certificate) throws MessagingException {
        log.info("Sending certificate email to {}", certificate.getRecipientEmail());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(certificate.getRecipientEmail());
        helper.setSubject("Your Certificate - " + certificate.getCourseName());

        String emailContent = buildEmailContent(certificate);
        helper.setText(emailContent, true);

        // Attach PDF
        FileSystemResource file = new FileSystemResource(new File(certificate.getFilePath()));
        helper.addAttachment(certificate.getCertificateId() + ".pdf", file);

        mailSender.send(message);
        log.info("Certificate email sent successfully to {}", certificate.getRecipientEmail());
    }

    /**
     * Build HTML email content
     */
    private String buildEmailContent(Certificate certificate) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0066cc; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .button { display: inline-block; padding: 10px 20px; background-color: #0066cc; 
                              color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                    .details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 Congratulations, %s!</h1>
                    </div>
                    <div class="content">
                        <p>We are pleased to inform you that you have successfully completed:</p>
                        <div class="details">
                            <strong>Course:</strong> %s<br>
                            <strong>Completion Date:</strong> %s<br>
                            <strong>Certificate ID:</strong> %s
                        </div>
                        <p>Your official certificate is attached to this email. You can also verify your certificate 
                           online at any time.</p>
                        <p>Keep this certificate safe as proof of your achievement!</p>
                        <p style="margin-top: 30px;">
                            <strong>Best regards,</strong><br>
                            %s
                        </p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>Certificate ID: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            certificate.getRecipientName(),
            certificate.getCourseName(),
            certificate.getCompletionDate().format(DATE_FORMATTER),
            certificate.getCertificateId(),
            certificate.getIssuerName() != null ? certificate.getIssuerName() : "Certificate Authority",
            certificate.getCertificateId()
        );
    }

    /**
     * Send batch notification email
     */
    @Async
    public void sendBatchNotificationEmail(String adminEmail, int totalCertificates, int successCount) 
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(adminEmail);
        helper.setSubject("Batch Certificate Generation Complete");

        String content = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Batch Certificate Generation Summary</h2>
                <p><strong>Total Certificates Requested:</strong> %d</p>
                <p><strong>Successfully Generated:</strong> %d</p>
                <p><strong>Failed:</strong> %d</p>
                <p>All generated certificates have been emailed to their respective recipients.</p>
            </body>
            </html>
            """, totalCertificates, successCount, totalCertificates - successCount);

        helper.setText(content, true);
        mailSender.send(message);
    }
}
