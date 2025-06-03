// Example 1: High Cyclomatic & Cognitive Complexity
// Metrics: Cyclomatic=15, Cognitive=18, WMC=25

import java.util.*;

public class ComplexProcessor {
    private int errorCount = 0;

    // Overly complex method with nested conditions and loops
    public String processOrder(String orderType, int quantity, double price,
            boolean isPremium, String customerType) {

        // Nested if-else chains increase cyclomatic complexity
        if (orderType != null) {
            if (orderType.equals("STANDARD")) {
                if (quantity > 0) {
                    if (price > 0) {
                        for (int i = 0; i < quantity; i++) {
                            if (isPremium) {
                                if (customerType.equals("VIP")) {
                                    if (price > 1000) {
                                        return "premium_vip_large";
                                    } else if (price > 500) {
                                        return "premium_vip_medium";
                                    } else {
                                        return "premium_vip_small";
                                    }
                                } else if (customerType.equals("REGULAR")) {
                                    return "premium_regular";
                                }
                            } else {
                                if (quantity > 10) {
                                    return "bulk_standard";
                                }
                            }
                        }
                    } else {
                        errorCount++;
                        return "invalid_price";
                    }
                } else {
                    errorCount++;
                    return "invalid_quantity";
                }
            } else if (orderType.equals("EXPRESS")) {
                // More nested complexity
                try {
                    if (isPremium && price > 100) {
                        switch (customerType) {
                            case "VIP":
                                return quantity > 5 ? "express_vip_bulk" : "express_vip";
                            case "REGULAR":
                                return "express_regular";
                            default:
                                return "express_unknown";
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    return "error_express";
                }
            }
        }
        return "default_order";
    }

    // Another complex method
    public boolean validateInput(Object data, String type, Map<String, Object> config) {
        if (data == null)
            return false;

        switch (type) {
            case "STRING":
                String str = (String) data;
                return str.length() > 0 && str.length() < 100;
            case "NUMBER":
                if (data instanceof Integer) {
                    return (Integer) data > 0;
                } else if (data instanceof Double) {
                    return (Double) data > 0.0;
                }
                break;
            case "LIST":
                List<?> list = (List<?>) data;
                return !list.isEmpty() && list.size() < 50;
        }
        return false;
    }
}