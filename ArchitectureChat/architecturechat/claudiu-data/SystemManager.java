// Example 6: God Class - Violates Single Responsibility Principle
// Metrics: WMC=45, LCOM=0.9, Cyclomatic=25, Lines of Code=95
// Combined Metrics: Responsibility Overload Index = HIGH

import java.util.*;
import java.time.LocalDateTime;
import java.io.*;

public class SystemManager {
    // Too many unrelated responsibilities in one class
    private List<User> users;
    private Map<String, Product> inventory;
    private double totalRevenue;
    private String databaseUrl;
    private List<String> logMessages;
    private Map<String, Integer> emailQueue;
    private boolean maintenanceMode;
    private LocalDateTime lastBackup;

    public SystemManager() {
        users = new ArrayList<>();
        inventory = new HashMap<>();
        logMessages = new ArrayList<>();
        emailQueue = new HashMap<>();
    }

    // User Management Responsibility
    public void addUser(String username, String email, String role) {
        if (username != null && email.contains("@")) {
            User user = new User(username, email, role);
            users.add(user);
            log("User added: " + username);
            queueWelcomeEmail(email);
        }
    }

    public boolean authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (user.validatePassword(password)) {
                    log("Login successful: " + username);
                    return true;
                } else {
                    log("Login failed: " + username);
                }
            }
        }
        return false;
    }

    // Inventory Management Responsibility
    public void addProduct(String id, String name, double price, int quantity) {
        Product product = new Product(id, name, price, quantity);
        inventory.put(id, product);
        log("Product added: " + name);

        if (quantity < 10) {
            queueLowStockEmail(name);
        }
    }

    public boolean sellProduct(String productId, int quantity) {
        Product product = inventory.get(productId);
        if (product != null && product.getQuantity() >= quantity) {
            product.reduceQuantity(quantity);
            totalRevenue += product.getPrice() * quantity;
            log("Sale: " + quantity + " units of " + product.getName());
            return true;
        }
        return false;
    }

    // Financial Management Responsibility
    public double calculateTotalRevenue() {
        return totalRevenue;
    }

    public Map<String, Double> generateSalesReport() {
        Map<String, Double> report = new HashMap<>();
        for (Product product : inventory.values()) {
            report.put(product.getName(), product.getPrice() * product.getSoldQuantity());
        }
        return report;
    }

    // System Administration Responsibility
    public void performBackup() {
        try {
            log("Starting system backup...");
            // Simulate backup process
            Thread.sleep(1000);
            lastBackup = LocalDateTime.now();
            log("Backup completed successfully");
        } catch (InterruptedException e) {
            log("Backup failed: " + e.getMessage());
        }
    }

    public void toggleMaintenanceMode() {
        maintenanceMode = !maintenanceMode;
        log("Maintenance mode: " + (maintenanceMode ? "ON" : "OFF"));

        if (maintenanceMode) {
            queueMaintenanceEmails();
        }
    }

    // Logging Responsibility
    private void log(String message) {
        String timestamp = LocalDateTime.now().toString();
        logMessages.add(timestamp + ": " + message);

        if (logMessages.size() > 1000) {
            archiveLogs();
        }
    }

    private void archiveLogs() {
        // Move old logs to archive
        logMessages.clear();
        log("Logs archived");
    }

    // Email Management Responsibility
    private void queueWelcomeEmail(String email) {
        emailQueue.put("welcome_" + email, 1);
    }

    private void queueLowStockEmail(String productName) {
        emailQueue.put("lowstock_" + productName, 2);
    }

    private void queueMaintenanceEmails() {
        for (User user : users) {
            emailQueue.put("maintenance_" + user.getEmail(), 3);
        }
    }

    public void processEmailQueue() {
        for (Map.Entry<String, Integer> entry : emailQueue.entrySet()) {
            String emailType = entry.getKey();
            int priority = entry.getValue();

            // Complex email processing logic
            if (priority == 1) {
                log("Sending welcome email: " + emailType);
            } else if (priority == 2) {
                log("Sending low stock alert: " + emailType);
            } else if (priority == 3) {
                log("Sending maintenance notice: " + emailType);
            }
        }
        emailQueue.clear();
    }
}

// Supporting classes
class User {
    private String username, email, role, password;

    public User(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = "default123"; // Poor security practice
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean validatePassword(String pwd) {
        return password.equals(pwd);
    }
}

class Product {
    private String id, name;
    private double price;
    private int quantity, soldQuantity = 0;

    public Product(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void reduceQuantity(int amount) {
        quantity -= amount;
        soldQuantity += amount;
    }
}