package com.example.problematic;

import java.util.*;
import java.util.regex.Pattern;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Example 1: High Cyclomatic Complexity + High Cognitive Complexity + High WMC
 * 
 * Problems:
 * - Cyclomatic Complexity: 25+ per method
 * - Cognitive Complexity: 35+ per method
 * - Weighted Methods per Class: 200+ total
 * 
 * This class has many complex validation methods with deep nesting and
 * branching
 */
public class ComplexValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[1-9]\\d{1,14}$");
    private static final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    /**
     * Complex user data validation - CC: 28, Cognitive: 42
     */
    public ValidationResult validateUserData(Map<String, Object> userData, ValidationOptions options) {
        ValidationResult result = new ValidationResult();

        // Null and empty checks (CC +3)
        if (userData == null) {
            result.addError("User data cannot be null");
            return result;
        }

        if (userData.isEmpty()) {
            result.addError("User data cannot be empty");
            return result;
        }

        // Required fields validation with complex nested logic (CC +15, Cognitive +25)
        String[] requiredFields = { "firstName", "lastName", "email", "password", "birthDate", "phoneNumber" };

        for (String field : requiredFields) {
            if (!userData.containsKey(field)) {
                result.addError("Missing required field: " + field);
            } else {
                Object value = userData.get(field);

                if (value == null) {
                    result.addError("Field '" + field + "' cannot be null");
                } else {
                    String stringValue = value.toString().trim();

                    if (stringValue.isEmpty()) {
                        result.addError("Field '" + field + "' cannot be empty");
                    } else {
                        // Field-specific validation with deep nesting
                        switch (field) {
                            case "firstName":
                            case "lastName":
                                if (stringValue.length() < 2) {
                                    result.addError(field + " must be at least 2 characters");
                                } else if (stringValue.length() > 50) {
                                    result.addError(field + " cannot exceed 50 characters");
                                } else {
                                    // Character validation
                                    for (char c : stringValue.toCharArray()) {
                                        if (!Character.isLetter(c) && c != ' ' && c != '-' && c != '\'') {
                                            result.addError(field + " contains invalid characters");
                                            break;
                                        }
                                    }

                                    // Additional checks for names
                                    if (stringValue.startsWith(" ") || stringValue.endsWith(" ")) {
                                        result.addError(field + " cannot start or end with spaces");
                                    }

                                    if (stringValue.contains("  ")) {
                                        result.addError(field + " cannot contain consecutive spaces");
                                    }
                                }
                                break;

                            case "email":
                                if (!EMAIL_PATTERN.matcher(stringValue).matches()) {
                                    result.addError("Invalid email format");
                                } else {
                                    String[] emailParts = stringValue.split("@");
                                    String localPart = emailParts[0];
                                    String domainPart = emailParts[1];

                                    // Local part validation
                                    if (localPart.length() > 64) {
                                        result.addError("Email local part too long");
                                    }

                                    if (localPart.startsWith(".") || localPart.endsWith(".")) {
                                        result.addError("Email local part cannot start or end with dot");
                                    }

                                    if (localPart.contains("..")) {
                                        result.addError("Email local part cannot contain consecutive dots");
                                    }

                                    // Domain validation
                                    if (domainPart.length() > 255) {
                                        result.addError("Email domain too long");
                                    }

                                    String[] domainParts = domainPart.split("\\.");
                                    for (String part : domainParts) {
                                        if (part.length() == 0) {
                                            result.addError("Invalid domain format");
                                            break;
                                        }
                                        if (part.length() > 63) {
                                            result.addError("Domain label too long");
                                            break;
                                        }
                                    }

                                    // Check for common domains if option enabled
                                    if (options != null && options.isCheckCommonDomains()) {
                                        String[] commonDomains = { "gmail.com", "yahoo.com", "hotmail.com",
                                                "outlook.com" };
                                        boolean isCommon = false;
                                        for (String commonDomain : commonDomains) {
                                            if (domainPart.equalsIgnoreCase(commonDomain)) {
                                                isCommon = true;
                                                break;
                                            }
                                        }
                                        if (options.isRequireCommonDomain() && !isCommon) {
                                            result.addError("Email must use a common domain");
                                        }
                                        if (options.isForbidCommonDomain() && isCommon) {
                                            result.addError("Email cannot use common domains");
                                        }
                                    }
                                }
                                break;

                            case "password":
                                if (stringValue.length() < 8) {
                                    result.addError("Password must be at least 8 characters");
                                } else if (stringValue.length() > 128) {
                                    result.addError("Password cannot exceed 128 characters");
                                } else {
                                    if (!PASSWORD_PATTERN.matcher(stringValue).matches()) {
                                        result.addError(
                                                "Password must contain uppercase, lowercase, digit, and special character");
                                    } else {
                                        // Additional password strength checks
                                        boolean hasSequential = false;
                                        boolean hasRepeated = false;

                                        // Check for sequential characters
                                        for (int i = 0; i < stringValue.length() - 2; i++) {
                                            char c1 = stringValue.charAt(i);
                                            char c2 = stringValue.charAt(i + 1);
                                            char c3 = stringValue.charAt(i + 2);

                                            if (c2 == c1 + 1 && c3 == c2 + 1) {
                                                hasSequential = true;
                                                break;
                                            }
                                        }

                                        // Check for repeated characters
                                        for (int i = 0; i < stringValue.length() - 2; i++) {
                                            char c1 = stringValue.charAt(i);
                                            char c2 = stringValue.charAt(i + 1);
                                            char c3 = stringValue.charAt(i + 2);

                                            if (c1 == c2 && c2 == c3) {
                                                hasRepeated = true;
                                                break;
                                            }
                                        }

                                        if (options != null) {
                                            if (options.isForbidSequential() && hasSequential) {
                                                result.addError("Password cannot contain sequential characters");
                                            }
                                            if (options.isForbidRepeated() && hasRepeated) {
                                                result.addError("Password cannot contain repeated characters");
                                            }

                                            // Check against common passwords
                                            if (options.isCheckCommonPasswords()) {
                                                String[] commonPasswords = { "password", "123456", "qwerty", "abc123" };
                                                for (String common : commonPasswords) {
                                                    if (stringValue.toLowerCase().contains(common.toLowerCase())) {
                                                        result.addError("Password contains common patterns");
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                break;

                            case "phoneNumber":
                                // Remove common formatting characters
                                String cleanPhone = stringValue.replaceAll("[\\s\\-\\(\\)\\.]", "");

                                if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                                    result.addError("Invalid phone number format");
                                } else {
                                    // Country-specific validation
                                    if (options != null && options.getCountryCode() != null) {
                                        switch (options.getCountryCode().toUpperCase()) {
                                            case "US":
                                                if (cleanPhone.length() != 10 && cleanPhone.length() != 11) {
                                                    result.addError("US phone number must be 10 or 11 digits");
                                                } else {
                                                    if (cleanPhone.length() == 11 && !cleanPhone.startsWith("1")) {
                                                        result.addError(
                                                                "US phone number with 11 digits must start with 1");
                                                    }

                                                    String areaCode = cleanPhone.length() == 10
                                                            ? cleanPhone.substring(0, 3)
                                                            : cleanPhone.substring(1, 4);

                                                    if (areaCode.startsWith("0") || areaCode.startsWith("1")) {
                                                        result.addError("Invalid US area code");
                                                    }
                                                }
                                                break;

                                            case "UK":
                                                if (!cleanPhone.startsWith("44")) {
                                                    result.addError("UK phone number must start with country code 44");
                                                }
                                                break;

                                            case "DE":
                                                if (!cleanPhone.startsWith("49")) {
                                                    result.addError(
                                                            "German phone number must start with country code 49");
                                                }
                                                break;
                                        }
                                    }
                                }
                                break;

                            case "birthDate":
                                try {
                                    SimpleDateFormat[] formats = {
                                            new SimpleDateFormat("yyyy-MM-dd"),
                                            new SimpleDateFormat("MM/dd/yyyy"),
                                            new SimpleDateFormat("dd-MM-yyyy")
                                    };

                                    Date birthDate = null;
                                    boolean validFormat = false;

                                    for (SimpleDateFormat format : formats) {
                                        try {
                                            format.setLenient(false);
                                            birthDate = format.parse(stringValue);
                                            validFormat = true;
                                            break;
                                        } catch (ParseException e) {
                                            // Try next format
                                        }
                                    }

                                    if (!validFormat) {
                                        result.addError("Invalid birth date format");
                                    } else {
                                        Date now = new Date();

                                        // Check if birth date is in the future
                                        if (birthDate.after(now)) {
                                            result.addError("Birth date cannot be in the future");
                                        } else {
                                            // Calculate age
                                            Calendar birthCal = Calendar.getInstance();
                                            birthCal.setTime(birthDate);
                                            Calendar nowCal = Calendar.getInstance();
                                            nowCal.setTime(now);

                                            int age = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

                                            if (nowCal.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                                                age--;
                                            }

                                            if (age < 13) {
                                                result.addError("User must be at least 13 years old");
                                            } else if (age > 120) {
                                                result.addError("Invalid birth date - age cannot exceed 120 years");
                                            }

                                            // Additional age-based validations
                                            if (options != null) {
                                                if (options.getMinAge() > 0 && age < options.getMinAge()) {
                                                    result.addError("User must be at least " + options.getMinAge()
                                                            + " years old");
                                                }
                                                if (options.getMaxAge() > 0 && age > options.getMaxAge()) {
                                                    result.addError("User cannot be older than " + options.getMaxAge()
                                                            + " years");
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    result.addError("Error processing birth date: " + e.getMessage());
                                }
                                break;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Complex payment validation - CC: 22, Cognitive: 28
     */
    public ValidationResult validatePaymentData(Map<String, Object> paymentData, PaymentOptions options) {
        ValidationResult result = new ValidationResult();

        if (paymentData == null || paymentData.isEmpty()) {
            result.addError("Payment data is required");
            return result;
        }

        String paymentType = (String) paymentData.get("paymentType");

        if (paymentType == null) {
            result.addError("Payment type is required");
            return result;
        }

        switch (paymentType.toLowerCase()) {
            case "credit_card":
                validateCreditCard(paymentData, options, result);
                break;
            case "debit_card":
                validateDebitCard(paymentData, options, result);
                break;
            case "paypal":
                validatePayPal(paymentData, options, result);
                break;
            case "bank_transfer":
                validateBankTransfer(paymentData, options, result);
                break;
            case "crypto":
                validateCrypto(paymentData, options, result);
                break;
            default:
                result.addError("Unsupported payment type: " + paymentType);
        }

        return result;
    }

    /**
     * Complex address validation - CC: 18, Cognitive: 25
     */
    public ValidationResult validateAddress(Map<String, Object> addressData, AddressOptions options) {
        ValidationResult result = new ValidationResult();

        if (addressData == null || addressData.isEmpty()) {
            result.addError("Address data is required");
            return result;
        }

        String[] requiredFields = { "street", "city", "state", "zipCode", "country" };

        for (String field : requiredFields) {
            if (!addressData.containsKey(field)) {
                result.addError("Missing address field: " + field);
            } else {
                Object value = addressData.get(field);
                if (value == null || value.toString().trim().isEmpty()) {
                    result.addError("Address field '" + field + "' cannot be empty");
                } else {
                    String stringValue = value.toString().trim();

                    switch (field) {
                        case "street":
                            if (stringValue.length() < 5) {
                                result.addError("Street address too short");
                            } else if (stringValue.length() > 100) {
                                result.addError("Street address too long");
                            }
                            break;

                        case "city":
                            if (stringValue.length() < 2) {
                                result.addError("City name too short");
                            } else if (stringValue.length() > 50) {
                                result.addError("City name too long");
                            } else {
                                // Check for valid city characters
                                for (char c : stringValue.toCharArray()) {
                                    if (!Character.isLetter(c) && c != ' ' && c != '-' && c != '\'') {
                                        result.addError("City name contains invalid characters");
                                        break;
                                    }
                                }
                            }
                            break;

                        case "state":
                            if (options != null && options.getCountry() != null) {
                                switch (options.getCountry().toUpperCase()) {
                                    case "US":
                                        if (stringValue.length() != 2) {
                                            result.addError("US state code must be 2 characters");
                                        } else {
                                            String[] validStates = { "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE",
                                                    "FL", "GA" };
                                            boolean validState = false;
                                            for (String state : validStates) {
                                                if (state.equalsIgnoreCase(stringValue)) {
                                                    validState = true;
                                                    break;
                                                }
                                            }
                                            if (!validState) {
                                                result.addError("Invalid US state code");
                                            }
                                        }
                                        break;

                                    case "CA":
                                        if (stringValue.length() != 2) {
                                            result.addError("Canadian province code must be 2 characters");
                                        }
                                        break;
                                }
                            }
                            break;

                        case "zipCode":
                            if (options != null && options.getCountry() != null) {
                                switch (options.getCountry().toUpperCase()) {
                                    case "US":
                                        if (!stringValue.matches("\\d{5}(-\\d{4})?")) {
                                            result.addError("Invalid US ZIP code format");
                                        }
                                        break;

                                    case "CA":
                                        if (!stringValue.matches("[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d")) {
                                            result.addError("Invalid Canadian postal code format");
                                        }
                                        break;

                                    case "UK":
                                        if (!stringValue.matches("[A-Za-z]{1,2}\\d[A-Za-z\\d]? \\d[A-Za-z]{2}")) {
                                            result.addError("Invalid UK postal code format");
                                        }
                                        break;
                                }
                            }
                            break;

                        case "country":
                            if (stringValue.length() != 2 && stringValue.length() != 3) {
                                result.addError("Country code must be 2 or 3 characters");
                            }
                            break;
                    }
                }
            }
        }

        return result;
    }

    // Additional complex validation methods to increase WMC
    private void validateCreditCard(Map<String, Object> data, PaymentOptions options, ValidationResult result) {
        // Complex credit card validation logic
    }

    private void validateDebitCard(Map<String, Object> data, PaymentOptions options, ValidationResult result) {
        // Complex debit card validation logic
    }

    private void validatePayPal(Map<String, Object> data, PaymentOptions options, ValidationResult result) {
        // Complex PayPal validation logic
    }

    private void validateBankTransfer(Map<String, Object> data, PaymentOptions options, ValidationResult result) {
        // Complex bank transfer validation logic
    }

    private void validateCrypto(Map<String, Object> data, PaymentOptions options, ValidationResult result) {
        // Complex cryptocurrency validation logic
    }
}

// Supporting classes
class ValidationResult {
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}

class ValidationOptions {
    private boolean checkCommonDomains;
    private boolean requireCommonDomain;
    private boolean forbidCommonDomain;
    private boolean forbidSequential;
    private boolean forbidRepeated;
    private boolean checkCommonPasswords;
    private String countryCode;
    private int minAge;
    private int maxAge;

    // Getters and setters
    public boolean isCheckCommonDomains() {
        return checkCommonDomains;
    }

    public boolean isRequireCommonDomain() {
        return requireCommonDomain;
    }

    public boolean isForbidCommonDomain() {
        return forbidCommonDomain;
    }

    public boolean isForbidSequential() {
        return forbidSequential;
    }

    public boolean isForbidRepeated() {
        return forbidRepeated;
    }

    public boolean isCheckCommonPasswords() {
        return checkCommonPasswords;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }
}

class PaymentOptions {
    // Payment validation options
}

class AddressOptions {
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}