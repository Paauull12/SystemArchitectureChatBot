package com.example.problematic;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import redis.clients.jedis.Jedis;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.index.IndexRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Example 3: High Efferent Coupling + High Instability + High Cognitive
 * Complexity
 * 
 * Problems:
 * - Efferent Coupling (Ce): 25+ external dependencies
 * - Instability (I): 0.92+ (Ce / (Ca + Ce) - very unstable)
 * - Cognitive Complexity: 85+ (complex integration logic)
 * 
 * This class depends on many external services and changes frequently
 */
public class IntegrationOrchestrator {

    // Database dependencies (Ce +4)
    private Connection primaryDatabase;
    private JdbcTemplate jdbcTemplate;
    private Jedis redisClient;
    private RestHighLevelClient elasticsearchClient;

    // HTTP/REST dependencies (Ce +3)
    private HttpClient httpClient;
    private RestTemplate restTemplate;
    private ObjectMapper jsonMapper;

    // Cloud services dependencies (Ce +3)
    private AmazonS3 s3Client;
    private String awsBucketName;
    private String awsRegion;

    // Payment processing dependencies (Ce +2)
    private String stripeApiKey;
    private String paypalClientId;

    // Messaging dependencies (Ce +3)
    private KafkaProducer<String, String> kafkaProducer;
    private Session emailSession;
    private Properties emailProperties;

    // External API dependencies (Ce +8)
    private String salesforceEndpoint;
    private String salesforceToken;
    private String hubspotApiKey;
    private String hubspotEndpoint;
    private String slackWebhookUrl;
    private String twilioSid;
    private String twilioAuthToken;
    private String googleApiKey;

    // Configuration dependencies (Ce +2)
    private Properties configProperties;
    private ExecutorService threadPool;

    /**
     * Complex integration method with high cognitive complexity
     * Cognitive Complexity: 45+ due to nested service calls and error handling
     */
    public IntegrationResult processCustomerOrder(String customerId, String orderId,
            Map<String, Object> orderData) {

        IntegrationResult result = new IntegrationResult();
        List<String> errors = new ArrayList<>();
        Map<String, Object> processingLog = new HashMap<>();

        try {
            // Step 1: Validate and fetch customer from database (Cognitive +3)
            Customer customer = null;
            try {
                String customerQuery = "SELECT * FROM customers WHERE id = ?";
                ResultSet rs = jdbcTemplate.query(customerQuery,
                        new Object[] { customerId },
                        resultSet -> {
                            if (resultSet.next()) {
                                Customer c = new Customer();
                                c.setId(resultSet.getString("id"));
                                c.setEmail(resultSet.getString("email"));
                                c.setName(resultSet.getString("name"));
                                c.setStripeCustomerId(resultSet.getString("stripe_customer_id"));
                                return c;
                            }
                            return null;
                        });

                if (rs == null) {
                    errors.add("Customer not found: " + customerId);
                    result.setSuccess(false);
                    result.setErrors(errors);
                    return result;
                } else {
                    customer = rs;
                    processingLog.put("customer_found", true);
                }
            } catch (Exception e) {
                errors.add("Database error fetching customer: " + e.getMessage());

                // Fallback: try Redis cache (Cognitive +2)
                try {
                    String cachedCustomer = redisClient.get("customer:" + customerId);
                    if (cachedCustomer != null) {
                        customer = jsonMapper.readValue(cachedCustomer, Customer.class);
                        processingLog.put("customer_from_cache", true);
                    } else {
                        errors.add("Customer not found in cache either");
                        result.setSuccess(false);
                        result.setErrors(errors);
                        return result;
                    }
                } catch (Exception cacheException) {
                    errors.add("Cache error: " + cacheException.getMessage());
                    result.setSuccess(false);
                    result.setErrors(errors);
                    return result;
                }
            }

            // Step 2: Process payment through Stripe (Cognitive +8)
            String chargeId = null;
            if (orderData.containsKey("paymentAmount") && orderData.containsKey("paymentMethod")) {
                try {
                    Stripe.apiKey = stripeApiKey;

                    Long amount = ((Number) orderData.get("paymentAmount")).longValue() * 100; // Convert to cents
                    String paymentMethodId = (String) orderData.get("paymentMethod");

                    ChargeCreateParams params = ChargeCreateParams.builder()
                            .setAmount(amount)
                            .setCurrency("usd")
                            .setCustomer(customer.getStripeCustomerId())
                            .setSource(paymentMethodId)
                            .setDescription("Order payment for order: " + orderId)
                            .build();

                    Charge charge = Charge.create(params);

                    if ("succeeded".equals(charge.getStatus())) {
                        chargeId = charge.getId();
                        processingLog.put("payment_successful", true);
                        processingLog.put("charge_id", chargeId);
                    } else {
                        errors.add("Payment failed: " + charge.getFailureMessage());

                        // Try alternative payment method if available (Cognitive +3)
                        if (orderData.containsKey("alternativePaymentMethod")) {
                            try {
                                String altPaymentMethod = (String) orderData.get("alternativePaymentMethod");
                                ChargeCreateParams altParams = ChargeCreateParams.builder()
                                        .setAmount(amount)
                                        .setCurrency("usd")
                                        .setCustomer(customer.getStripeCustomerId())
                                        .setSource(altPaymentMethod)
                                        .setDescription("Retry payment for order: " + orderId)
                                        .build();

                                Charge altCharge = Charge.create(altParams);
                                if ("succeeded".equals(altCharge.getStatus())) {
                                    chargeId = altCharge.getId();
                                    processingLog.put("payment_retry_successful", true);
                                } else {
                                    errors.add("Alternative payment also failed: " + altCharge.getFailureMessage());
                                }
                            } catch (Exception altPaymentException) {
                                errors.add("Alternative payment error: " + altPaymentException.getMessage());
                            }
                        }
                    }
                } catch (Exception stripeException) {
                    errors.add("Stripe payment error: " + stripeException.getMessage());

                    // Log payment failure to multiple systems (Cognitive +4)
                    try {
                        // Log to database
                        String insertError = "INSERT INTO payment_errors (customer_id, order_id, error_message, timestamp) VALUES (?, ?, ?, ?)";
                        jdbcTemplate.update(insertError, customerId, orderId, stripeException.getMessage(),
                                new Timestamp(System.currentTimeMillis()));

                        // Log to Elasticsearch
                        Map<String, Object> errorDoc = new HashMap<>();
                        errorDoc.put("customer_id", customerId);
                        errorDoc.put("order_id", orderId);
                        errorDoc.put("error_type", "payment_failure");
                        errorDoc.put("error_message", stripeException.getMessage());
                        errorDoc.put("timestamp", System.currentTimeMillis());

                        IndexRequest indexRequest = new IndexRequest("payment-errors")
                                .source(errorDoc);
                        elasticsearchClient.index(indexRequest, null);

                    } catch (Exception loggingException) {
                        errors.add("Failed to log payment error: " + loggingException.getMessage());
                    }
                }
            }

            // Step 3: Update CRM system (Salesforce) (Cognitive +6)
            if (chargeId != null) {
                try {
                    Map<String, Object> salesforceData = new HashMap<>();
                    salesforceData.put("CustomerId__c", customerId);
                    salesforceData.put("OrderId__c", orderId);
                    salesforceData.put("Amount__c", orderData.get("paymentAmount"));
                    salesforceData.put("ChargeId__c", chargeId);
                    salesforceData.put("Status__c", "Completed");

                    String salesforceJson = jsonMapper.writeValueAsString(salesforceData);

                    HttpPost salesforceRequest = new HttpPost(
                            salesforceEndpoint + "/services/data/v52.0/sobjects/Order__c/");
                    salesforceRequest.addHeader("Authorization", "Bearer " + salesforceToken);
                    salesforceRequest.addHeader("Content-Type", "application/json");
                    salesforceRequest.setEntity(new StringEntity(salesforceJson));

                    var salesforceResponse = httpClient.execute(salesforceRequest);

                    if (salesforceResponse.getStatusLine().getStatusCode() == 201) {
                        processingLog.put("salesforce_updated", true);
                    } else {
                        errors.add("Salesforce update failed: " + salesforceResponse.getStatusLine().getReasonPhrase());

                        // Retry with HubSpot as backup CRM (Cognitive +4)
                        try {
                            Map<String, Object> hubspotData = new HashMap<>();
                            hubspotData.put("properties", Map.of(
                                    "customer_id", customerId,
                                    "order_id", orderId,
                                    "amount", orderData.get("paymentAmount"),
                                    "charge_id", chargeId));

                            String hubspotJson = jsonMapper.writeValueAsString(hubspotData);

                            HttpPost hubspotRequest = new HttpPost(hubspotEndpoint + "/crm/v3/objects/deals");
                            hubspotRequest.addHeader("Authorization", "Bearer " + hubspotApiKey);
                            hubspotRequest.addHeader("Content-Type", "application/json");
                            hubspotRequest.setEntity(new StringEntity(hubspotJson));

                            var hubspotResponse = httpClient.execute(hubspotRequest);

                            if (hubspotResponse.getStatusLine().getStatusCode() == 201) {
                                processingLog.put("hubspot_updated", true);
                            } else {
                                errors.add("HubSpot backup also failed");
                            }
                        } catch (Exception hubspotException) {
                            errors.add("HubSpot error: " + hubspotException.getMessage());
                        }
                    }

                } catch (Exception salesforceException) {
                    errors.add("CRM integration error: " + salesforceException.getMessage());
                }
            }

            // Step 4: Send notifications (Cognitive +10)
            if (chargeId != null) {
                // Send email confirmation (Cognitive +3)
                try {
                    Message emailMessage = new MimeMessage(emailSession);
                    emailMessage.setFrom(new InternetAddress("noreply@company.com"));
                    emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(customer.getEmail()));
                    emailMessage.setSubject("Order Confirmation - " + orderId);

                    String emailBody = buildEmailTemplate(customer, orderId, orderData);
                    emailMessage.setContent(emailBody, "text/html");

                    Transport.send(emailMessage);
                    processingLog.put("email_sent", true);

                } catch (MessagingException emailException) {
                    errors.add("Email sending failed: " + emailException.getMessage());

                    // Fallback: send SMS via Twilio (Cognitive +3)
                    if (customer.getPhoneNumber() != null) {
                        try {
                            String smsMessage = "Order " + orderId + " confirmed. Amount: $"
                                    + orderData.get("paymentAmount");

                            HttpPost twilioRequest = new HttpPost(
                                    "https://api.twilio.com/2010-04-01/Accounts/" + twilioSid + "/Messages.json");
                            String auth = twilioSid + ":" + twilioAuthToken;
                            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                            twilioRequest.addHeader("Authorization", "Basic " + encodedAuth);

                            List<String> params = Arrays.asList(
                                    "To=" + customer.getPhoneNumber(),
                                    "From=+1234567890",
                                    "Body=" + smsMessage);
                            twilioRequest.setEntity(new StringEntity(String.join("&", params)));

                            var twilioResponse = httpClient.execute(twilioRequest);
                            if (twilioResponse.getStatusLine().getStatusCode() == 201) {
                                processingLog.put("sms_sent", true);
                            }

                        } catch (Exception smsException) {
                            errors.add("SMS backup also failed: " + smsException.getMessage());
                        }
                    }
                }

                // Send Slack notification to team (Cognitive +2)
                try {
                    Map<String, Object> slackPayload = new HashMap<>();
                    slackPayload.put("text", "New order processed: " + orderId + " for customer " + customer.getName());
                    slackPayload.put("channel", "#orders");

                    String slackJson = jsonMapper.writeValueAsString(slackPayload);

                    HttpPost slackRequest = new HttpPost(slackWebhookUrl);
                    slackRequest.addHeader("Content-Type", "application/json");
                    slackRequest.setEntity(new StringEntity(slackJson));

                    httpClient.execute(slackRequest);
                    processingLog.put("slack_notified", true);

                } catch (Exception slackException) {
                    errors.add("Slack notification failed: " + slackException.getMessage());
                }

                // Publish to Kafka for downstream processing (Cognitive +2)
                try {
                    Map<String, Object> kafkaMessage = new HashMap<>();
                    kafkaMessage.put("event_type", "order_completed");
                    kafkaMessage.put("customer_id", customerId);
                    kafkaMessage.put("order_id", orderId);
                    kafkaMessage.put("charge_id", chargeId);
                    kafkaMessage.put("timestamp", System.currentTimeMillis());

                    String kafkaJson = jsonMapper.writeValueAsString(kafkaMessage);

                    ProducerRecord<String, String> record = new ProducerRecord<>("order-events", orderId, kafkaJson);
                    kafkaProducer.send(record);
                    processingLog.put("kafka_published", true);

                } catch (Exception kafkaException) {
                    errors.add("Kafka publishing failed: " + kafkaException.getMessage());
                }
            }

            // Step 5: Update caches and search indexes (Cognitive +4)
            try {
                // Update Redis cache
                String customerCacheKey = "customer:" + customerId;
                String orderCacheKey = "order:" + orderId;

                redisClient.setex(customerCacheKey, 3600, jsonMapper.writeValueAsString(customer));
                redisClient.setex(orderCacheKey, 3600, jsonMapper.writeValueAsString(orderData));

                // Update Elasticsearch for search
                Map<String, Object> searchDoc = new HashMap<>();
                searchDoc.put("customer_id", customerId);
                searchDoc.put("customer_name", customer.getName());
                searchDoc.put("order_id", orderId);
                searchDoc.put("amount", orderData.get("paymentAmount"));
                searchDoc.put("status", "completed");
                searchDoc.put("timestamp", System.currentTimeMillis());

                IndexRequest searchRequest = new IndexRequest("orders")
                        .id(orderId)
                        .source(searchDoc);
                elasticsearchClient.index(searchRequest, null);

                processingLog.put("caches_updated", true);

            } catch (Exception cacheException) {
                errors.add("Cache update failed: " + cacheException.getMessage());
            }

        } catch (Exception generalException) {
            errors.add("General processing error: " + generalException.getMessage());
        }

        // Build final result
        result.setSuccess(errors.isEmpty());
        result.setErrors(errors);
        result.setProcessingLog(processingLog);

        return result;
    }

    /**
     * Another complex integration method
     * Cognitive Complexity: 25+ due to multiple service coordination
     */
    public void syncCustomerDataAcrossSystems(String customerId) {
        // Complex logic to sync customer data across multiple systems
        // This method also depends on many external services
    }

    /**
     * Complex analytics integration
     * Cognitive Complexity: 30+ due to data transformation and multiple APIs
     */
    public void generateCrossSystemReport(String reportType, Map<String, Object> parameters) {
        // Complex logic to gather data from multiple systems and generate reports
        // Depends on analytics APIs, databases, and file storage systems
    }

    private String buildEmailTemplate(Customer customer, String orderId, Map<String, Object> orderData) {
        return "<html><body>Order confirmation for " + customer.getName() + "</body></html>";
    }
}

// Supporting classes
class IntegrationResult {
    private boolean success;
    private List<String> errors;
    private Map<String, Object> processingLog;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getProcessingLog() {
        return processingLog;
    }

    public void setProcessingLog(Map<String, Object> processingLog) {
        this.processingLog = processingLog;
    }
}

class Customer {
    private String id;
    private String email;
    private String name;
    private String phoneNumber;
    private String stripeCustomerId;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }
}