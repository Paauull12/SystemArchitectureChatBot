// Example 9: Long Parameter Lists & Data Clumps
// Metrics: Parameter Count=15+, Method Length=80+ lines, Coupling=High
// Combined Metrics: Parameter Pollution Index = Avg Parameters × Method Count

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderManagementSystem {
    private Map<String, Object> orderCache = new HashMap<>();

    // Extremely long parameter list - violates clean code principles
    public String createOrder(String customerId, String customerName, String customerEmail,
            String customerPhone, String billingStreet, String billingCity,
            String billingState, String billingZip, String billingCountry,
            String shippingStreet, String shippingCity, String shippingState,
            String shippingZip, String shippingCountry, boolean sameAsBilling,
            String productId1, int quantity1, double price1, String productId2,
            int quantity2, double price2, String productId3, int quantity3,
            double price3, String paymentMethod, String cardNumber,
            String expiryMonth, String expiryYear, String cvv, String cardHolderName,
            boolean expedited, String couponCode, double taxRate,
            boolean giftWrap, String giftMessage, boolean emailReceipt,
            boolean smsNotification, String preferredDeliveryTime,
            String specialInstructions, boolean insurance, double insuranceAmount) {

        // Long method with complex parameter handling
        StringBuilder orderDetails = new StringBuilder();
        double totalAmount = 0.0;

        // Validate customer information (repetitive parameter checking)
        if (customerId == null || customerId.trim().isEmpty()) {
            return "Error: Customer ID required";
        }
        if (customerName == null || customerName.length() < 2) {
            return "Error: Valid customer name required";
        }
        if (customerEmail == null || !customerEmail.contains("@")) {
            return "Error: Valid email required";
        }
        if (customerPhone == null || customerPhone.length() < 10) {
            return "Error: Valid phone number required";
        }

        // Billing address validation (data clump - should be object)
        if (billingStreet == null || billingCity == null || billingState == null ||
                billingZip == null || billingCountry == null) {
            return "Error: Complete billing address required";
        }

        // Shipping address handling (repetitive logic)
        String finalShippingStreet, finalShippingCity, finalShippingState,
                finalShippingZip, finalShippingCountry;

        if (sameAsBilling) {
            finalShippingStreet = billingStreet;
            finalShippingCity = billingCity;
            finalShippingState = billingState;
            finalShippingZip = billingZip;
            finalShippingCountry = billingCountry;
        } else {
            if (shippingStreet == null || shippingCity == null || shippingState == null ||
                    shippingZip == null || shippingCountry == null) {
                return "Error: Complete shipping address required";
            }
            finalShippingStreet = shippingStreet;
            finalShippingCity = shippingCity;
            finalShippingState = shippingState;
            finalShippingZip = shippingZip;
            finalShippingCountry = shippingCountry;
        }

        // Product processing (repetitive parameter handling)
        if (productId1 != null && !productId1.isEmpty()) {
            if (quantity1 <= 0 || price1 <= 0) {
                return "Error: Invalid quantity or price for product 1";
            }
            totalAmount += quantity1 * price1;
            orderDetails.append("Product 1: ").append(productId1)
                    .append(" Qty: ").append(quantity1)
                    .append(" Price: $").append(price1).append("\n");
        }

        if (productId2 != null && !productId2.isEmpty()) {
            if (quantity2 <= 0 || price2 <= 0) {
                return "Error: Invalid quantity or price for product 2";
            }
            totalAmount += quantity2 * price2;
            orderDetails.append("Product 2: ").append(productId2)
                    .append(" Qty: ").append(quantity2)
                    .append(" Price: $").append(price2).append("\n");
        }

        if (productId3 != null && !productId3.isEmpty()) {
            if (quantity3 <= 0 || price3 <= 0) {
                return "Error: Invalid quantity or price for product 3";
            }
            totalAmount += quantity3 * price3;
            orderDetails.append("Product 3: ").append(productId3)
                    .append(" Qty: ").append(quantity3)
                    .append(" Price: $").append(price3).append("\n");
        }

        // Payment validation (data clump)
        if (paymentMethod == null || (!paymentMethod.equals("CREDIT") && !paymentMethod.equals("DEBIT"))) {
            return "Error: Valid payment method required";
        }
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return "Error: Valid card number required";
        }
        if (expiryMonth == null || expiryYear == null || cardHolderName == null) {
            return "Error: Complete payment information required";
        }

        // Apply discounts and calculate final amount
        if (couponCode != null && !couponCode.isEmpty()) {
            if (couponCode.equals("SAVE10")) {
                totalAmount *= 0.9;
            } else if (couponCode.equals("SAVE20")) {
                totalAmount *= 0.8;
            }
        }

        totalAmount += totalAmount * taxRate;

        if (expedited) {
            totalAmount += 15.0;
        }

        if (giftWrap) {
            totalAmount += 5.0;
        }

        if (insurance) {
            totalAmount += insuranceAmount;
        }

        // Store order details
        String orderId = "ORD_" + System.currentTimeMillis();
        orderCache.put(orderId, totalAmount);

        return "Order created: " + orderId + " Total: $" + String.format("%.2f", totalAmount);
    }

    // Another method with long parameter list for updating order
    public boolean updateOrder(String orderId, String newCustomerName, String newCustomerEmail,
            String newCustomerPhone, String newBillingStreet, String newBillingCity,
            String newBillingState, String newBillingZip, String newBillingCountry,
            String newShippingStreet, String newShippingCity, String newShippingState,
            String newShippingZip, String newShippingCountry, String newPaymentMethod,
            String newCardNumber, String newExpiryMonth, String newExpiryYear,
            String newCvv, String newCardHolderName, boolean newExpedited,
            String newCouponCode, boolean newGiftWrap, String newGiftMessage,
            boolean newEmailReceipt, boolean newSmsNotification,
            String newPreferredDeliveryTime, String newSpecialInstructions,
            boolean newInsurance, double newInsuranceAmount) {

        if (!orderCache.containsKey(orderId)) {
            return false;
        }

        // Repetitive validation logic (same as create method)
        if (newCustomerName != null && newCustomerName.length() < 2) {
            return false;
        }
        if (newCustomerEmail != null && !newCustomerEmail.contains("@")) {
            return false;
        }

        // More parameter processing...
        return true;
    }

    // Method with data clumps - parameters that always go together
    public double calculateShipping(String fromStreet, String fromCity, String fromState,
            String fromZip, String fromCountry, String toStreet,
            String toCity, String toState, String toZip, String toCountry,
            double weight, double length, double width, double height,
            boolean expedited, boolean insurance, String serviceType) {

        double baseCost = 10.0;

        // Complex calculation using many parameters
        if (!fromState.equals(toState)) {
            baseCost += 5.0;
        }
        if (!fromCountry.equals(toCountry)) {
            baseCost += 20.0;
        }

        double volumeWeight = (length * width * height) / 166.0;
        double chargeableWeight = Math.max(weight, volumeWeight);
        baseCost += chargeableWeight * 0.5;

        if (expedited) {
            baseCost *= 2.0;
        }
        if (insurance) {
            baseCost += 10.0;
        }

        return baseCost;
    }

    // Method showing primitive obsession - using primitives instead of objects
    public String generateInvoice(String customerName, String customerEmail, String customerPhone,
            String billingStreet, String billingCity, String billingState,
            String billingZip, String billingCountry, String orderId,
            double subtotal, double tax, double shipping, double discount,
            double total, String paymentMethod, String cardLastFour,
            boolean isPaid, String invoiceDate, String dueDate) {

        StringBuilder invoice = new StringBuilder();
        invoice.append("INVOICE\n");
        invoice.append("Order ID: ").append(orderId).append("\n");
        invoice.append("Customer: ").append(customerName).append("\n");
        invoice.append("Email: ").append(customerEmail).append("\n");
        invoice.append("Phone: ").append(customerPhone).append("\n");
        invoice.append("Billing Address:\n");
        invoice.append(billingStreet).append("\n");
        invoice.append(billingCity).append(", ").append(billingState).append(" ").append(billingZip).append("\n");
        invoice.append(billingCountry).append("\n\n");

        invoice.append("Subtotal: $").append(String.format("%.2f", subtotal)).append("\n");
        invoice.append("Tax: $").append(String.format("%.2f", tax)).append("\n");
        invoice.append("Shipping: $").append(String.format("%.2f", shipping)).append("\n");
        invoice.append("Discount: -$").append(String.format("%.2f", discount)).append("\n");
        invoice.append("Total: $").append(String.format("%.2f", total)).append("\n");

        invoice.append("Payment Method: ").append(paymentMethod).append("\n");
        invoice.append("Card ending in: ").append(cardLastFour).append("\n");
        invoice.append("Payment Status: ").append(isPaid ? "PAID" : "PENDING").append("\n");
        invoice.append("Invoice Date: ").append(invoiceDate).append("\n");
        invoice.append("Due Date: ").append(dueDate).append("\n");

        return invoice.toString();
    }

    // Method that should use objects instead of many parameters
    public boolean validateOrderData(String customerId, String customerName, String customerEmail,
            String productId, int quantity, double price, String paymentMethod,
            String cardNumber, String street, String city, String state,
            String zip, String country, boolean expedited, String couponCode) {

        // Lots of repetitive validation logic that could be in objects
        return customerId != null && !customerId.isEmpty() &&
                customerName != null && customerName.length() >= 2 &&
                customerEmail != null && customerEmail.contains("@") &&
                productId != null && !productId.isEmpty() &&
                quantity > 0 && price > 0 &&
                paymentMethod != null && (paymentMethod.equals("CREDIT") || paymentMethod.equals("DEBIT")) &&
                cardNumber != null && cardNumber.length() >= 13 &&
                street != null && city != null && state != null && zip != null && country != null;
    }
}