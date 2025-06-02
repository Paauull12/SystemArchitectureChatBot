package com.example.problematic;

import java.util.*;
import java.text.*;
import java.io.*;
import java.math.*;
import java.awt.Color;
import java.time.*;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Example 2: High LCOM + Low TCC + High Afferent Coupling
 * 
 * Problems:
 * - LCOM: 0.95+ (methods don't share instance variables)
 * - TCC: 0.05 (very low tight class cohesion)
 * - Afferent Coupling: 40+ (many classes depend on this utility)
 * 
 * This is a "utility library" that many classes depend on but has no cohesion
 */
public class UtilityLibrary {

    // String processing fields (used only by string methods)
    private String defaultEncoding = "UTF-8";
    private Pattern whiteSpacePattern = Pattern.compile("\\s+");
    private Map<String, String> stringReplacements = new HashMap<>();

    // Math calculation fields (used only by math methods)
    private int mathPrecision = 10;
    private RoundingMode defaultRounding = RoundingMode.HALF_UP;
    private BigDecimal mathConstantPi = new BigDecimal("3.1415926535897932384626433832795");

    // Date formatting fields (used only by date methods)
    private String defaultDateFormat = "yyyy-MM-dd";
    private TimeZone defaultTimeZone = TimeZone.getDefault();
    private Locale dateLocale = Locale.US;

    // File processing fields (used only by file methods)
    private String tempDirectoryPath = System.getProperty("java.io.tmpdir");
    private int bufferSize = 8192;
    private List<String> allowedExtensions = Arrays.asList(".txt", ".csv", ".json");

    // Color processing fields (used only by color methods)
    private Color defaultBackgroundColor = Color.WHITE;
    private Color defaultForegroundColor = Color.BLACK;
    private Map<String, Color> namedColors = new HashMap<>();

    // Security fields (used only by security methods)
    private String defaultHashAlgorithm = "SHA-256";
    private int saltLength = 16;
    private Random secureRandom = new Random();

    // Network fields (used only by network methods)
    private int connectionTimeout = 30000;
    private int readTimeout = 60000;
    private String userAgent = "UtilityLibrary/1.0";

    // Validation fields (used only by validation methods)
    private Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private Pattern phonePattern = Pattern.compile("^[+]?[1-9]\\d{1,14}$");
    private List<String> reservedWords = Arrays.asList("admin", "root", "system");

    /**
     * String utility methods - only use string-related fields
     * Many external classes depend on these methods (High Ca)
     */
    public String formatString(String input) {
        if (input == null)
            return "";

        String result = input.trim();
        result = whiteSpacePattern.matcher(result).replaceAll(" ");

        // Apply replacements
        for (Map.Entry<String, String> replacement : stringReplacements.entrySet()) {
            result = result.replace(replacement.getKey(), replacement.getValue());
        }

        return result;
    }

    public String capitalizeWords(String input) {
        if (input == null || input.isEmpty())
            return "";

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    public String reverseString(String input) {
        if (input == null)
            return "";
        return new StringBuilder(input).reverse().toString();
    }

    public boolean isStringEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public String truncateString(String input, int maxLength) {
        if (input == null)
            return "";
        if (input.length() <= maxLength)
            return input;
        return input.substring(0, maxLength - 3) + "...";
    }

    /**
     * Math utility methods - only use math-related fields
     * Completely separate from string methods (increases LCOM)
     */
    public BigDecimal calculatePercentage(BigDecimal value, BigDecimal percentage) {
        if (value == null || percentage == null)
            return BigDecimal.ZERO;

        return value.multiply(percentage)
                .divide(new BigDecimal("100"), mathPrecision, defaultRounding);
    }

    public double calculateDistance(double x1, double y1, double x2, double y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public BigDecimal calculateCircleArea(BigDecimal radius) {
        if (radius == null || radius.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return mathConstantPi.multiply(radius.multiply(radius))
                .setScale(mathPrecision, defaultRounding);
    }

    public boolean isPrime(int number) {
        if (number < 2)
            return false;
        if (number == 2)
            return true;
        if (number % 2 == 0)
            return false;

        for (int i = 3; i <= Math.sqrt(number); i += 2) {
            if (number % i == 0)
                return false;
        }
        return true;
    }

    public long fibonacci(int n) {
        if (n <= 1)
            return n;

        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }

    /**
     * Date utility methods - only use date-related fields
     * No shared variables with other method groups
     */
    public String formatDate(Date date) {
        if (date == null)
            return "";

        SimpleDateFormat formatter = new SimpleDateFormat(defaultDateFormat, dateLocale);
        formatter.setTimeZone(defaultTimeZone);
        return formatter.format(date);
    }

    public Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty())
            return null;

        SimpleDateFormat formatter = new SimpleDateFormat(defaultDateFormat, dateLocale);
        formatter.setTimeZone(defaultTimeZone);

        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public boolean isWeekend(Date date) {
        if (date == null) return false;
        
        Calendar cal = Calendar.getInstance(defaultTimeZone, dateLocale);
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

public int getAge(Date birthDate) {
        if (birthDate == null) return 0;
        
        Calendar birth = Calendar.getInstance(defaultTimeZone, dateLocale);
        birth.setTime(birthDate);
        Calendar now = Calendar.getInstance(defaultTimeZone, dateLocale);
        
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;