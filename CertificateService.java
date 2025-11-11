package com.certificate.service;

import com.certificate.dto.CertificateRequest;
import com.certificate.dto.CertificateResponse;
import com.certificate.model.Certificate;
import com.certificate.model.CertificateTemplate;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.CertificateTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main Certificate Service
 * Coordinates all certificate operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateTemplateRepository templateRepository;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;

    @Value("${certificate.verification.base-url}")
    private String verificationBaseUrl;

    /**
     * Generate a single certificate
     */
    @Transactional
    public CertificateResponse generateCertificate(CertificateRequest request) throws IOException {
        log.info("Generating certificate for {}", request.getRecipientName());

        // Get template
        CertificateTemplate template = getTemplate(request.getTemplateId());

        // Create certificate entity
        Certificate certificate = buildCertificate(request, template);

        // Generate unique certificate ID
        certificate.setCertificateId(generateUniqueCertificateId());

        // Generate PDF
        String pdfPath = pdfGenerationService.generateCertificatePdf(certificate, template);
        certificate.setFilePath(pdfPath);

        // Generate QR code
        String qrPath = pdfGenerationService.generateQRCode(certificate.getCertificateId());
        certificate.setQrCodePath(qrPath);

        // Save to database
        certificate = certificateRepository.save(certificate);

        // Send email if requested
        if (request.isSendEmail() && request.getRecipientEmail() != null) {
            try {
                emailService.sendCertificateEmail(certificate);
                certificate.setEmailSent(true);
                certificate.setEmailSentDate(LocalDateTime.now());
                certificateRepository.save(certificate);
            } catch (Exception e) {
                log.error("Failed to send email for certificate {}", certificate.getCertificateId(), e);
            }
        }

        log.info("Certificate generated successfully: {}", certificate.getCertificateId());
        return convertToResponse(certificate);
    }

    /**
     * Generate multiple certificates from batch
     */
    @Transactional
    public List<CertificateResponse> generateBatchCertificates(List<CertificateRequest> requests) {
        return requests.stream()
            .map(request -> {
                try {
                    return generateCertificate(request);
                } catch (IOException e) {
                    log.error("Failed to generate certificate for {}", request.getRecipientName(), e);
                    return null;
                }
            })
            .filter(response -> response != null)
            .collect(Collectors.toList());
    }

    /**
     * Verify certificate by ID
     */
    public Optional<Certificate> verifyCertificate(String certificateId) {
        return certificateRepository.findByCertificateId(certificateId)
            .filter(cert -> cert.getStatus() == Certificate.CertificateStatus.ACTIVE);
    }

    /**
     * Get all certificates
     */
    public List<CertificateResponse> getAllCertificates() {
        return certificateRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get certificates by recipient email
     */
    public List<CertificateResponse> getCertificatesByEmail(String email) {
        return certificateRepository.findByRecipientEmail(email).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get certificate by ID
     */
    public Optional<CertificateResponse> getCertificateById(Long id) {
        return certificateRepository.findById(id)
            .map(this::convertToResponse);
    }

    /**
     * Revoke a certificate
     */
    @Transactional
    public void revokeCertificate(String certificateId) {
        certificateRepository.findByCertificateId(certificateId)
            .ifPresent(certificate -> {
                certificate.setStatus(Certificate.CertificateStatus.REVOKED);
                certificateRepository.save(certificate);
                log.info("Certificate revoked: {}", certificateId);
            });
    }

    /**
     * Generate unique certificate ID
     */
    private String generateUniqueCertificateId() {
        String certificateId;
        do {
            // Format: CERT-XXXX-XXXX
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            certificateId = String.format("CERT-%s-%s", 
                uuid.substring(0, 4), 
                uuid.substring(4, 8));
        } while (certificateRepository.existsByCertificateId(certificateId));
        
        return certificateId;
    }

    /**
     * Get template (default if not specified)
     */
    private CertificateTemplate getTemplate(Long templateId) {
        if (templateId != null) {
            return templateRepository.findById(templateId).orElse(getDefaultTemplate());
        }
        return getDefaultTemplate();
    }

    /**
     * Get default template
     */
    private CertificateTemplate getDefaultTemplate() {
        return templateRepository.findByIsDefaultTrue().stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * Build certificate from request
     */
    private Certificate buildCertificate(CertificateRequest request, CertificateTemplate template) {
        LocalDateTime completionDateTime;
        if (request.getCompletionDate() != null) {
            completionDateTime = request.getCompletionDate().atStartOfDay();
        } else {
            completionDateTime = LocalDateTime.now();
        }
        
        return Certificate.builder()
            .recipientName(request.getRecipientName())
            .recipientEmail(request.getRecipientEmail())
            .courseName(request.getCourseName())
            .achievementTitle(request.getAchievementTitle())
            .completionDate(completionDateTime)
            .issuerName(request.getIssuerName())
            .instructorName(request.getInstructorName())
            .template(template)
            .emailSent(false)
            .status(Certificate.CertificateStatus.ACTIVE)
            .build();
    }

    /**
     * Convert entity to response DTO
     */
    private CertificateResponse convertToResponse(Certificate certificate) {
        return CertificateResponse.builder()
            .id(certificate.getId())
            .certificateId(certificate.getCertificateId())
            .recipientName(certificate.getRecipientName())
            .recipientEmail(certificate.getRecipientEmail())
            .courseName(certificate.getCourseName())
            .achievementTitle(certificate.getAchievementTitle())
            .completionDate(certificate.getCompletionDate())
            .issuerName(certificate.getIssuerName())
            .instructorName(certificate.getInstructorName())
            .issuedDate(certificate.getIssuedDate())
            .emailSent(certificate.isEmailSent())
            .downloadUrl("/api/certificates/" + certificate.getId() + "/download")
            .verificationUrl(verificationBaseUrl + "/" + certificate.getCertificateId())
            .status(certificate.getStatus().name())
            .build();
    }
}
