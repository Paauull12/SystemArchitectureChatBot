package com.example.short;

import java.util.*;

/**
 * Example 5: High WMC + High LCOM
 * Problems: WMC: 80+, LCOM: 0.87+
 * 
 * This class tries to do everything but methods don't share instance variables
 */
public class MultiPurposeService {
    
    // Email fields (used only by email methods)
    private String smtpHost = "localhost";
    private int smtpPort = 587;
    private String emailUsername = "admin";
    private String emailPassword = "password";
    
    // Database fields (used only by DB methods)
    private String dbUrl = "jdbc:mysql://localhost:3306/db";
    private String dbUsername = "root";
    private String dbPassword = "admin";
    private int connectionTimeout = 30;
    
    // File fields (used only by file methods)
    private String uploadPath = "/uploads/";
    private String tempPath = "/tmp/";
    private long maxFileSize = 1000000L;
    private List<String> allowedExtensions = Arrays.asList(".jpg", ".png", ".pdf");
    
    // Cache fields (used only by cache methods)
    private Map<String, Object> cacheData = new HashMap<>();
    private Map<String, Long> cacheTimestamps = new HashMap<>();
    private long cacheExpiry = 3600000L;
    private int maxCacheSize = 1000;
    
    // Notification fields (used only by notification methods)
    private Queue<String> notificationQueue = new LinkedList<>();
    private Set<String> subscribedUsers = new HashSet<>();
    private String notificationTemplate = "Hello {user}, you have a new message: {message}";
    
    /**
     * Email methods - only use email-related fields
     */
    public boolean sendEmail(String to, String subject, String body) {
        if (to == null || subject == null || body == null) {
            return false;
        }
        
        if (!to.contains("@")) {
            return false;
        }
        
        // Email sending logic using smtpHost, smtpPort, emailUsername, emailPassword
        System.out.println("Sending email via " + smtpHost + ":" + smtpPort);
        System.out.println("To: " + to + ", Subject: " + subject);
        
        return true;
    }
    
    public boolean configureEmailServer(String host, int port, String username, String password) {
        if (host == null || username == null || password == null) {
            return false;
        }
        
        this.smtpHost = host;
        this.smtpPort = port;
        this.emailUsername = username;
        this.emailPassword = password;
        
        return true;
    }
    
    /**
     * Database methods - only use database-related fields
     */
    public boolean saveToDatabase(String table, Map<String, Object> data) {
        if (table == null || data == null || data.isEmpty()) {
            return false;
        }
        
        // Database logic using dbUrl, dbUsername, dbPassword, connectionTimeout
        System.out.println("Connecting to " + dbUrl + " with timeout " + connectionTimeout);
        System.out.println("Saving to table: " + table);
        
        return true;
    }
    
    public Map<String, Object> loadFromDatabase(String table, String id) {
        if (table == null || id == null) {
            return null;
        }
        
        // Database retrieval logic
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("table", table);
        
        return result;
    }
    
    public boolean configureDatabaseConnection(String url, String username, String password, int timeout) {
        if (url == null || username == null || password == null) {
            return false;
        }
        
        this.dbUrl = url;
        this.dbUsername = username;
        this.dbPassword = password;
        this.connectionTimeout = timeout;
        
        return true;
    }
    
    /**
     * File methods - only use file-related fields
     */
    public boolean uploadFile(String fileName, byte[] content) {
        if (fileName == null || content == null) {
            return false;
        }
        
        if (content.length > maxFileSize) {
            return false;
        }
        
        String extension = getFileExtension(fileName);
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            return false;
        }
        
        // File upload logic using uploadPath, tempPath, maxFileSize
        String fullPath = uploadPath + fileName;
        System.out.println("Uploading file to: " + fullPath);
        
        return true;
    }
    
    public boolean deleteFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String fullPath = uploadPath + fileName;
        System.out.println("Deleting file: " + fullPath);
        
        return true;
    }
    
    public String createTempFile(String prefix) {
        if (prefix == null) {
            prefix = "temp";
        }
        
        return tempPath + prefix + "_" + System.currentTimeMillis();
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
    
    /**
     * Cache methods - only use cache-related fields
     */
    public Object getFromCache(String key) {
        if (key == null) {
            return null;
        }
        
        // Check if cache entry exists and is not expired
        if (cacheData.containsKey(key)) {
            Long timestamp = cacheTimestamps.get(key);
            if (timestamp != null && (System.currentTimeMillis() - timestamp) < cacheExpiry) {
                return cacheData.get(key);
            } else {
                // Remove expired entry
                cacheData.remove(key);
                cacheTimestamps.remove(key);
            }
        }
        
        return null;
    }
    
    public void putInCache(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        
        // Check cache size limit
        if (cacheData.size() >= maxCacheSize) {
            clearOldestCacheEntry();
        }
        
        cacheData.put(key, value);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    public boolean clearExpiredCache() {
        List<String> expiredKeys = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
            if ((currentTime - entry.getValue()) >= cacheExpiry) {
                expiredKeys.add(entry.getKey());
            }
        }
        
        for (String key : expiredKeys) {
            cacheData.remove(key);
            cacheTimestamps.remove(key);
        }
        
        return !expiredKeys.isEmpty();
    }
    
    private void clearOldestCacheEntry() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
            if (entry.getValue() < oldestTime) {
                oldestTime = entry.getValue();
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cacheData.remove(oldestKey);
            cacheTimestamps.remove(oldestKey);
        }
    }
    
    /**
     * Notification methods - only use notification-related fields
     */
    public void queueNotification(String message) {
        if (message != null && !message.trim().isEmpty()) {
            notificationQueue.offer(message);
        }
    }
    
    public boolean processNotificationQueue() {
        if (notificationQueue.isEmpty()) {
            return false;
        }
        
        while (!notificationQueue.isEmpty()) {
            String message = notificationQueue.poll();
            for (String user : subscribedUsers) {
                String formattedMessage = notificationTemplate
                    .replace("{user}", user)
                    .replace("{message}", message);
                System.out.println("Notification: " + formattedMessage);
            }
        }
        
        return true;
    }
    
    public void subscribeUser(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            subscribedUsers.add(userId);
        }
    }
    
    public void unsubscribeUser(String userId) {
        if (userId != null) {
            subscribedUsers.remove(userId);
        }
    }
    
    /**
     * Additional utility methods to increase WMC
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("System Report\n");
        report.append("=============\n");
        report.append("Cache entries: ").append(cacheData.size()).append("\n");
        report.append("Subscribed users: ").append(subscribedUsers.size()).append("\n");
        report.append("Pending notifications: ").append(notificationQueue.size()).append("\n");
        
        return report.toString();
    }
    
    public boolean validateInput(String input) {
        return input != null && !input.trim().isEmpty() && input.length() <= 1000;
    }
    
    public String formatTimestamp(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }
    
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("uptime", System.currentTimeMillis());
        stats.put("cacheSize", cacheData.size());
        stats.put("subscriberCount", subscribedUsers.size());
        stats.put("queueSize", notificationQueue.size());
        
        return stats;
    }
}