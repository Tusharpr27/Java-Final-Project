package com.certificate.service;

import com.certificate.model.CertificateTemplate;
import com.certificate.repository.CertificateTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Template Management Service
 * Functionality #2: Template Management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final CertificateTemplateRepository templateRepository;

    @Value("${certificate.template.path}")
    private String templatePath;

    /**
     * Get all templates
     */
    public List<CertificateTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    /**
     * Get template by ID
     */
    public Optional<CertificateTemplate> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    /**
     * Get default template
     */
    public Optional<CertificateTemplate> getDefaultTemplate() {
        return templateRepository.findByIsDefaultTrue().stream().findFirst();
    }

    /**
     * Create new template
     */
    @Transactional
    public CertificateTemplate createTemplate(String name, String description, boolean isDefault) {
        CertificateTemplate template = CertificateTemplate.builder()
            .name(name)
            .description(description)
            .isDefault(isDefault)
            .build();

        if (isDefault) {
            // Remove default flag from other templates
            templateRepository.findByIsDefaultTrue().forEach(t -> {
                t.setDefault(false);
                templateRepository.save(t);
            });
        }

        return templateRepository.save(template);
    }

    /**
     * Upload template background
     */
    @Transactional
    public CertificateTemplate uploadTemplateBackground(Long templateId, MultipartFile file) throws IOException {
        CertificateTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        // Ensure template directory exists
        Path templateDir = Paths.get(templatePath);
        if (!Files.exists(templateDir)) {
            Files.createDirectories(templateDir);
        }

        // Determine file type
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        
        CertificateTemplate.BackgroundType backgroundType = switch (extension) {
            case "pdf" -> CertificateTemplate.BackgroundType.PDF;
            case "svg" -> CertificateTemplate.BackgroundType.SVG;
            case "png" -> CertificateTemplate.BackgroundType.PNG;
            case "jpg", "jpeg" -> CertificateTemplate.BackgroundType.JPEG;
            default -> throw new IllegalArgumentException("Unsupported file type: " + extension);
        };

        // Save file
        String filename = UUID.randomUUID() + "." + extension;
        Path filePath = Paths.get(templatePath, filename);
        Files.write(filePath, file.getBytes());

        // Update template
        template.setBackgroundPath(filePath.toString());
        template.setBackgroundType(backgroundType);

        log.info("Template background uploaded: {}", filename);
        return templateRepository.save(template);
    }

    /**
     * Update template field configuration
     */
    @Transactional
    public CertificateTemplate updateTemplateConfiguration(Long templateId, String fieldConfiguration) {
        CertificateTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        template.setFieldConfiguration(fieldConfiguration);
        return templateRepository.save(template);
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        CertificateTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        // Delete background file if exists
        if (template.getBackgroundPath() != null) {
            try {
                Files.deleteIfExists(Paths.get(template.getBackgroundPath()));
            } catch (IOException e) {
                log.error("Failed to delete template background file", e);
            }
        }

        templateRepository.delete(template);
        log.info("Template deleted: {}", templateId);
    }

    /**
     * Set template as default
     */
    @Transactional
    public void setDefaultTemplate(Long templateId) {
        // Remove default flag from all templates
        templateRepository.findByIsDefaultTrue().forEach(t -> {
            t.setDefault(false);
            templateRepository.save(t);
        });

        // Set new default
        templateRepository.findById(templateId).ifPresent(t -> {
            t.setDefault(true);
            templateRepository.save(t);
            log.info("Template {} set as default", templateId);
        });
    }

    /**
     * Initialize default templates
     */
    @Transactional
    public void initializeDefaultTemplates() {
        if (templateRepository.count() == 0) {
            CertificateTemplate defaultTemplate = CertificateTemplate.builder()
                .name("Classic Certificate")
                .description("Professional classic certificate design")
                .backgroundPath("templates/default-background.pdf")
                .backgroundType(CertificateTemplate.BackgroundType.PDF)
                .isDefault(true)
                .build();
            
            templateRepository.save(defaultTemplate);
            log.info("Default template initialized");
        }
    }
}
