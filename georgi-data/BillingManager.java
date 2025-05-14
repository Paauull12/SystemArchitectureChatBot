public class BillingManager {
    public void handleBilling(String mode, double amount) {
        if (mode.equals("credit")) {
            // long if-else block
            if (amount > 1000) {
                System.out.println("Special handling for credit > 1000");
                // ...
            } else {
                if (amount > 500) {
                    // ...
                } else {
                    // ...
                }
            }
        } else if (mode.equals("paypal")) {
            // Nested logic here
        } else if (mode.equals("bitcoin")) {
            // More nested logic
        } else {
            throw new IllegalArgumentException("Unknown mode");
        }
    }
}