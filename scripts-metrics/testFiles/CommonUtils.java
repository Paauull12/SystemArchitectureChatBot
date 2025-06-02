package com.example.short;

import java.util.*;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Short Example 2: High LCOM + High Afferent Coupling
 * Problems: LCOM: 0.95+, Ca: 40+ (everyone depends on this utility)
 */
public class CommonUtils {
    
    // String processing fields (used only by string methods)
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Map<String, String> HTML_ENTITIES = new HashMap<>();
    
    // Date processing fields (used only by date methods)
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    
    // Security fields (used only by security methods)
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SALT_PREFIX = "APP_SALT_";
    
    // Math fields (used only by math methods)
    private static final double PI_CONSTANT = 3.14159265359;
    private static final Random mathRandom = new Random();
    
    // File processing fields (used only by file methods)
    private static final String TEMP_DIR = "/tmp/";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".txt", ".pdf", ".jpg");
    
    static {
        HTML_ENTITIES.put("&", "&amp;");
        HTML_ENTITIES.put("<", "&lt;");
        HTML_ENTITIES.put(">", "&gt;");
    }
    
    // String utility methods - HIGH Ca (called by 15+ classes)
    public static String formatString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }
    
    public static String escapeHtml(String input) {
        if (input == null) return "";
        String result = input;
        for (Map.Entry<String, String> entry : HTML_ENTITIES.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static String capitalize(String input) {
        if (isEmpty(input)) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
    
    // Date utility methods - HIGH Ca (called by 12+ classes)
    // No shared fields with string methods
    public static String formatDate(Date date) {
        if (date == null) return "";
        synchronized (dateFormatter) {
            return dateFormatter.format(date);
        }
    }
    
    public static Date parseDate(String dateStr) {
        if (isEmpty(dateStr)) return null;
        try {
            synchronized (dateFormatter) {
                return dateFormatter.parse(dateStr);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isWeekend(Date date) {
        if (date == null) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }
    
    // Security utility methods - HIGH Ca (called by 8+ classes)
    // No shared fields with other method groups
    public static String hashPassword(String password) {
        if (isEmpty(password)) return "";
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            String saltedPassword = SALT_PREFIX + password;
            byte[] hash = md.digest(saltedPassword.getBytes(DEFAULT_ENCODING));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "";
        }
    }
    
    public static String generateToken() {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            String input = SALT_PREFIX + System.currentTimeMillis();
            byte[] hash = md.digest(input.getBytes(DEFAULT_ENCODING));
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            return UUID.randomUUID().toString().substring(0, 16);
        }
    }
    
    // Math utility methods - HIGH Ca (called by 6+ classes)
    // Completely separate from other methods
    public static double calculatePercentage(double value, double total) {
        if (total == 0) return 0;
        return (value / total) * 100;
    }
    
    public static double roundToDecimals(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
    
    public static int randomNumber(int min, int max) {
        return mathRandom.nextInt(max - min + 1) + min;
    }
    
    public static double calculateCircleArea(double radius) {
        return PI_CONSTANT * radius * radius;
    }
    
    // File utility methods - HIGH Ca (called by 10+ classes)
    // Uses only file-related fields
    public static String getFileExtension(String fileName) {
        if (isEmpty(fileName)) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
    
    public static boolean isAllowedFile(String fileName) {
        String extension = getFileExtension(fileName);
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    public static String generateTempFileName(String prefix) {
        return TEMP_DIR + prefix + "_" + System.currentTimeMillis();
    }
}

/*
 * CLASSES THAT DEPEND ON CommonUtils (High Afferent Coupling):
 * 
 * UserService.java - uses formatString(), hashPassword(), formatDate()
 * OrderService.java - uses isEmpty(), formatDate(), calculatePercentage()
 * PaymentService.java - uses generateToken(), hashPassword()
 * ProductService.java - uses formatString(), isEmpty(), roundToDecimals()
 * ReportService.java - uses formatDate(), calculatePercentage(), escapeHtml()
 * FileUploadService.java - uses isAllowedFile(), generateTempFileName()
 * AuthenticationService.java - uses hashPassword(), generateToken()
 * ValidationService.java - uses isEmpty(), formatString()
 * NotificationService.java - uses formatDate(), escapeHtml()
 * InventoryService.java - uses calculatePercentage(), roundToDecimals()
 * 
 * ... and 30+ more services that all call these utility methods
 */