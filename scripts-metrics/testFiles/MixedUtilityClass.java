// Example 2: Poor Cohesion (High LCOM = 0.85)
// Metrics: LCOM=0.85, TCC=0.15, WMC=28

import java.util.*;
import java.time.LocalDate;

public class MixedUtilityClass {
    // Unrelated fields that don't work together
    private String userName;
    private double accountBalance;
    private List<String> favoriteColors;
    private int loginAttempts;
    private boolean isSubscribed;
    private LocalDate birthDate;
    private String emailAddress;

    // Methods that only use one or two fields - poor cohesion
    public void updateUserName(String name) {
        this.userName = name;
    }

    public void addFavoriteColor(String color) {
        if (favoriteColors == null) {
            favoriteColors = new ArrayList<>();
        }
        favoriteColors.add(color);
    }

    public void incrementLoginAttempts() {
        loginAttempts++;
    }

    public double calculateTax() {
        return accountBalance * 0.25;
    }

    public void toggleSubscription() {
        isSubscribed = !isSubscribed;
    }

    public int calculateAge() {
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    public boolean isValidEmail() {
        return emailAddress != null && emailAddress.contains("@");
    }

    // Completely unrelated utility methods
    public static String reverseString(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    public static double calculateCircumference(double radius) {
        return 2 * Math.PI * radius;
    }

    public static int findMaxInArray(int[] numbers) {
        int max = numbers[0];
        for (int num : numbers) {
            if (num > max)
                max = num;
        }
        return max;
    }

    public static boolean isPalindrome(String text) {
        String clean = text.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return clean.equals(new StringBuilder(clean).reverse().toString());
    }

    // Mathematical operations unrelated to class purpose
    public static double fibonacci(int n) {
        if (n <= 1)
            return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static List<Integer> generatePrimes(int limit) {
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= limit; i++) {
            boolean isPrime = true;
            for (int j = 2; j <= Math.sqrt(i); j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime)
                primes.add(i);
        }
        return primes;
    }
}