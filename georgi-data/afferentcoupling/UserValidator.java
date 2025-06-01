package com.example.validation;

public class UserValidator {

    public boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) return false;
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$");
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public boolean isValidAge(int age) {
        return age >= 13 && age <= 120;
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        return phoneNumber.matches("^\\+?[0-9]{7,15}$");
    }
}
