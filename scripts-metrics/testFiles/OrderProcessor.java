package com.example.problematic;

import java.util.List;

/**
 * Example 1: High Cyclomatic Complexity (CC = 15+)
 * This class demonstrates excessive decision points and nested conditions
 * that make the code difficult to test and maintain.
 */
public class OrderProcessor {

    /**
     * Processes an order with extremely high cyclomatic complexity
     * CC ≈ 18 (every if/else if adds to the count)
     * 
     * Problems:
     * - Too many nested conditions
     * - Multiple exit points
     * - Difficult to test all paths
     * - Hard to maintain and modify
     */
    public String processOrder(Order order, Customer customer, Inventory inventory) {
        // Decision point 1
        if (order != null) {
            // Decision point 2
            if (customer != null) {
                // Decision point 3
                if (customer.isActive()) {
                    // Decision point 4
                    if (customer.hasValidPayment()) {
                        for (Item item : order.getItems()) {
                            // Decision point 5
                            if (item != null) {
                                // Decision point 6
                                if (inventory.hasStock(item)) {
                                    // Decision point 7
                                    if (item.getPrice() > 0) {
                                        // Decision point 8
                                        if (customer.getBalance() >= item.getPrice()) {
                                            // Decision point 9
                                            if (item.getCategory().equals("electronics")) {
                                                // Decision point 10
                                                if (customer.getAge() >= 18) {
                                                    // Process electronics for adults
                                                    processElectronicsItem(item, customer);
                                                } else {
                                                    return "Age restriction for electronics: " + item.getName();
                                                }
                                                // Decision point 11
                                            } else if (item.getCategory().equals("books")) {
                                                // Process books
                                                processBooksItem(item, customer);
                                                // Decision point 12
                                            } else if (item.getCategory().equals("clothing")) {
                                                // Decision point 13
                                                if (item.getSize() != null) {
                                                    // Process clothing
                                                    processClothingItem(item, customer);
                                                } else {
                                                    return "Invalid size for clothing item: " + item.getName();
                                                }
                                                // Decision point 14
                                            } else if (item.getCategory().equals("food")) {
                                                // Decision point 15
                                                if (!item.isExpired()) {
                                                    processFoodItem(item, customer);
                                                } else {
                                                    return "Expired food item: " + item.getName();
                                                }
                                                // Decision point 16
                                            } else if (item.getCategory().equals("medicine")) {
                                                // Decision point 17
                                                if (customer.hasPrescription(item)) {
                                                    processMedicineItem(item, customer);
                                                } else {
                                                    return "Prescription required for: " + item.getName();
                                                }
                                            } else {
                                                return "Unknown category: " + item.getCategory();
                                            }
                                        } else {
                                            return "Insufficient funds for item: " + item.getName();
                                        }
                                    } else {
                                        return "Invalid price for item: " + item.getName();
                                    }
                                } else {
                                    return "Out of stock: " + item.getName();
                                }
                            } else {
                                return "Invalid item in order";
                            }
                        }

                        // Final processing after all items validated
                        finalizeOrder(order, customer);

                    } else {
                        return "Invalid payment method";
                    }
                } else {
                    return "Customer account is inactive";
                }
            } else {
                return "Customer information is required";
            }
        } else {
            return "Order cannot be null";
        }

        return "Order processed successfully";
    }

    private void processElectronicsItem(Item item, Customer customer) {
        // Electronics processing logic
        System.out.println("Processing electronics: " + item.getName());
    }

    private void processBooksItem(Item item, Customer customer) {
        // Books processing logic
        System.out.println("Processing book: " + item.getName());
    }

    private void processClothingItem(Item item, Customer customer) {
        // Clothing processing logic
        System.out.println("Processing clothing: " + item.getName());
    }

    private void processFoodItem(Item item, Customer customer) {
        // Food processing logic
        System.out.println("Processing food: " + item.getName());
    }

    private void processMedicineItem(Item item, Customer customer) {
        // Medicine processing logic
        System.out.println("Processing medicine: " + item.getName());
    }

    private void finalizeOrder(Order order, Customer customer) {
        // Order finalization logic
        System.out.println("Finalizing order for customer: " + customer.getName());
    }
}

// Supporting classes for the example
class Order {
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}

class Customer {
    private String name;
    private boolean active;
    private boolean validPayment;
    private double balance;
    private int age;

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasValidPayment() {
        return validPayment;
    }

    public double getBalance() {
        return balance;
    }

    public int getAge() {
        return age;
    }

    public boolean hasPrescription(Item item) {
        // Mock prescription check
        return true;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setValidPayment(boolean validPayment) {
        this.validPayment = validPayment;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

class Item {
    private String name;
    private String category;
    private double price;
    private String size;
    private boolean expired;

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public String getSize() {
        return size;
    }

    public boolean isExpired() {
        return expired;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}

class Inventory {
    public boolean hasStock(Item item) {
        // Mock stock check
        return true;
    }
}