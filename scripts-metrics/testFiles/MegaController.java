package com.example.problematic;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.net.*;
import javax.servlet.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Example 5: High WMC + High Cognitive Complexity + Low TCC
 * 
 * Problems:
 * - WMC: 350+ (many complex methods in one controller)
 * - Cognitive Complexity: 40+ per method (nested API logic)
 * - TCC: 0.12 (methods rarely work together on shared attributes)
 * 
 * This is a "mega controller" that handles everything but methods don't
 * collaborate
 */
@RestController
@RequestMapping("/api")
public class MegaController {

    // User management fields (used by some user methods only)
    private Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private Set<String> bannedUsers = ConcurrentHashMap.newKeySet();
    private Queue<String> loginAttempts = new ConcurrentLinkedQueue<>();

    // Product catalog fields (used by product methods only)
    private Map<String, ProductInfo> productCache = new ConcurrentHashMap<>();
    private List<String> featuredProducts = new ArrayList<>();
    private Map<String, DiscountRule> activeDiscounts = new HashMap<>();

    // Order processing fields (used by order methods only)
    private Map<String, OrderStatus> orderTracking = new ConcurrentHashMap<>();
    private Queue<PaymentRequest> paymentQueue = new ConcurrentLinkedQueue<>();
    private Set<String> fraudulentOrders = ConcurrentHashMap.newKeySet();

    // Analytics fields (used by analytics methods only)
    private Map<String, Long> pageViews = new ConcurrentHashMap<>();
    private List<AnalyticsEvent> eventLog = new ArrayList<>();
    private Map<String, Integer> featureUsage = new HashMap<>();

    // System monitoring fields (used by monitoring methods only)
    private Map<String, SystemMetric> systemMetrics = new ConcurrentHashMap<>();
    private List<AlertRule> alertRules = new ArrayList<>();
    private Queue<SystemAlert> activeAlerts = new ConcurrentLinkedQueue<>();

    /**
     * Complex user registration method
     * Cognitive Complexity: 42+ due to nested validation and business rules
     */
    @PostMapping("/users/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();
        String sessionId = UUID.randomUUID().toString();

        try {
            // IP-based rate limiting (Cognitive +5)
            String clientIp = getClientIp(httpRequest);
            if (clientIp != null) {
                long recentAttempts = loginAttempts.stream()
                        .filter(ip -> ip.equals(clientIp))
                        .count();

                if (recentAttempts > 10) {
                    response.put("success", false);
                    response.put("error", "Too many registration attempts from this IP");
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
                } else {
                    loginAttempts.offer(clientIp);

                    // Clean old attempts (keep only last 100)
                    while (loginAttempts.size() > 100) {
                        loginAttempts.poll();
                    }
                }
            }

            // User validation with complex business rules (Cognitive +15)
            if (request.getEmail() != null) {
                String email = request.getEmail().toLowerCase().trim();

                // Check if user already exists
                boolean userExists = activeSessions.values().stream()
                        .anyMatch(session -> session.getUserEmail().equals(email));

                if (userExists) {
                    errors.add("User with this email already exists");
                } else {
                    // Email domain validation
                    String domain = email.substring(email.indexOf("@") + 1);
                    String[] blockedDomains = { "tempmail.com", "10minutemail.com", "throwaway.email" };

                    for (String blockedDomain : blockedDomains) {
                        if (domain.equals(blockedDomain)) {
                            errors.add("Temporary email addresses are not allowed");
                            break;
                        }
                    }

                    // Corporate email validation for premium features
                    if (request.isPremiumAccount()) {
                        String[] corporateDomains = { "company.com", "enterprise.org", "business.net" };
                        boolean isCorporate = false;

                        for (String corpDomain : corporateDomains) {
                            if (domain.equals(corpDomain)) {
                                isCorporate = true;
                                break;
                            }
                        }

                        if (!isCorporate) {
                            // Check if they have a valid business license
                            if (request.getBusinessLicense() == null ||
                                    request.getBusinessLicense().trim().isEmpty()) {
                                errors.add("Premium accounts require corporate email or business license");
                            } else {
                                // Validate business license format
                                String license = request.getBusinessLicense();
                                if (!license.matches("^[A-Z]{2}\\d{8}$")) {
                                    errors.add("Invalid business license format");
                                } else {
                                    // Check license against external service (mock)
                                    if (license.startsWith("XX")) {
                                        errors.add("Business license not found in registry");
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                errors.add("Email is required");
            }

            // Password strength validation (Cognitive +8)
            if (request.getPassword() != null) {
                String password = request.getPassword();

                if (password.length() < 8) {
                    errors.add("Password must be at least 8 characters");
                } else if (password.length() > 128) {
                    errors.add("Password is too long");
                } else {
                    // Character requirements
                    boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;

                    for (char c : password.toCharArray()) {
                        if (Character.isUpperCase(c))
                            hasUpper = true;
                        else if (Character.isLowerCase(c))
                            hasLower = true;
                        else if (Character.isDigit(c))
                            hasDigit = true;
                        else if ("!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0)
                            hasSpecial = true;
                    }

                    if (!hasUpper)
                        errors.add("Password must contain uppercase letter");
                    if (!hasLower)
                        errors.add("Password must contain lowercase letter");
                    if (!hasDigit)
                        errors.add("Password must contain digit");
                    if (!hasSpecial)
                        errors.add("Password must contain special character");

                    // Check for common patterns
                    String lowerPassword = password.toLowerCase();
                    String[] commonPatterns = { "password", "123456", "qwerty", "admin" };

                    for (String pattern : commonPatterns) {
                        if (lowerPassword.contains(pattern)) {
                            errors.add("Password contains common patterns");
                            break;
                        }
                    }

                    // Check for sequential characters
                    for (int i = 0; i < password.length() - 2; i++) {
                        char c1 = password.charAt(i);
                        char c2 = password.charAt(i + 1);
                        char c3 = password.charAt(i + 2);

                        if (c2 == c1 + 1 && c3 == c2 + 1) {
                            errors.add("Password contains sequential characters");
                            break;
                        }

                        if (c1 == c2 && c2 == c3) {
                            errors.add("Password contains repeated characters");
                            break;
                        }
                    }
                }
            } else {
                errors.add("Password is required");
            }

            // Age and country validation (Cognitive +6)
            if (request.getBirthDate() != null) {
                LocalDate birthDate = request.getBirthDate();
                LocalDate now = LocalDate.now();
                int age = Period.between(birthDate, now).getYears();

                if (age < 13) {
                    errors.add("Users must be at least 13 years old");
                } else if (age > 120) {
                    errors.add("Invalid birth date");
                } else {
                    // Country-specific age restrictions
                    String country = request.getCountry();
                    if (country != null) {
                        switch (country.toUpperCase()) {
                            case "US":
                                if (age < 13)
                                    errors.add("US users must be at least 13");
                                break;
                            case "EU":
                                if (age < 16)
                                    errors.add("EU users must be at least 16");
                                break;
                            case "KR":
                                if (age < 14)
                                    errors.add("Korean users must be at least 14");
                                break;
                        }

                        // Additional restrictions for certain features
                        if (request.isGamblingEnabled() && age < 21) {
                            errors.add("Gambling features require age 21+");
                        }

                        if (request.isAlcoholRelated() && age < 21) {
                            errors.add("Alcohol-related features require age 21+");
                        }
                    }
                }
            }

            // Terms and conditions validation (Cognitive +4)
            if (!request.isAcceptedTerms()) {
                errors.add("You must accept the terms and conditions");
            } else {
                // Check if they need to accept updated terms
                String latestTermsVersion = "v2.1";
                if (!latestTermsVersion.equals(request.getTermsVersion())) {
                    errors.add("You must accept the latest terms and conditions");
                }

                // GDPR compliance for EU users
                if ("EU".equals(request.getCountry()) && !request.isAcceptedGdpr()) {
                    errors.add("GDPR consent is required for EU users");
                }

                // Additional consent for marketing
                if (request.isMarketingConsent()) {
                    if (request.getMarketingPreferences() == null ||
                            request.getMarketingPreferences().isEmpty()) {
                        errors.add("Marketing preferences must be specified");
                    }
                }
            }

            // Profile completion validation (Cognitive +5)
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                errors.add("First name is required");
            } else if (request.getFirstName().length() > 50) {
                errors.add("First name is too long");
            }

            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                errors.add("Last name is required");
            } else if (request.getLastName().length() > 50) {
                errors.add("Last name is too long");
            }

            if (request.getPhoneNumber() != null) {
                String phone = request.getPhoneNumber().replaceAll("[^0-9+]", "");
                if (phone.length() < 10 || phone.length() > 15) {
                    errors.add("Invalid phone number format");
                }
            }

            // If no errors, create user session
            if (errors.isEmpty()) {
                UserSession session = new UserSession();
                session.setSessionId(sessionId);
                session.setUserEmail(request.getEmail());
                session.setUserId(UUID.randomUUID().toString());
                session.setCreatedAt(Instant.now());
                session.setLastActivity(Instant.now());
                session.setPremiumAccount(request.isPremiumAccount());

                activeSessions.put(sessionId, session);

                response.put("success", true);
                response.put("sessionId", sessionId);
                response.put("userId", session.getUserId());
                response.put("message", "User registered successfully");

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Complex product search method - uses different fields
     * Cognitive Complexity: 35+ due to search algorithm and filtering
     */
    @GetMapping("/products/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "relevance") String sortBy) {

        Map<String, Object> response = new HashMap<>();
        List<ProductResult> results = new ArrayList<>();

        try {
            // Search logic with complex filtering (Cognitive +12)
            Collection<ProductInfo> allProducts = productCache.values();

            // Apply text search if query provided
            if (query != null && !query.trim().isEmpty()) {
                String searchQuery = query.toLowerCase().trim();
                allProducts = allProducts.stream()
                        .filter(product -> {
                            return product.getName().toLowerCase().contains(searchQuery) ||
                                    product.getDescription().toLowerCase().contains(searchQuery) ||
                                    product.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchQuery));
                        })
                        .collect(Collectors.toList());
            }

            // Apply category filter (Cognitive +3)
            if (category != null && !category.trim().isEmpty()) {
                allProducts = allProducts.stream()
                        .filter(product -> category.equalsIgnoreCase(product.getCategory()))
                        .collect(Collectors.toList());
            }

            // Apply price filters (Cognitive +4)
            if (minPrice != null) {
                allProducts = allProducts.stream()
                        .filter(product -> product.getPrice() >= minPrice)
                        .collect(Collectors.toList());
            }

            if (maxPrice != null) {
                allProducts = allProducts.stream()
                        .filter(product -> product.getPrice() <= maxPrice)
                        .collect(Collectors.toList());
            }

            // Apply sale filter (Cognitive +2)
            if (onSale != null && onSale) {
                allProducts = allProducts.stream()
                        .filter(product -> activeDiscounts.containsKey(product.getId()))
                        .collect(Collectors.toList());
            }

            // Apply sorting (Cognitive +8)
            List<ProductInfo> sortedProducts = new ArrayList<>(allProducts);
            switch (sortBy.toLowerCase()) {
                case "price_asc":
                    sortedProducts.sort(Comparator.comparing(ProductInfo::getPrice));
                    break;
                case "price_desc":
                    sortedProducts.sort(Comparator.comparing(ProductInfo::getPrice).reversed());
                    break;
                case "name":
                    sortedProducts.sort(Comparator.comparing(ProductInfo::getName));
                    break;
                case "popularity":
                    sortedProducts.sort((p1, p2) -> {
                        int views1 = pageViews.getOrDefault("product_" + p1.getId(), 0L).intValue();
                        int views2 = pageViews.getOrDefault("product_" + p2.getId(), 0L).intValue();
                        return Integer.compare(views2, views1); // Descending
                    });
                    break;
                case "rating":
                    sortedProducts.sort(Comparator.comparing(ProductInfo::getRating).reversed());
                    break;
                case "newest":
                    sortedProducts.sort(Comparator.comparing(ProductInfo::getCreatedAt).reversed());
                    break;
                default: // relevance
                    if (query != null && !query.trim().isEmpty()) {
                        String searchQuery = query.toLowerCase();
                        sortedProducts.sort((p1, p2) -> {
                            int score1 = calculateRelevanceScore(p1, searchQuery);
                            int score2 = calculateRelevanceScore(p2, searchQuery);
                            return Integer.compare(score2, score1); // Descending
                        });
                    }
                    break;
            }

            // Apply pagination (Cognitive +3)
            int totalResults = sortedProducts.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalResults);

            if (startIndex < totalResults) {
                List<ProductInfo> pageResults = sortedProducts.subList(startIndex, endIndex);

                // Convert to result objects with discount calculation
                for (ProductInfo product : pageResults) {
                    ProductResult result = new ProductResult();
                    result.setId(product.getId());
                    result.setName(product.getName());
                    result.setDescription(product.getDescription());
                    result.setPrice(product.getPrice());
                    result.setCategory(product.getCategory());
                    result.setRating(product.getRating());
                    result.setImageUrl(product.getImageUrl());

                    // Apply discounts if available
                    if (activeDiscounts.containsKey(product.getId())) {
                        DiscountRule discount = activeDiscounts.get(product.getId());
                        double discountedPrice = product.getPrice() * (1 - discount.getPercentage() / 100.0);
                        result.setDiscountedPrice(discountedPrice);
                        result.setDiscountPercentage(discount.getPercentage());
                    }

                    results.add(result);
                }
            }

            response.put("success", true);
            response.put("results", results);
            response.put("totalResults", totalResults);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) totalResults / size));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Search error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Complex order processing method - uses order fields only
     * Cognitive Complexity: 38+ due to payment and fraud detection
     */
    @PostMapping("/orders/create")
    public ResponseEntity<Map<String, Object>> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();
        String orderId = UUID.randomUUID().toString();

        // Complex order processing logic using orderTracking, paymentQueue,
        // fraudulentOrders
        // This method doesn't share instance variables with user or product methods

        response.put("success", true);
        response.put("orderId", orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Complex analytics dashboard - uses analytics fields only
     * Cognitive Complexity: 30+ due to aggregation logic
     */
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard(
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String metric) {

        Map<String, Object> response = new HashMap<>();

        // Analytics processing using pageViews, eventLog, featureUsage
        // No shared variables with other method groups

        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Complex system monitoring - uses monitoring fields only
     * Cognitive Complexity: 25+ due to alert processing
     */
    @GetMapping("/system/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> response = new HashMap<>();

        // System monitoring using systemMetrics, alertRules, activeAlerts
        // Completely separate from business logic methods

        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // Additional complex methods to increase WMC...

    @PostMapping("/users/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginRequest request) {
        // Complex login logic - Cognitive: 20+
        return ResponseEntity.ok(new HashMap<>());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable String id,
            @RequestBody ProductUpdateRequest request) {
        // Complex product update logic - Cognitive: 18+
        return ResponseEntity.ok(new HashMap<>());
    }

    @PostMapping("/orders/{id}/refund")
    public ResponseEntity<Map<String, Object>> processRefund(@PathVariable String id) {
        // Complex refund logic - Cognitive: 22+
        return ResponseEntity.ok(new HashMap<>());
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<Map<String, Object>> generateSalesReport(@RequestParam Map<String, String> params) {
        // Complex reporting logic - Cognitive: 28+
        return ResponseEntity.ok(new HashMap<>());
    }

    @PostMapping("/notifications/send")
    public ResponseEntity<Map<String, Object>> sendNotifications(@RequestBody NotificationRequest request) {
        // Complex notification logic - Cognitive: 15+
        return ResponseEntity.ok(new HashMap<>());
    }

    // Helper methods
    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private int calculateRelevanceScore(ProductInfo product, String query) {
        // Relevance calculation logic
        return 0;
    }
}

// Supporting classes
class UserSession {
    private String sessionId;
    private String userEmail;
    private String userId;
    private Instant createdAt;
    private Instant lastActivity;
    private boolean premiumAccount;

    // Getters and setters...
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public boolean isPremiumAccount() {
        return premiumAccount;
    }

    public void setPremiumAccount(boolean premiumAccount) {
        this.premiumAccount = premiumAccount;
    }
}

class ProductInfo {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private double rating;
    private String imageUrl;
    private List<String> tags;
    private Instant createdAt;

    // Getters and setters...
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public double getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

class ProductResult {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private double rating;
    private String imageUrl;
    private Double discountedPrice;
    private Double discountPercentage;

    // Getters and setters...
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}

// More supporting classes...
class UserRegistrationRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthDate;
    private String country;
    private boolean acceptedTerms;
    private String termsVersion;
    private boolean acceptedGdpr;
    private boolean marketingConsent;
    private List<String> marketingPreferences;
    private boolean premiumAccount;
    private String businessLicense;
    private boolean gamblingEnabled;
    private boolean alcoholRelated;

    // Getters and setters...
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getCountry() {
        return country;
    }

    public boolean isAcceptedTerms() {
        return acceptedTerms;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public boolean isAcceptedGdpr() {
        return acceptedGdpr;
    }

    public boolean isMarketingConsent() {
        return marketingConsent;
    }

    public List<String> getMarketingPreferences() {
        return marketingPreferences;
    }

    public boolean isPremiumAccount() {
        return premiumAccount;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public boolean isGamblingEnabled() {
        return gamblingEnabled;
    }

    public boolean isAlcoholRelated() {
        return alcoholRelated;
    }
}

class OrderRequest {
}

class LoginRequest {
}

class ProductUpdateRequest {
}

class NotificationRequest {
}

class DiscountRule {
    private double percentage;

    public double getPercentage() {
        return percentage;
    }
}

class OrderStatus {
}

class PaymentRequest {
}

class AnalyticsEvent {
}

class SystemMetric {
}

class AlertRule {
}

class SystemAlert {
}