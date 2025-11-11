package com.certificate.service;

import com.certificate.dto.CertificateRequest;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch Import Service for CSV/Excel files
 * Functionality #3: Recipient & Data Management - Batch Import
 */
@Service
@Slf4j
public class BatchImportService {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    };

    /**
     * Import certificates from CSV file
     */
    public List<CertificateRequest> importFromCsv(MultipartFile file) throws IOException, CsvException {
        log.info("Importing certificates from CSV file: {}", file.getOriginalFilename());
        
        List<CertificateRequest> requests = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            // First row is header
            String[] headers = rows.get(0);
            int nameIndex = findColumnIndex(headers, "name", "recipient_name", "recipient");
            int emailIndex = findColumnIndex(headers, "email", "recipient_email");
            int courseIndex = findColumnIndex(headers, "course", "course_name");
            int achievementIndex = findColumnIndex(headers, "achievement", "achievement_title", "title");
            int dateIndex = findColumnIndex(headers, "date", "completion_date", "completed_on");
            int issuerIndex = findColumnIndex(headers, "issuer", "issuer_name");
            int instructorIndex = findColumnIndex(headers, "instructor", "instructor_name");

            // Process data rows
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                
                if (row.length == 0 || row[0] == null || row[0].trim().isEmpty()) {
                    continue; // Skip empty rows
                }

                CertificateRequest request = CertificateRequest.builder()
                    .recipientName(getValueOrNull(row, nameIndex))
                    .recipientEmail(getValueOrNull(row, emailIndex))
                    .courseName(getValueOrNull(row, courseIndex))
                    .achievementTitle(getValueOrNull(row, achievementIndex))
                    .completionDate(parseDate(getValueOrNull(row, dateIndex)))
                    .issuerName(getValueOrNull(row, issuerIndex))
                    .instructorName(getValueOrNull(row, instructorIndex))
                    .sendEmail(emailIndex >= 0 && row.length > emailIndex && !row[emailIndex].trim().isEmpty())
                    .build();

                requests.add(request);
            }
        }

        log.info("Imported {} certificate requests from CSV", requests.size());
        return requests;
    }

    /**
     * Import certificates from Excel file
     */
    public List<CertificateRequest> importFromExcel(MultipartFile file) throws IOException {
        log.info("Importing certificates from Excel file: {}", file.getOriginalFilename());
        
        List<CertificateRequest> requests = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("Excel file is empty");
            }

            // Get header row
            Row headerRow = sheet.getRow(0);
            int nameIndex = findColumnIndex(headerRow, "name", "recipient_name", "recipient");
            int emailIndex = findColumnIndex(headerRow, "email", "recipient_email");
            int courseIndex = findColumnIndex(headerRow, "course", "course_name");
            int achievementIndex = findColumnIndex(headerRow, "achievement", "achievement_title", "title");
            int dateIndex = findColumnIndex(headerRow, "date", "completion_date", "completed_on");
            int issuerIndex = findColumnIndex(headerRow, "issuer", "issuer_name");
            int instructorIndex = findColumnIndex(headerRow, "instructor", "instructor_name");

            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellValueAsString(row, nameIndex);
                if (name == null || name.trim().isEmpty()) {
                    continue; // Skip rows without name
                }

                CertificateRequest request = CertificateRequest.builder()
                    .recipientName(name)
                    .recipientEmail(getCellValueAsString(row, emailIndex))
                    .courseName(getCellValueAsString(row, courseIndex))
                    .achievementTitle(getCellValueAsString(row, achievementIndex))
                    .completionDate(parseDate(getCellValueAsString(row, dateIndex)))
                    .issuerName(getCellValueAsString(row, issuerIndex))
                    .instructorName(getCellValueAsString(row, instructorIndex))
                    .sendEmail(emailIndex >= 0 && getCellValueAsString(row, emailIndex) != null)
                    .build();

                requests.add(request);
            }
        }

        log.info("Imported {} certificate requests from Excel", requests.size());
        return requests;
    }

    /**
     * Find column index by multiple possible header names
     */
    private int findColumnIndex(String[] headers, String... possibleNames) {
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].toLowerCase().trim();
            for (String name : possibleNames) {
                if (header.equals(name.toLowerCase()) || header.contains(name.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Find column index in Excel row
     */
    private int findColumnIndex(Row headerRow, String... possibleNames) {
        if (headerRow == null) return -1;
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            
            String header = cell.getStringCellValue().toLowerCase().trim();
            for (String name : possibleNames) {
                if (header.equals(name.toLowerCase()) || header.contains(name.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Get value from array safely
     */
    private String getValueOrNull(String[] row, int index) {
        if (index >= 0 && index < row.length) {
            String value = row[index].trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Row row, int columnIndex) {
        if (columnIndex < 0) return null;
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    /**
     * Parse date string to LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now();
        }

        // Try parsing as LocalDate first
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }

        // Try parsing as LocalDateTime and convert to LocalDate
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter).toLocalDate();
            } catch (Exception e) {
                // Try next formatter
            }
        }

        log.warn("Could not parse date: {}, using current date", dateStr);
        return LocalDate.now();
    }
}
