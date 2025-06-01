package com.example.utils;

public class CommonUtils {

    public static String formatName(String firstName, String lastName) {
        return lastName.toUpperCase() + ", " + firstName;
    }

    public static double calculateDiscount(double originalPrice, double discountPercentage) {
        return originalPrice * (1 - discountPercentage);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
    }

    public static void logActivity(String activity) {
        System.out.println("LOG: " + activity);
    }
}

package com.example.orders;
import com.example.utils.CommonUtils;

public class OrderProcessor {

    public void processOrder(String customerName, double amount) {
        String formattedCustomer = CommonUtils.formatName("John", "Doe"); // 
        double discountedAmount = CommonUtils.calculateDiscount(amount, 0.10); //
        CommonUtils.logActivity("Order processed for " + formattedCustomer + " with amount " + discountedAmount); //
        // ... 
    }
}

package com.example.security;
import com.example.utils.CommonUtils;

public class UserAuthenticator {

    public boolean authenticateUser(String username, String password) {
        // ... 
        CommonUtils.logActivity("User " + username + " attempted login.");
        return true;
    }

    public boolean registerUser(String email, String password) {
        if (!CommonUtils.isValidEmail(email)) { //
            System.out.println("Invalid email format.");
            return false;
        }
        CommonUtils.logActivity("New user registered with email: " + email); //
        return true; 
    }
}

package com.example.reporting;
import com.example.utils.CommonUtils;

public class ReportGenerator {

    public void generateDailyReport() {
        // ...
        CommonUtils.logActivity("Daily report generated.");
        // ...
    }
}