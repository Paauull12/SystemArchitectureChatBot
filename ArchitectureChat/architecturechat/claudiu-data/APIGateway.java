package com.example.short;

import java.util.*;

/**
 * Example 6: High CC + High Ce
 * Problems: CC: 22+, Ce: 15+
 * 
 * This API Gateway has complex routing logic and depends on many external services
 */
public class APIGateway {
    
    // External service dependencies (High Efferent Coupling)
    private HttpClient httpClient;
    private ObjectMapper jsonMapper;
    private RedisClient redisClient;
    private MetricsCollector metricsCollector;
    private SecurityValidator securityValidator;
    private RateLimiter rateLimiter;
    private CircuitBreaker circuitBreaker;
    private LoadBalancer loadBalancer;
    private LoggingService loggingService;
    private ConfigurationService configService;
    private HealthCheckService healthCheckService;
    private CacheManager cacheManager;
    private AlertManager alertManager;
    private AuditLogger auditLogger;
    private RequestTransformer requestTransformer;
    
    private final Map<String, String> routingTable = new HashMap<>();
    private final Set<String> blacklistedIPs = new HashSet<>();
    
    /**
     * Complex request handling method
     * Cyclomatic Complexity: 22+ due to nested conditions and routing logic
     */
    public APIResponse handleRequest(APIRequest request) {
        if (request == null) {
            metricsCollector.incrementInvalidRequests();
            return new APIResponse(400, "Bad Request", null);
        }
        
        // IP blacklist check (CC +2)
        if (blacklistedIPs.contains(request.getClientIP())) {
            auditLogger.logBlockedRequest(request);
            metricsCollector.incrementBlockedRequests();
            return new APIResponse(403, "Forbidden", null);
        }
        
        // Security validation with retry logic (CC +4)
        if (!securityValidator.isValid(request.getToken())) {
            if (request.getRetryCount() < 3) {
                if (securityValidator.canRetry(request.getToken())) {
                    APIRequest retryRequest = request.withIncrementedRetry();
                    loggingService.logRetry(retryRequest);
                    return handleRequest(retryRequest);
                } else {
                    metricsCollector.incrementSecurityFailures();
                    auditLogger.logSecurityFailure(request);
                    return new APIResponse(401, "Unauthorized", null);
                }
            } else {
                metricsCollector.incrementMaxRetriesExceeded();
                alertManager.sendSecurityAlert("Max retries exceeded for token: " + request.getToken());
                return new APIResponse(401, "Max retries exceeded", null);
            }
        }
        
        // Rate limiting with premium client logic (CC +5)
        if (!rateLimiter.allowRequest(request.getClientId())) {
            metricsCollector.incrementRateLimitExceeded();
            
            if (request.isPremiumClient()) {
                if (rateLimiter.allowPremiumRequest(request.getClientId())) {
                    loggingService.logPremiumBypass(request);
                } else {
                    return new APIResponse(429, "Premium rate limit exceeded", null);
                }
            } else {
                return new APIResponse(429, "Rate limit exceeded", null);
            }
        }
        
        // Request transformation based on client type (CC +3)
        APIRequest transformedRequest = request;
        if (request.requiresTransformation()) {
            try {
                transformedRequest = requestTransformer.transform(request);
                if (transformedRequest == null) {
                    return new APIResponse(400, "Request transformation failed", null);
                }
            } catch (Exception e) {
                loggingService.logError("Request transformation error", e);
                return new APIResponse(500, "Internal transformation error", null);
            }
        }
        
        // Circuit breaker and load balancing logic (CC +6)
        if (circuitBreaker.isOpen()) {
            if (circuitBreaker.shouldAttemptReset()) {
                String server = loadBalancer.getHealthyServer();
                if (server != null) {
                    try {
                        APIResponse response = forwardRequest(transformedRequest, server);
                        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                            circuitBreaker.recordSuccess();
                            cacheManager.cacheResponse(transformedRequest, response);
                            metricsCollector.recordSuccessfulRequest(transformedRequest, response);
                            return response;
                        } else {
                            circuitBreaker.recordFailure();
                            return response;
                        }
                    } catch (Exception e) {
                        circuitBreaker.recordFailure();
                        loggingService.logError("Request forwarding failed", e);
                        return new APIResponse(503, "Service Unavailable", null);
                    }
                } else {
                    alertManager.sendAlert("No healthy servers available");
                    return new APIResponse(503, "No healthy servers", null);
                }
            } else {
                metricsCollector.incrementCircuitBreakerBlocked();
                return new APIResponse(503, "Circuit breaker open", null);
            }
        }
        
        // Normal request processing with fallbacks (CC +4)
        try {
            // Check cache first
            APIResponse cachedResponse = cacheManager.getCachedResponse(transformedRequest);
            if (cachedResponse != null && !cachedResponse.isExpired()) {
                metricsCollector.incrementCacheHits();
                return cachedResponse;
            }
            
            String server = loadBalancer.getNextServer();
            if (server == null) {
                return new APIResponse(503, "No servers available", null);
            }
            
            APIResponse response = forwardRequest(transformedRequest, server);
            
            // Cache successful responses
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                cacheManager.cacheResponse(transformedRequest, response);
            }
            
            metricsCollector.recordRequest(transformedRequest, response);
            auditLogger.logRequest(transformedRequest, response);
            
            return response;
            
        } catch (Exception e) {
            metricsCollector.incrementErrors();
            loggingService.logError("Request processing failed", e);
            alertManager.sendErrorAlert("Request processing failed: " + e.getMessage());
            return new APIResponse(500, "Internal Server Error", null);
        }
    }
    
    /**
     * Request forwarding with timeout and retry logic
     */
    private APIResponse forwardRequest(APIRequest request, String serverUrl) throws Exception {
        // Get server-specific configuration
        int timeout = configService.getServerTimeout(serverUrl);
        int maxRetries = configService.getMaxRetries(serverUrl);
        
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                // Health check before forwarding
                if (!healthCheckService.isServerHealthy(serverUrl)) {
                    loadBalancer.markServerUnhealthy(serverUrl);
                    throw new Exception("Server is unhealthy: " + serverUrl);
                }
                
                // Forward the request
                String requestBody = jsonMapper.serialize(request);
                String responseBody = httpClient.post(serverUrl + request.getPath(), requestBody, timeout);
                
                APIResponse response = jsonMapper.deserialize(responseBody, APIResponse.class);
                
                if (response != null) {
                    return response;
                }
                
            } catch (Exception e) {
                lastException = e;
                loggingService.logWarning("Request attempt " + (attempt + 1) + " failed", e);
                
                if (attempt < maxRetries - 1) {
                    // Wait before retry
                    Thread.sleep(1000 * (attempt + 1));
                }
            }
        }
        
        throw new Exception("All retry attempts failed", lastException);
    }
    
    /**
     * Additional methods for configuration and monitoring
     */
    public void addRoute(String path, String targetServer) {
        if (path != null && targetServer != null) {
            routingTable.put(path, targetServer);
        }
    }
    
    public void blacklistIP(String ipAddress) {
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            blacklistedIPs.add(ipAddress);
            auditLogger.logIPBlacklisted(ipAddress);
        }
    }
    
    public GatewayStats getGatewayStats() {
        GatewayStats stats = new GatewayStats();
        stats.setTotalRequests(metricsCollector.getTotalRequests());
        stats.setSuccessfulRequests(metricsCollector.getSuccessfulRequests());
        stats.setFailedRequests(metricsCollector.getFailedRequests());
        stats.setCacheHitRate(metricsCollector.getCacheHitRate());
        stats.setCircuitBreakerStatus(circuitBreaker.getStatus());
        
        return stats;
    }
    
    public boolean performHealthCheck() {
        try {
            boolean allSystemsHealthy = true;
            
            if (!redisClient.isConnected()) {
                allSystemsHealthy = false;
            }
            
            if (!circuitBreaker.isHealthy()) {
                allSystemsHealthy = false;
            }
            
            if (!loadBalancer.hasHealthyServers()) {
                allSystemsHealthy = false;
            }
            
            return allSystemsHealthy;
            
        } catch (Exception e) {
            loggingService.logError("Health check failed", e);
            return false;
        }
    }
}

// Supporting classes and interfaces

class APIRequest {
    private String clientId;
    private String clientIP;
    private String token;
    private String path;
    private Map<String, Object> data;
    private int retryCount = 0;
    private boolean premiumClient = false;
    private boolean requiresTransformation = false;
    
    // Getters and setters
    public String getClientId() { return clientId; }
    public String getClientIP() { return clientIP; }
    public String getToken() { return token; }
    public String getPath() { return path; }
    public Map<String, Object> getData() { return data; }
    public int getRetryCount() { return retryCount; }
    public boolean isPremiumClient() { return premiumClient; }
    public boolean requiresTransformation() { return requiresTransformation; }
    
    public APIRequest withIncrementedRetry() {
        APIRequest newRequest = new APIRequest();
        newRequest.clientId = this.clientId;
        newRequest.clientIP = this.clientIP;
        newRequest.token = this.token;
        newRequest.path = this.path;
        newRequest.data = this.data;
        newRequest.retryCount = this.retryCount + 1;
        newRequest.premiumClient = this.premiumClient;
        newRequest.requiresTransformation = this.requiresTransformation;
        return newRequest;
    }
}

class APIResponse {
    private int statusCode;
    private String message;
    private Object data;
    private long timestamp;
    private long expiryTime;
    
    public APIResponse(int statusCode, String message, Object data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.expiryTime = timestamp + 300000; // 5 minutes
    }
    
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
    public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
}

class GatewayStats {
    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private double cacheHitRate;
    private String circuitBreakerStatus;
    
    // Getters and setters
    public void setTotalRequests(long total) { this.totalRequests = total; }
    public void setSuccessfulRequests(long successful) { this.successfulRequests = successful; }
    public void setFailedRequests(long failed) { this.failedRequests = failed; }
    public void setCacheHitRate(double rate) { this.cacheHitRate = rate; }
    public void setCircuitBreakerStatus(String status) { this.circuitBreakerStatus = status; }
}

// External service interfaces (High Ce dependencies)
interface HttpClient {
    String post(String url, String body, int timeout) throws Exception;
}

interface ObjectMapper {
    String serialize(Object obj) throws Exception;
    <T> T deserialize(String json, Class<T> clazz) throws Exception;
}

interface RedisClient {
    boolean isConnected();
}

interface MetricsCollector {
    void incrementInvalidRequests();
    void incrementBlockedRequests();
    void incrementSecurityFailures();
    void incrementMaxRetriesExceeded();
    void incrementRateLimitExceeded();
    void incrementCircuitBreakerBlocked();
    void incrementCacheHits();
    void incrementErrors();
    void recordRequest(APIRequest request, APIResponse response);
    void recordSuccessfulRequest(APIRequest request, APIResponse response);
    
    long getTotalRequests();
    long getSuccessfulRequests();
    long getFailedRequests();
    double getCacheHitRate();
}

interface SecurityValidator {
    boolean isValid(String token);
    boolean canRetry(String token);
}

interface RateLimiter {
    boolean allowRequest(String clientId);
    boolean allowPremiumRequest(String clientId);
}

interface CircuitBreaker {
    boolean isOpen();
    boolean shouldAttemptReset();
    void recordSuccess();
    void recordFailure();
    boolean isHealthy();
    String getStatus();
}

interface LoadBalancer {
    String getHealthyServer();
    String getNextServer();
    void markServerUnhealthy(String server);
    boolean hasHealthyServers();
}

interface LoggingService {
    void logRetry(APIRequest request);
    void logPremiumBypass(APIRequest request);
    void logError(String message, Exception e);
    void logWarning(String message, Exception e);
}

interface ConfigurationService {
    int getServerTimeout(String server);
    int getMaxRetries(String server);
}

interface HealthCheckService {
    boolean isServerHealthy(String server);
}

interface CacheManager {
    APIResponse getCachedResponse(APIRequest request);
    void cacheResponse(APIRequest request, APIResponse response);
}

interface AlertManager {
    void sendSecurityAlert(String message);
    void sendAlert(String message);
    void sendErrorAlert(String message);
}

interface AuditLogger {
    void logBlockedRequest(APIRequest request);
    void logSecurityFailure(APIRequest request);
    void logRequest(APIRequest request, APIResponse response);
    void logIPBlacklisted(String ip);
}

interface RequestTransformer {
    APIRequest transform(APIRequest request) throws Exception;
}