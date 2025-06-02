package com.example.problematic;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.net.*;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.env.Environment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.KeeperException;
import redis.clients.jedis.Jedis;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import org.apache.curator.framework.CuratorFramework;
import org.etcd.jetcd.Client;
import org.etcd.jetcd.KV;
import org.springframework.vault.core.VaultTemplate;

/**
 * Example 7: High Cyclomatic Complexity + High Instability + High Afferent
 * Coupling
 * 
 * Problems:
 * - Cyclomatic Complexity: 40+ per method (complex config loading logic)
 * - Instability: 0.94+ (depends on many volatile config systems)
 * - Afferent Coupling: 60+ (every service needs configuration)
 * 
 * This configuration manager is used by everyone but is highly unstable
 */
public class ConfigurationManager {

    // File-based configuration dependencies (change frequently)
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final Yaml yamlProcessor = new Yaml();
    private final DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    // External configuration service dependencies (highly volatile)
    private Environment springEnvironment;
    private ConfigClientProperties springCloudConfig;
    private ConsulConfigProperties consulConfig;
    private ZooKeeper zookeeperClient;
    private CuratorFramework curatorClient;
    private Client etcdClient;
    private VaultTemplate vaultTemplate;
    private Jedis redisClient;
    private AmazonS3 s3Client;
    private AWSSecretsManager secretsManager;

    // Configuration caching and state
    private final Map<String, ConfigValue> configCache = new ConcurrentHashMap<>();
    private final Map<String, Long> configTimestamps = new ConcurrentHashMap<>();
    private final Map<String, ConfigSource> configSources = new ConcurrentHashMap<>();
    private final Set<String> encryptedKeys = ConcurrentHashMap.newKeySet();
    private final Queue<ConfigChangeEvent> changeEvents = new ConcurrentLinkedQueue<>();

    // Configuration refresh and monitoring
    private final ScheduledExecutorService refreshScheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, WatchService> fileWatchers = new ConcurrentHashMap<>();
    private final Map<String, Boolean> sourceAvailability = new ConcurrentHashMap<>();

    /**
     * Complex configuration loading method
     * CC: 45+ due to multiple sources and fallback logic
     * HIGH Ca - called by every service in the application
     */
    public ConfigValue getConfiguration(String key, ConfigurationRequest request) {

        // Input validation with complex branching (CC +8)
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration key cannot be null or empty");
        }

        String normalizedKey = normalizeConfigKey(key);
        ConfigValue cachedValue = null;

        // Cache validation with expiry logic (CC +6)
        if (request.isUseCache() && configCache.containsKey(normalizedKey)) {
            cachedValue = configCache.get(normalizedKey);
            Long timestamp = configTimestamps.get(normalizedKey);

            if (timestamp != null) {
                long cacheAge = System.currentTimeMillis() - timestamp;
                long maxAge = request.getCacheMaxAgeMs();

                if (maxAge <= 0 || cacheAge < maxAge) {
                    // Check if cached value is still valid
                    if (cachedValue != null && !cachedValue.isExpired()) {
                        return cachedValue;
                    } else if (cachedValue != null && cachedValue.isExpired()) {
                        // Remove expired cache entry
                        configCache.remove(normalizedKey);
                        configTimestamps.remove(normalizedKey);
                    }
                }
            }
        }

        // Configuration source priority handling (CC +15)
        List<ConfigSource> sources = determinePrioritizedSources(request);
        ConfigValue result = null;
        List<String> errors = new ArrayList<>();

        for (ConfigSource source : sources) {
            try {
                // Check source availability first
                if (!isSourceAvailable(source)) {
                    errors.add("Source " + source.getName() + " is not available");
                    continue;
                }

                // Load configuration from specific source
                switch (source.getType()) {
                    case FILE_PROPERTIES:
                        result = loadFromPropertiesFile(normalizedKey, source, request);
                        break;

                    case FILE_YAML:
                        result = loadFromYamlFile(normalizedKey, source, request);
                        break;

                    case FILE_JSON:
                        result = loadFromJsonFile(normalizedKey, source, request);
                        break;

                    case FILE_XML:
                        result = loadFromXmlFile(normalizedKey, source, request);
                        break;

                    case SPRING_ENVIRONMENT:
                        result = loadFromSpringEnvironment(normalizedKey, source, request);
                        break;

                    case SPRING_CLOUD_CONFIG:
                        result = loadFromSpringCloudConfig(normalizedKey, source, request);
                        break;

                    case CONSUL:
                        result = loadFromConsul(normalizedKey, source, request);
                        break;

                    case ZOOKEEPER:
                        result = loadFromZookeeper(normalizedKey, source, request);
                        break;

                    case ETCD:
                        result = loadFromEtcd(normalizedKey, source, request);
                        break;

                    case VAULT:
                        result = loadFromVault(normalizedKey, source, request);
                        break;

                    case REDIS:
                        result = loadFromRedis(normalizedKey, source, request);
                        break;

                    case AWS_SECRETS_MANAGER:
                        result = loadFromAwsSecretsManager(normalizedKey, source, request);
                        break;

                    case AWS_S3:
                        result = loadFromS3(normalizedKey, source, request);
                        break;

                    case ENVIRONMENT_VARIABLES:
                        result = loadFromEnvironmentVariables(normalizedKey, source, request);
                        break;

                    case SYSTEM_PROPERTIES:
                        result = loadFromSystemProperties(normalizedKey, source, request);
                        break;

                    default:
                        errors.add("Unknown source type: " + source.getType());
                        continue;
                }

                // Value found, apply post-processing (CC +8)
                if (result != null && result.getValue() != null) {

                    // Decryption if needed
                    if (encryptedKeys.contains(normalizedKey) ||
                            (result.isEncrypted() && request.isAutoDecrypt())) {
                        try {
                            String decryptedValue = decryptValue(result.getValue().toString(), source);
                            result = new ConfigValue(decryptedValue, result.getSource(), result.getType());
                        } catch (Exception e) {
                            if (request.isFailOnDecryptionError()) {
                                throw new ConfigurationException("Failed to decrypt value for key: " + normalizedKey,
                                        e);
                            } else {
                                errors.add("Decryption failed for key " + normalizedKey + ": " + e.getMessage());
                                continue;
                            }
                        }
                    }

                    // Type conversion if requested
                    if (request.getTargetType() != null &&
                            !request.getTargetType().equals(result.getType())) {
                        try {
                            Object convertedValue = convertValue(result.getValue(), request.getTargetType());
                            result = new ConfigValue(convertedValue, result.getSource(), request.getTargetType());
                        } catch (Exception e) {
                            if (request.isFailOnConversionError()) {
                                throw new ConfigurationException("Failed to convert value for key: " + normalizedKey,
                                        e);
                            } else {
                                errors.add("Type conversion failed for key " + normalizedKey + ": " + e.getMessage());
                                continue;
                            }
                        }
                    }

                    // Validation if specified
                    if (request.getValidator() != null) {
                        try {
                            if (!request.getValidator().isValid(result.getValue())) {
                                if (request.isFailOnValidationError()) {
                                    throw new ConfigurationException("Validation failed for key: " + normalizedKey);
                                } else {
                                    errors.add("Validation failed for key " + normalizedKey);
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            if (request.isFailOnValidationError()) {
                                throw new ConfigurationException("Validation error for key: " + normalizedKey, e);
                            } else {
                                errors.add("Validation error for key " + normalizedKey + ": " + e.getMessage());
                                continue;
                            }
                        }
                    }

                    // Cache the result if caching is enabled
                    if (request.isUseCache()) {
                        configCache.put(normalizedKey, result);
                        configTimestamps.put(normalizedKey, System.currentTimeMillis());
                        configSources.put(normalizedKey, source);
                    }

                    // Fire configuration change event if this is a refresh
                    if (cachedValue != null && !Objects.equals(cachedValue.getValue(), result.getValue())) {
                        ConfigChangeEvent event = new ConfigChangeEvent(
                                normalizedKey,
                                cachedValue.getValue(),
                                result.getValue(),
                                source.getName());
                        changeEvents.offer(event);
                        notifyConfigurationListeners(event);
                    }

                    return result;
                }

            } catch (Exception e) {
                errors.add("Error loading from " + source.getName() + ": " + e.getMessage());

                // Mark source as unavailable temporarily
                sourceAvailability.put(source.getName(), false);
                scheduleSourceHealthCheck(source);
            }
        }

        // No value found in any source, handle fallbacks (CC +5)
        if (request.getDefaultValue() != null) {
            result = new ConfigValue(
                    request.getDefaultValue(),
                    "default",
                    request.getDefaultValue().getClass());

            if (request.isUseCache()) {
                configCache.put(normalizedKey, result);
                configTimestamps.put(normalizedKey, System.currentTimeMillis());
            }

            return result;
        } else if (request.isRequired()) {
            throw new ConfigurationException(
                    "Required configuration key '" + normalizedKey + "' not found in any source. Errors: " +
                            String.join(", ", errors));
        } else {
            return new ConfigValue(null, "not_found", Object.class);
        }
    }

    /**
     * Complex configuration refresh method
     * CC: 35+ due to source checking and update logic
     * HIGH Ca - called by many monitoring and refresh systems
     */
    public ConfigurationRefreshResult refreshConfiguration(ConfigurationRefreshRequest refreshRequest) {
        ConfigurationRefreshResult result = new ConfigurationRefreshResult();
        List<String> refreshedKeys = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();

        try {
            // Determine what to refresh based on request (CC +8)
            Set<String> keysToRefresh = new HashSet<>();

            if (refreshRequest.isRefreshAll()) {
                keysToRefresh.addAll(configCache.keySet());
            } else if (refreshRequest.getSpecificKeys() != null && !refreshRequest.getSpecificKeys().isEmpty()) {
                keysToRefresh.addAll(refreshRequest.getSpecificKeys());
            } else if (refreshRequest.getKeyPattern() != null) {
                Pattern pattern = Pattern.compile(refreshRequest.getKeyPattern());
                keysToRefresh.addAll(
                        configCache.keySet().stream()
                                .filter(key -> pattern.matcher(key).matches())
                                .collect(Collectors.toSet()));
            } else if (refreshRequest.getSource() != null) {
                // Refresh all keys from a specific source
                keysToRefresh.addAll(
                        configSources.entrySet().stream()
                                .filter(entry -> entry.getValue().getName().equals(refreshRequest.getSource()))
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toSet()));
            }

            // Source health check before refresh (CC +6)
            if (refreshRequest.isCheckSourceHealth()) {
                for (ConfigSource source : getAllConfigSources()) {
                    try {
                        boolean isHealthy = performSourceHealthCheck(source);
                        sourceAvailability.put(source.getName(), isHealthy);

                        if (!isHealthy) {
                            errors.add("Source " + source.getName() + " failed health check");

                            // Remove keys from refresh list if their primary source is unhealthy
                            if (refreshRequest.isSkipUnhealthySources()) {
                                keysToRefresh.removeIf(key -> {
                                    ConfigSource keySource = configSources.get(key);
                                    return keySource != null && keySource.getName().equals(source.getName());
                                });
                            }
                        }
                    } catch (Exception e) {
                        errors.add("Health check failed for " + source.getName() + ": " + e.getMessage());
                        sourceAvailability.put(source.getName(), false);
                    }
                }
            }

            // Parallel refresh if requested (CC +4)
            if (refreshRequest.isParallelRefresh() && keysToRefresh.size() > 10) {
                List<CompletableFuture<Void>> refreshTasks = new ArrayList<>();

                for (String key : keysToRefresh) {
                    CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                        try {
                            refreshSingleKey(key, refreshRequest, refreshedKeys, errors, oldValues, newValues);
                        } catch (Exception e) {
                            synchronized (errors) {
                                errors.add("Parallel refresh failed for key " + key + ": " + e.getMessage());
                            }
                        }
                    });
                    refreshTasks.add(task);
                }

                // Wait for all tasks to complete
                CompletableFuture.allOf(refreshTasks.toArray(new CompletableFuture[0]))
                        .get(refreshRequest.getTimeoutMs(), TimeUnit.MILLISECONDS);

            } else {
                // Sequential refresh
                for (String key : keysToRefresh) {
                    try {
                        refreshSingleKey(key, refreshRequest, refreshedKeys, errors, oldValues, newValues);
                    } catch (Exception e) {
                        errors.add("Sequential refresh failed for key " + key + ": " + e.getMessage());
                    }
                }
            }

            // Post-refresh validation (CC +6)
            if (refreshRequest.isValidateAfterRefresh()) {
                for (String key : refreshedKeys) {
                    try {
                        ConfigValue currentValue = configCache.get(key);
                        if (currentValue != null && refreshRequest.getPostRefreshValidator() != null) {
                            if (!refreshRequest.getPostRefreshValidator().isValid(currentValue.getValue())) {
                                errors.add("Post-refresh validation failed for key: " + key);

                                // Rollback if requested
                                if (refreshRequest.isRollbackOnValidationFailure() && oldValues.containsKey(key)) {
                                    ConfigValue oldValue = new ConfigValue(
                                            oldValues.get(key),
                                            "rollback",
                                            oldValues.get(key).getClass());
                                    configCache.put(key, oldValue);
                                    configTimestamps.put(key, System.currentTimeMillis());
                                }
                            }
                        }
                    } catch (Exception e) {
                        errors.add("Post-refresh validation error for key " + key + ": " + e.getMessage());
                    }
                }
            }

            // Notification and event handling (CC +4)
            if (!refreshedKeys.isEmpty()) {
                try {
                    // Notify all registered listeners
                    for (String key : refreshedKeys) {
                        if (oldValues.containsKey(key) && newValues.containsKey(key)) {
                            ConfigChangeEvent event = new ConfigChangeEvent(
                                    key,
                                    oldValues.get(key),
                                    newValues.get(key),
                                    "refresh");
                            changeEvents.offer(event);
                            notifyConfigurationListeners(event);
                        }
                    }

                    // Publish refresh event to external systems
                    if (refreshRequest.isPublishRefreshEvents()) {
                        publishRefreshEvent(refreshedKeys, oldValues, newValues);
                    }

                } catch (Exception e) {
                    errors.add("Notification error: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            errors.add("Unexpected refresh error: " + e.getMessage());
        }

        result.setSuccess(errors.isEmpty() || !refreshedKeys.isEmpty());
        result.setRefreshedKeys(refreshedKeys);
        result.setErrors(errors);
        result.setOldValues(oldValues);
        result.setNewValues(newValues);
        result.setRefreshTimestamp(Instant.now());

        return result;
    }

    /**
     * Complex configuration backup method
     * CC: 25+ due to multiple backup destinations and formats
     * HIGH Ca - called by backup services, monitoring systems, deployment tools
     */
    public ConfigurationBackupResult backupConfiguration(ConfigurationBackupRequest backupRequest) {
        ConfigurationBackupResult result = new ConfigurationBackupResult();
        List<String> backupLocations = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            // Determine backup scope (CC +5)
            Map<String, ConfigValue> configToBackup = new HashMap<>();

            if (backupRequest.isBackupAll()) {
                configToBackup.putAll(configCache);
            } else if (backupRequest.getSpecificKeys() != null) {
                for (String key : backupRequest.getSpecificKeys()) {
                    if (configCache.containsKey(key)) {
                        configToBackup.put(key, configCache.get(key));
                    }
                }
            } else if (backupRequest.getKeyPattern() != null) {
                Pattern pattern = Pattern.compile(backupRequest.getKeyPattern());
                configCache.entrySet().stream()
                        .filter(entry -> pattern.matcher(entry.getKey()).matches())
                        .forEach(entry -> configToBackup.put(entry.getKey(), entry.getValue()));
            }

            // Data filtering and preparation (CC +6)
            if (backupRequest.isExcludeSecrets()) {
                configToBackup.entrySet().removeIf(entry -> encryptedKeys.contains(entry.getKey()) ||
                        entry.getValue().isEncrypted() ||
                        isSecretKey(entry.getKey()));
            }

            if (backupRequest.getExcludePatterns() != null) {
                for (String excludePattern : backupRequest.getExcludePatterns()) {
                    Pattern pattern = Pattern.compile(excludePattern);
                    configToBackup.entrySet().removeIf(entry -> pattern.matcher(entry.getKey()).matches());
                }
            }

            // Multiple backup destinations (CC +8)
            for (BackupDestination destination : backupRequest.getDestinations()) {
                try {
                    switch (destination.getType()) {
                        case LOCAL_FILE:
                            String filePath = createLocalBackup(configToBackup, destination, backupRequest);
                            backupLocations.add("file://" + filePath);
                            break;

                        case S3:
                            String s3Location = createS3Backup(configToBackup, destination, backupRequest);
                            backupLocations.add("s3://" + s3Location);
                            break;

                        case DATABASE:
                            String dbLocation = createDatabaseBackup(configToBackup, destination, backupRequest);
                            backupLocations.add("db://" + dbLocation);
                            break;

                        case VAULT:
                            String vaultLocation = createVaultBackup(configToBackup, destination, backupRequest);
                            backupLocations.add("vault://" + vaultLocation);
                            break;

                        case GIT:
                            String gitLocation = createGitBackup(configToBackup, destination, backupRequest);
                            backupLocations.add("git://" + gitLocation);
                            break;

                        default:
                            errors.add("Unknown backup destination type: " + destination.getType());
                    }
                } catch (Exception e) {
                    errors.add("Backup failed for destination " + destination.getName() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            errors.add("Unexpected backup error: " + e.getMessage());
        }

        result.setSuccess(!backupLocations.isEmpty());
        result.setBackupLocations(backupLocations);
        result.setErrors(errors);
        result.setBackupTimestamp(Instant.now());
        result.setConfigCount(configToBackup.size());

        return result;
    }

    // Additional complex methods that contribute to high Ca...

    /**
     * HIGH Ca - called by every service during startup
     */
    public void initializeConfiguration(String serviceName, List<String> requiredKeys) {
        // Complex initialization logic
    }

    /**
     * HIGH Ca - called by health check systems
     */
    public ConfigurationHealthStatus getConfigurationHealth() {
        // Complex health checking logic
        return new ConfigurationHealthStatus();
    }

    /**
     * HIGH Ca - called by monitoring systems
     */
    public ConfigurationMetrics getConfigurationMetrics() {
        // Complex metrics collection
        return new ConfigurationMetrics();
    }

    // Private helper methods with complex logic
    private ConfigValue loadFromPropertiesFile(String key, ConfigSource source, ConfigurationRequest request) {
        // Complex properties file loading logic
        return null;
    }

    private ConfigValue loadFromYamlFile(String key, ConfigSource source, ConfigurationRequest request) {
        // Complex YAML file loading logic
        return null;
    }

    private ConfigValue loadFromJsonFile(String key, ConfigSource source, ConfigurationRequest request) {
        // Complex JSON file loading logic
        return null;
    }

    private ConfigValue loadFromXmlFile(String key, ConfigSource source, ConfigurationRequest request) {
        // Complex XML file loading logic
        return null;
    }

    private ConfigValue loadFromSpringEnvironment(String key, ConfigSource source, ConfigurationRequest request) {
        // Spring environment loading logic
        return null;
    }

    private ConfigValue loadFromSpringCloudConfig(String key, ConfigSource source, ConfigurationRequest request) {
        // Spring Cloud Config loading logic
        return null;
    }

    private ConfigValue loadFromConsul(String key, ConfigSource source, ConfigurationRequest request) {
        // Consul loading logic
        return null;
    }

    private ConfigValue loadFromZookeeper(String key, ConfigSource source, ConfigurationRequest request) {
        // ZooKeeper loading logic
        return null;
    }

    private ConfigValue loadFromEtcd(String key, ConfigSource source, ConfigurationRequest request) {
        // etcd loading logic
        return null;
    }

    private ConfigValue loadFromVault(String key, ConfigSource source, ConfigurationRequest request) {
        // Vault loading logic
        return null;
    }

    private ConfigValue loadFromRedis(String key, ConfigSource source, ConfigurationRequest request) {
        // Redis loading logic
        return null;
    }

    private ConfigValue loadFromAwsSecretsManager(String key, ConfigSource source, ConfigurationRequest request) {
        // AWS Secrets Manager loading logic
        return null;
    }

    private ConfigValue loadFromS3(String key, ConfigSource source, ConfigurationRequest request) {
        // S3 loading logic
        return null;
    }

    private ConfigValue loadFromEnvironmentVariables(String key, ConfigSource source, ConfigurationRequest request) {
        // Environment variables loading logic
        return null;
    }

    private ConfigValue loadFromSystemProperties(String key, ConfigSource source, ConfigurationRequest request) {
        // System properties loading logic
        return null;
    }

    // More helper methods...
    private String normalizeConfigKey(String key) {
        return key.toLowerCase().replace("_", ".");
    }

    private List<ConfigSource> determinePrioritizedSources(ConfigurationRequest request) {
        return new ArrayList<>();
    }

    private boolean isSourceAvailable(ConfigSource source) {
        return true;
    }

    private String decryptValue(String value, ConfigSource source) {
        return value;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        return value;
    }

    private void notifyConfigurationListeners(ConfigChangeEvent event) {
    }

    private void scheduleSourceHealthCheck(ConfigSource source) {
    }

    private boolean performSourceHealthCheck(ConfigSource source) {
        return true;
    }

    private List<ConfigSource> getAllConfigSources() {
        return new ArrayList<>();
    }

    private void refreshSingleKey(String key, ConfigurationRefreshRequest request, List<String> refreshed,
            List<String> errors, Map<String, Object> old, Map<String, Object> newVals) {
    }

    private void publishRefreshEvent(List<String> keys, Map<String, Object> old, Map<String, Object> newVals) {
    }

    private boolean isSecretKey(String key) {
        return key.contains("secret") || key.contains("password");
    }

    private String createLocalBackup(Map<String, ConfigValue> config, BackupDestination dest,
            ConfigurationBackupRequest req) {
        return "/tmp/backup";
    }

    private String createS3Backup(Map<String, ConfigValue> config, BackupDestination dest,
            ConfigurationBackupRequest req) {
        return "bucket/backup";
    }

    private String createDatabaseBackup(Map<String, ConfigValue> config, BackupDestination dest,
            ConfigurationBackupRequest req) {
        return "table_backup";
    }

    private String createVaultBackup(Map<String, ConfigValue> config, BackupDestination dest,
            ConfigurationBackupRequest req) {
        return "vault/backup";
    }

    private String createGitBackup(Map<String, ConfigValue> config, BackupDestination dest,
            ConfigurationBackupRequest req) {
        return "repo/backup";
    }
}

// Supporting classes
class ConfigValue {
    private Object value;
    private String source;
    private Class<?> type;
    private boolean encrypted;
    private Instant expiry;

    public ConfigValue(Object value, String source, Class<?> type) {
        this.value = value;
        this.source = source;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isExpired() {
        return expiry != null && Instant.now().isAfter(expiry);
    }
}

class ConfigurationRequest {
    private boolean useCache = true;
    private long cacheMaxAgeMs = 300000; // 5 minutes
    private Class<?> targetType;
    private Object defaultValue;
    private boolean required = false;
    private boolean autoDecrypt = true;
    private boolean failOnDecryptionError = false;
    private boolean failOnConversionError = false;
    private boolean failOnValidationError = false;
    private ConfigValidator validator;

    // Getters and setters
    public boolean isUseCache() {
        return useCache;
    }

    public long getCacheMaxAgeMs() {
        return cacheMaxAgeMs;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isAutoDecrypt() {
        return autoDecrypt;
    }

    public boolean isFailOnDecryptionError() {
        return failOnDecryptionError;
    }

    public boolean isFailOnConversionError() {
        return failOnConversionError;
    }

    public boolean isFailOnValidationError() {
        return failOnValidationError;
    }

    public ConfigValidator getValidator() {
        return validator;
    }
}

interface ConfigValidator {
    boolean isValid(Object value);
}

enum ConfigSourceType {
    FILE_PROPERTIES, FILE_YAML, FILE_JSON, FILE_XML,
    SPRING_ENVIRONMENT, SPRING_CLOUD_CONFIG,
    CONSUL, ZOOKEEPER, ETCD, VAULT, REDIS,
    AWS_SECRETS_MANAGER, AWS_S3,
    ENVIRONMENT_VARIABLES, SYSTEM_PROPERTIES
}

class ConfigSource {
    private String name;
    private ConfigSourceType type;
    private Map<String, String> properties;

    public String getName() {
        return name;
    }

    public ConfigSourceType getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}

class ConfigChangeEvent {
    private String key;
    private Object oldValue;
    private Object newValue;
    private String source;

    public ConfigChangeEvent(String key, Object oldValue, Object newValue, String source) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.source = source;
    }

    public String getKey() {
        return key;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public String getSource() {
        return source;
    }
}

class ConfigurationRefreshRequest {
    private boolean refreshAll = false;
    private List<String> specificKeys;
    private String keyPattern;
    private String source;
    private boolean checkSourceHealth = true;
    private boolean skipUnhealthySources = true;
    private boolean parallelRefresh = false;
    private long timeoutMs = 30000;
    private boolean validateAfterRefresh = false;
    private ConfigValidator postRefreshValidator;
    private boolean rollbackOnValidationFailure = false;
    private boolean publishRefreshEvents = true;

    // Getters and setters
    public boolean isRefreshAll() {
        return refreshAll;
    }

    public List<String> getSpecificKeys() {
        return specificKeys;
    }

    public String getKeyPattern() {
        return keyPattern;
    }

    public String getSource() {
        return source;
    }

    public boolean isCheckSourceHealth() {
        return checkSourceHealth;
    }

    public boolean isSkipUnhealthySources() {
        return skipUnhealthySources;
    }

    public boolean isParallelRefresh() {
        return parallelRefresh;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isValidateAfterRefresh() {
        return validateAfterRefresh;
    }

    public ConfigValidator getPostRefreshValidator() {
        return postRefreshValidator;
    }

    public boolean isRollbackOnValidationFailure() {
        return rollbackOnValidationFailure;
    }

    public boolean isPublishRefreshEvents() {
        return publishRefreshEvents;
    }
}

class ConfigurationRefreshResult {
    private boolean success;
    private List<String> refreshedKeys;
    private List<String> errors;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private Instant refreshTimestamp;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getRefreshedKeys() {
        return refreshedKeys;
    }

    public void setRefreshedKeys(List<String> refreshedKeys) {
        this.refreshedKeys = refreshedKeys;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getOldValues() {
        return oldValues;
    }

    public void setOldValues(Map<String, Object> oldValues) {
        this.oldValues = oldValues;
    }

    public Map<String, Object> getNewValues() {
        return newValues;
    }

    public void setNewValues(Map<String, Object> newValues) {
        this.newValues = newValues;
    }

    public Instant getRefreshTimestamp() {
        return refreshTimestamp;
    }

    public void setRefreshTimestamp(Instant refreshTimestamp) {
        this.refreshTimestamp = refreshTimestamp;
    }
}

class ConfigurationBackupRequest {
    private boolean backupAll = true;
    private List<String> specificKeys;
    private String keyPattern;
    private boolean excludeSecrets = true;
    private List<String> excludePatterns;
    private List<BackupDestination> destinations;

    // Getters and setters
    public boolean isBackupAll() {
        return backupAll;
    }

    public List<String> getSpecificKeys() {
        return specificKeys;
    }

    public String getKeyPattern() {
        return keyPattern;
    }

    public boolean isExcludeSecrets() {
        return excludeSecrets;
    }

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public List<BackupDestination> getDestinations() {
        return destinations;
    }
}

class ConfigurationBackupResult {
    private boolean success;
    private List<String> backupLocations;
    private List<String> errors;
    private Instant backupTimestamp;
    private int configCount;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getBackupLocations() {
        return backupLocations;
    }

    public void setBackupLocations(List<String> backupLocations) {
        this.backupLocations = backupLocations;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Instant getBackupTimestamp() {
        return backupTimestamp;
    }

    public void setBackupTimestamp(Instant backupTimestamp) {
        this.backupTimestamp = backupTimestamp;
    }

    public int getConfigCount() {
        return configCount;
    }

    public void setConfigCount(int configCount) {
        this.configCount = configCount;
    }
}

enum BackupDestinationType {
    LOCAL_FILE, S3, DATABASE, VAULT, GIT
}

class BackupDestination {
    private String name;
    private BackupDestinationType type;
    private Map<String, String> properties;

    public String getName() {
        return name;
    }

    public BackupDestinationType getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}

class ConfigurationHealthStatus {
    private boolean healthy = true;
    private Map<String, Boolean> sourceHealth = new HashMap<>();
    private List<String> issues = new ArrayList<>();

    public boolean isHealthy() {
        return healthy;
    }

    public Map<String, Boolean> getSourceHealth() {
        return sourceHealth;
    }

    public List<String> getIssues() {
        return issues;
    }
}

class ConfigurationMetrics {
    private long totalConfigKeys;
    private long cacheHitRate;
    private long averageLoadTime;
    private Map<String, Long> sourceLoadTimes = new HashMap<>();
    private int refreshCount;
    private int errorCount;

    public long getTotalConfigKeys() {
        return totalConfigKeys;
    }

    public long getCacheHitRate() {
        return cacheHitRate;
    }

    public long getAverageLoadTime() {
        return averageLoadTime;
    }

    public Map<String, Long> getSourceLoadTimes() {
        return sourceLoadTimes;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public int getErrorCount() {
        return errorCount;
    }
}

class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/*
 * EXAMPLES OF CLASSES THAT DEPEND ON THIS CONFIGURATION MANAGER
 * (Demonstrating High Afferent Coupling - Ca: 60+)
 * 
 * @Service
 * class UserService {
 * 
 * @Autowired ConfigurationManager configManager;
 * 
 * public void processUser() {
 * String dbUrl = configManager.getConfiguration("database.url", new
 * ConfigurationRequest()).getValue().toString();
 * // Every service method needs configuration
 * }
 * }
 * 
 * @Service
 * class PaymentService {
 * 
 * @Autowired ConfigurationManager configManager;
 * 
 * public void processPayment() {
 * String apiKey = configManager.getConfiguration("payment.stripe.key", new
 * ConfigurationRequest()).getValue().toString();
 * // Payment processing needs config
 * }
 * }
 * 
 * @Service
 * class EmailService {
 * 
 * @Autowired ConfigurationManager configManager;
 * 
 * public void sendEmail() {
 * String smtpHost = configManager.getConfiguration("email.smtp.host", new
 * ConfigurationRequest()).getValue().toString();
 * // Email service needs config
 * }
 * }
 * 
 * @Service
 * class SecurityService {
 * 
 * @Autowired ConfigurationManager configManager;
 * 
 * public void authenticate() {
 * String jwtSecret = configManager.getConfiguration("security.jwt.secret", new
 * ConfigurationRequest()).getValue().toString();
 * // Security service needs config
 * }
 * }
 * 
 * @Service
 * class MonitoringService {
 * 
 * @Autowired ConfigurationManager configManager;
 * 
 * public void collectMetrics() {
 * configManager.refreshConfiguration(new ConfigurationRefreshRequest());
 * configManager.getConfigurationHealth();
 * configManager.getConfigurationMetrics();
 * // Monitoring calls multiple methods
 * }
 * }
 * 
 * @Service
 * class BackupService {
 * 
 * @Autowired ConfigurationManager configManager;
 * 
 * public void performBackup() {
 * configManager.backupConfiguration(new ConfigurationBackupRequest());
 * // Backup service needs config backup
 * }
 * }
 * 
 * // ... and 50+ more services that all depend on ConfigurationManager
 * // including: CacheService, DatabaseService, IntegrationService,
 * // ReportingService, NotificationService, SchedulingService, etc.
 */