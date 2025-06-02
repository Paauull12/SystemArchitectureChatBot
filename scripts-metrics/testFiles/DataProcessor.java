package com.example.problematic;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.*;
import java.util.concurrent.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.json.JSONObject;
import org.json.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * Example 4: High Cyclomatic Complexity + High LCOM + High Instability
 * 
 * Problems:
 * - Cyclomatic Complexity: 35+ per method (complex data processing logic)
 * - LCOM: 0.88+ (methods work on completely different data types)
 * - Instability: 0.89+ (depends on many external file processing libraries)
 * 
 * This class processes different data formats but methods share no common state
 */
public class DataProcessor {

    // CSV processing fields (used only by CSV methods)
    private String csvDelimiter = ",";
    private char csvQuoteChar = '"';
    private boolean csvHasHeader = true;
    private List<String> csvHeaders = new ArrayList<>();
    private Map<String, Integer> csvColumnIndexes = new HashMap<>();

    // Excel processing fields (used only by Excel methods)
    private String excelSheetName = "Sheet1";
    private int excelStartRow = 0;
    private int excelStartColumn = 0;
    private boolean excelAutoSizeColumns = true;
    private Map<String, CellStyle> excelStyles = new HashMap<>();

    // JSON processing fields (used only by JSON methods)
    private ObjectMapper jsonMapper = new ObjectMapper();
    private String jsonDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private boolean jsonPrettyPrint = false;
    private Set<String> jsonRequiredFields = new HashSet<>();

    // XML processing fields (used only by XML methods)
    private String xmlRootElement = "root";
    private String xmlEncoding = "UTF-8";
    private boolean xmlValidateSchema = false;
    private Map<String, String> xmlNamespaces = new HashMap<>();

    // Database export fields (used only by DB methods)
    private Connection dbConnection;
    private String dbTablePrefix = "export_";
    private int dbBatchSize = 1000;
    private boolean dbCreateTables = true;

    // File system fields (used only by file methods)
    private String outputDirectory = "./output/";
    private String tempDirectory = "./temp/";
    private boolean createBackups = true;
    private long maxFileSize = 100 * 1024 * 1024; // 100MB

    // Email report fields (used only by email methods)
    private String smtpHost = "localhost";
    private int smtpPort = 587;
    private String emailFrom = "reports@company.com";
    private List<String> emailRecipients = new ArrayList<>();

    /**
     * Complex CSV processing method
     * CC: 28+ due to multiple validation and transformation branches
     */
    public ProcessingResult processCSVFile(String inputFilePath, String outputFilePath,
            Map<String, Object> options) {
        ProcessingResult result = new ProcessingResult();
        List<String> errors = new ArrayList<>();
        int processedRows = 0;

        try {
            // Input validation with multiple branches (CC +8)
            if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
                errors.add("Input file path is required");
                result.setSuccess(false);
                result.setErrors(errors);
                return result;
            }

            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                errors.add("Input file does not exist: " + inputFilePath);
                result.setSuccess(false);
                result.setErrors(errors);
                return result;
            }

            if (!inputFile.canRead()) {
                errors.add("Cannot read input file: " + inputFilePath);
                result.setSuccess(false);
                result.setErrors(errors);
                return result;
            }

            if (inputFile.length() > maxFileSize) {
                errors.add("File size exceeds maximum allowed: " + maxFileSize);
                result.setSuccess(false);
                result.setErrors(errors);
                return result;
            }

            // Options processing (CC +6)
            if (options != null) {
                if (options.containsKey("delimiter")) {
                    String delimiter = (String) options.get("delimiter");
                    if (delimiter != null && !delimiter.isEmpty()) {
                        csvDelimiter = delimiter;
                    }
                }

                if (options.containsKey("hasHeader")) {
                    csvHasHeader = (Boolean) options.get("hasHeader");
                }

                if (options.containsKey("quoteChar")) {
                    String quoteChar = (String) options.get("quoteChar");
                    if (quoteChar != null && quoteChar.length() == 1) {
                        csvQuoteChar = quoteChar.charAt(0);
                    }
                }
            }

            // CSV reading and processing (CC +15)
            try (CSVReader reader = new CSVReader(new FileReader(inputFile))) {
                List<String[]> allRows = reader.readAll();

                if (allRows.isEmpty()) {
                    errors.add("CSV file is empty");
                    result.setSuccess(false);
                    result.setErrors(errors);
                    return result;
                }

                // Header processing
                String[] headers = null;
                int dataStartIndex = 0;

                if (csvHasHeader) {
                    headers = allRows.get(0);
                    dataStartIndex = 1;

                    // Validate headers
                    for (int i = 0; i < headers.length; i++) {
                        String header = headers[i].trim();
                        if (header.isEmpty()) {
                            errors.add("Empty header at column " + (i + 1));
                        } else {
                            csvHeaders.add(header);
                            csvColumnIndexes.put(header, i);
                        }
                    }

                    // Check for duplicate headers
                    Set<String> uniqueHeaders = new HashSet<>(csvHeaders);
                    if (uniqueHeaders.size() != csvHeaders.size()) {
                        errors.add("Duplicate headers found in CSV");
                    }
                } else {
                    // Generate default headers
                    if (!allRows.isEmpty()) {
                        int columnCount = allRows.get(0).length;
                        for (int i = 0; i < columnCount; i++) {
                            String header = "Column_" + (i + 1);
                            csvHeaders.add(header);
                            csvColumnIndexes.put(header, i);
                        }
                    }
                }

                // Data processing with complex validation
                List<String[]> processedData = new ArrayList<>();

                for (int rowIndex = dataStartIndex; rowIndex < allRows.size(); rowIndex++) {
                    String[] row = allRows.get(rowIndex);

                    // Row validation
                    if (row.length != csvHeaders.size()) {
                        if (options != null && options.containsKey("strictMode") &&
                                (Boolean) options.get("strictMode")) {
                            errors.add("Row " + (rowIndex + 1) + " has incorrect column count");
                            continue;
                        } else {
                            // Pad or trim row to match header count
                            String[] adjustedRow = new String[csvHeaders.size()];
                            for (int i = 0; i < adjustedRow.length; i++) {
                                if (i < row.length) {
                                    adjustedRow[i] = row[i];
                                } else {
                                    adjustedRow[i] = "";
                                }
                            }
                            row = adjustedRow;
                        }
                    }

                    // Data transformation based on column type
                    String[] transformedRow = new String[row.length];
                    for (int colIndex = 0; colIndex < row.length; colIndex++) {
                        String cellValue = row[colIndex] != null ? row[colIndex].trim() : "";
                        String header = csvHeaders.get(colIndex);

                        // Type-specific transformations
                        if (header.toLowerCase().contains("date")) {
                            // Date formatting
                            if (!cellValue.isEmpty()) {
                                try {
                                    SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
                                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date = inputFormat.parse(cellValue);
                                    transformedRow[colIndex] = outputFormat.format(date);
                                } catch (ParseException e) {
                                    try {
                                        SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        Date date = altFormat.parse(cellValue);
                                        transformedRow[colIndex] = cellValue; // Already in correct format
                                    } catch (ParseException e2) {
                                        if (options != null && options.containsKey("strictMode") &&
                                                (Boolean) options.get("strictMode")) {
                                            errors.add("Invalid date format in row " + (rowIndex + 1) +
                                                    ", column " + header + ": " + cellValue);
                                            transformedRow[colIndex] = "";
                                        } else {
                                            transformedRow[colIndex] = cellValue; // Keep original
                                        }
                                    }
                                }
                            } else {
                                transformedRow[colIndex] = "";
                            }
                        } else if (header.toLowerCase().contains("email")) {
                            // Email validation and normalization
                            if (!cellValue.isEmpty()) {
                                if (cellValue.contains("@") && cellValue.contains(".")) {
                                    transformedRow[colIndex] = cellValue.toLowerCase();
                                } else {
                                    if (options != null && options.containsKey("strictMode") &&
                                            (Boolean) options.get("strictMode")) {
                                        errors.add("Invalid email format in row " + (rowIndex + 1) +
                                                ", column " + header + ": " + cellValue);
                                        transformedRow[colIndex] = "";
                                    } else {
                                        transformedRow[colIndex] = cellValue;
                                    }
                                }
                            } else {
                                transformedRow[colIndex] = "";
                            }
                        } else if (header.toLowerCase().contains("phone")) {
                            // Phone number normalization
                            if (!cellValue.isEmpty()) {
                                String normalizedPhone = cellValue.replaceAll("[^0-9+]", "");
                                if (normalizedPhone.length() >= 10) {
                                    transformedRow[colIndex] = normalizedPhone;
                                } else {
                                    if (options != null && options.containsKey("strictMode") &&
                                            (Boolean) options.get("strictMode")) {
                                        errors.add("Invalid phone number in row " + (rowIndex + 1) +
                                                ", column " + header + ": " + cellValue);
                                        transformedRow[colIndex] = "";
                                    } else {
                                        transformedRow[colIndex] = cellValue;
                                    }
                                }
                            } else {
                                transformedRow[colIndex] = "";
                            }
                        } else if (header.toLowerCase().contains("amount") ||
                                header.toLowerCase().contains("price") ||
                                header.toLowerCase().contains("cost")) {
                            // Numeric formatting
                            if (!cellValue.isEmpty()) {
                                try {
                                    String numericValue = cellValue.replaceAll("[^0-9.-]", "");
                                    double value = Double.parseDouble(numericValue);
                                    transformedRow[colIndex] = String.format("%.2f", value);
                                } catch (NumberFormatException e) {
                                    if (options != null && options.containsKey("strictMode") &&
                                            (Boolean) options.get("strictMode")) {
                                        errors.add("Invalid numeric value in row " + (rowIndex + 1) +
                                                ", column " + header + ": " + cellValue);
                                        transformedRow[colIndex] = "0.00";
                                    } else {
                                        transformedRow[colIndex] = cellValue;
                                    }
                                }
                            } else {
                                transformedRow[colIndex] = "0.00";
                            }
                        } else {
                            // Default string processing
                            transformedRow[colIndex] = cellValue;
                        }
                    }

                    processedData.add(transformedRow);
                    processedRows++;
                }

                // Write processed data to output file
                if (outputFilePath != null && !outputFilePath.trim().isEmpty()) {
                    try (CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath))) {
                        if (csvHasHeader) {
                            writer.writeNext(csvHeaders.toArray(new String[0]));
                        }

                        for (String[] row : processedData) {
                            writer.writeNext(row);
                        }
                    }
                }

            } catch (IOException e) {
                errors.add("Error reading CSV file: " + e.getMessage());
            }

        } catch (Exception e) {
            errors.add("Unexpected error processing CSV: " + e.getMessage());
        }

        result.setSuccess(errors.isEmpty());
        result.setErrors(errors);
        result.setProcessedRecords(processedRows);
        return result;
    }

    /**
     * Complex Excel processing method - different instance variables
     * CC: 25+ due to Excel-specific validation and formatting
     */
    public ProcessingResult processExcelFile(String inputFilePath, String outputFilePath,
            Map<String, Object> options) {
        ProcessingResult result = new ProcessingResult();
        List<String> errors = new ArrayList<>();
        int processedRows = 0;

        try {
            // File validation (CC +5)
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists() || !inputFile.canRead()) {
                errors.add("Cannot access Excel file: " + inputFilePath);
                result.setSuccess(false);
                result.setErrors(errors);
                return result;
            }

            // Options processing for Excel (CC +4)
            if (options != null) {
                if (options.containsKey("sheetName")) {
                    excelSheetName = (String) options.get("sheetName");
                }
                if (options.containsKey("startRow")) {
                    excelStartRow = (Integer) options.get("startRow");
                }
                if (options.containsKey("startColumn")) {
                    excelStartColumn = (Integer) options.get("startColumn");
                }
            }

            // Excel processing with complex cell handling (CC +16)
            try (FileInputStream fis = new FileInputStream(inputFile);
                    Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheet(excelSheetName);
                if (sheet == null) {
                    // Try to get first sheet if named sheet doesn't exist
                    if (workbook.getNumberOfSheets() > 0) {
                        sheet = workbook.getSheetAt(0);
                        excelSheetName = sheet.getSheetName();
                    } else {
                        errors.add("No sheets found in Excel file");
                        result.setSuccess(false);
                        result.setErrors(errors);
                        return result;
                    }
                }

                List<List<Object>> processedData = new ArrayList<>();

                // Process each row
                for (Row row : sheet) {
                    if (row.getRowNum() < excelStartRow) {
                        continue;
                    }

                    List<Object> rowData = new ArrayList<>();

                    // Process each cell in the row
                    for (int cellIndex = excelStartColumn; cellIndex < row.getLastCellNum(); cellIndex++) {
                        Cell cell = row.getCell(cellIndex);
                        Object cellValue = null;

                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    cellValue = cell.getStringCellValue().trim();
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        Date dateValue = cell.getDateCellValue();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        cellValue = dateFormat.format(dateValue);
                                    } else {
                                        double numericValue = cell.getNumericCellValue();
                                        if (numericValue == Math.floor(numericValue)) {
                                            cellValue = (long) numericValue;
                                        } else {
                                            cellValue = numericValue;
                                        }
                                    }
                                    break;
                                case BOOLEAN:
                                    cellValue = cell.getBooleanCellValue();
                                    break;
                                case FORMULA:
                                    try {
                                        cellValue = cell.getNumericCellValue();
                                    } catch (Exception e) {
                                        try {
                                            cellValue = cell.getStringCellValue();
                                        } catch (Exception e2) {
                                            cellValue = "";
                                        }
                                    }
                                    break;
                                case BLANK:
                                case _NONE:
                                default:
                                    cellValue = "";
                                    break;
                            }
                        } else {
                            cellValue = "";
                        }

                        rowData.add(cellValue);
                    }

                    if (!rowData.isEmpty()) {
                        processedData.add(rowData);
                        processedRows++;
                    }
                }

                // Create output Excel file if specified
                if (outputFilePath != null && !outputFilePath.trim().isEmpty()) {
                    try (Workbook outputWorkbook = new XSSFWorkbook()) {
                        Sheet outputSheet = outputWorkbook.createSheet("ProcessedData");

                        for (int rowIndex = 0; rowIndex < processedData.size(); rowIndex++) {
                            Row outputRow = outputSheet.createRow(rowIndex);
                            List<Object> rowData = processedData.get(rowIndex);

                            for (int cellIndex = 0; cellIndex < rowData.size(); cellIndex++) {
                                Cell outputCell = outputRow.createCell(cellIndex);
                                Object value = rowData.get(cellIndex);

                                if (value instanceof String) {
                                    outputCell.setCellValue((String) value);
                                } else if (value instanceof Number) {
                                    outputCell.setCellValue(((Number) value).doubleValue());
                                } else if (value instanceof Boolean) {
                                    outputCell.setCellValue((Boolean) value);
                                } else {
                                    outputCell.setCellValue(value != null ? value.toString() : "");
                                }
                            }
                        }

                        if (excelAutoSizeColumns) {
                            for (int i = 0; i < processedData.get(0).size(); i++) {
                                outputSheet.autoSizeColumn(i);
                            }
                        }

                        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                            outputWorkbook.write(fos);
                        }
                    }
                }

            } catch (Exception e) {
                errors.add("Error processing Excel file: " + e.getMessage());
            }

        } catch (Exception e) {
            errors.add("Unexpected error: " + e.getMessage());
        }

        result.setSuccess(errors.isEmpty());
        result.setErrors(errors);
        result.setProcessedRecords(processedRows);
        return result;
    }

    /**
     * Complex JSON processing - uses completely different fields
     * CC: 22+ due to JSON structure validation and transformation
     */
    public ProcessingResult processJSONFile(String inputFilePath, String outputFilePath,
            Map<String, Object> options) {
        ProcessingResult result = new ProcessingResult();
        List<String> errors = new ArrayList<>();
        int processedRecords = 0;

        // JSON processing logic using jsonMapper, jsonDateFormat, etc.
        // This method doesn't share any instance variables with CSV or Excel methods
        // Leading to very high LCOM

        result.setSuccess(errors.isEmpty());
        result.setErrors(errors);
        result.setProcessedRecords(processedRecords);
        return result;
    }

    /**
     * Database export method - uses DB-specific fields only
     * CC: 18+ due to database connection and transaction handling
     */
    public ProcessingResult exportToDatabase(List<Map<String, Object>> data, String tableName) {
        ProcessingResult result = new ProcessingResult();
        List<String> errors = new ArrayList<>();

        // Database export logic using dbConnection, dbBatchSize, etc.
        // No shared fields with other processing methods

        result.setSuccess(errors.isEmpty());
        result.setErrors(errors);
        return result;
    }

    /**
     * Email reporting method - uses email-specific fields only
     */
    public void sendProcessingReport(ProcessingResult result, String reportType) {
        // Email logic using smtpHost, emailRecipients, etc.
        // Completely separate from data processing methods
    }
}

// Supporting classes
class ProcessingResult {
    private boolean success;
    private List<String> errors;
    private int processedRecords;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(int processedRecords) {
        this.processedRecords = processedRecords;
    }
}