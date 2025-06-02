package com.example.short;

import java.util.*;
import java.net.http.*;
import java.io.IOException;
import org.springframework.web.client.RestTemplate;
import org.springframework.retry.annotation.Retryable;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import com.amazonaws.services.s3.AmazonS3;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.twilio.Twilio;
import com.sendgrid.SendGrid;
import org.apache.kafka.clients.producer.KafkaProducer;

/**
 * Short Example 3: High Efferent Coupling + High Instability
 * Problems: Ce: 20+, Instability: 0.91+ (depends on many volatile APIs)
 */
public class ExternalServiceClient {
    
    // HTTP dependencies (Ce +3)
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    
    // Payment service dependencies (Ce +2)
    private String stripeApiKey;
    private String paypalClientId;
    
    // Cloud service dependencies (Ce +3)
    private AmazonS3 s3Client;
    private Jedis redisClient;
    private String cloudinaryUrl;
    
    // Communication dependencies (Ce +3)
    private String twilioSid;
    private String sendGridApiKey;
    private String slackWebhookUrl;
    
    // Messaging dependencies (Ce +2)
    private KafkaProducer<String, String> kafkaProducer;
    private String rabbitMqUrl;
    
    // External API dependencies (Ce +5)
    private String salesforceEndpoint;
    private String hubspotApiKey;
    private String googleMapsApiKey;
    private String weatherApiKey;
    private String currencyApiKey;
    
    // Analytics dependencies (Ce +2)
    private String googleAnalyticsId;
    private String mixpanelToken;
    
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public PaymentResult processPayment(String customerId, double amount) {
        try {
            // Primary: Stripe (external dependency)
            Stripe.apiKey = stripeApiKey;
            Map<String, Object> params = Map.of(
                "amount", (int)(amount * 100),
                "currency", "usd",
                "customer", customerId
            );
            Charge charge = Charge.create(params);
            
            if ("succeeded".equals(charge.getStatus())) {
                return new PaymentResult(true, charge.getId());
            }
            
            // Fallback: PayPal API call
            String paypalResponse = restTemplate.postForObject(
                "https://api.paypal.com/v1/payments/payment",
                createPayPalRequest(amount, customerId),
                String.class
            );
            
            return new PaymentResult(true, "paypal_" + System.currentTimeMillis());
            
        } catch (Exception e) {
            return new PaymentResult(false, "Error: " + e.getMessage());
        }
    }
    
    public NotificationResult sendNotification(String message, String recipient) {
        List<String> errors = new ArrayList<>();
        
        try {
            // SMS via Twilio
            Twilio.init(twilioSid, "auth_token");
            com.twilio.rest.api.v2010.account.Message.creator(
                new com.twilio.type.PhoneNumber(recipient),
                new com.twilio.type.PhoneNumber("+1234567890"),
                message
            ).create();
            
        } catch (Exception e) {
            errors.add("Twilio failed: " + e.getMessage());
            
            // Fallback: SendGrid email
            try {
                SendGrid sg = new SendGrid(sendGridApiKey);
                // SendGrid API call logic here
            } catch (Exception e2) {
                errors.add("SendGrid failed: " + e2.getMessage());
                
                // Final fallback: Slack webhook
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(slackWebhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"text\":\"" + message + "\"}"
                        ))
                        .build();
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (Exception e3) {
                    errors.add("Slack failed: " + e3.getMessage());
                }
            }
        }
        
        return new NotificationResult(errors.isEmpty(), errors);
    }
    
    public DataResult fetchExternalData(String dataType, String identifier) {
        try {
            switch (dataType) {
                case "weather":
                    String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + 
                                      identifier + "&appid=" + weatherApiKey;
                    HttpRequest weatherRequest = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(weatherUrl))
                        .build();
                    HttpResponse<String> weatherResponse = httpClient.send(
                        weatherRequest, HttpResponse.BodyHandlers.ofString()
                    );
                    return new DataResult(true, weatherResponse.body());
                    
                case "currency":
                    String currencyUrl = "https://api.exchangerate-api.com/v4/latest/" + identifier;
                    String currencyData = restTemplate.getForObject(currencyUrl, String.class);
                    return new DataResult(true, currencyData);
                    
                case "geocoding":
                    String mapsUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + 
                                   identifier + "&key=" + googleMapsApiKey;
                    HttpRequest mapsRequest = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(mapsUrl))
                        .build();
                    HttpResponse<String> mapsResponse = httpClient.send(
                        mapsRequest, HttpResponse.BodyHandlers.ofString()
                    );
                    return new DataResult(true, mapsResponse.body());
                    
                default:
                    return new DataResult(false, "Unknown data type: " + dataType);
            }
        } catch (Exception e) {
            return new DataResult(false, "Error: " + e.getMessage());
        }
    }
    
    public void syncToCRM(Map<String, Object> customerData) {
        try {
            // Sync to Salesforce
            String salesforcePayload = jsonMapper.writeValueAsString(customerData);
            HttpRequest salesforceRequest = HttpRequest.newBuilder()
                .uri(java.net.URI.create(salesforceEndpoint + "/services/data/v52.0/sobjects/Contact/"))
                .header("Authorization", "Bearer " + getSalesforceToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(salesforcePayload))
                .build();
            httpClient.send(salesforceRequest, HttpResponse.BodyHandlers.ofString());
            
            // Sync to HubSpot
            String hubspotPayload = createHubSpotPayload(customerData);
            restTemplate.postForObject(
                "https://api.hubapi.com/crm/v3/objects/contacts?hapikey=" + hubspotApiKey,
                hubspotPayload,
                String.class
            );
            
        } catch (Exception e) {
            // Log error and continue
        }
    }
    
    public void publishAnalytics(String event, Map<String, Object> properties) {
        try {
            // Google Analytics
            String gaPayload = createGoogleAnalyticsPayload(event, properties);
            HttpRequest gaRequest = HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://www.google-analytics.com/collect"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(gaPayload))
                .build();
            httpClient.send(gaRequest, HttpResponse.BodyHandlers.ofString());
            
            // Mixpanel
            String mixpanelPayload = createMixpanelPayload(event, properties);
            restTemplate.postForObject(
                "https://api.mixpanel.com/track/?data=" + 
                Base64.getEncoder().encodeToString(mixpanelPayload.getBytes()),
                null,
                String.class
            );
            
        } catch (Exception e) {
            // Analytics failures should not break main flow
        }
    }
    
    // Helper methods
    private String createPayPalRequest(double amount, String customerId) { return "{}"; }
    private String getSalesforceToken() { return "token"; }
    private String createHubSpotPayload(Map<String, Object> data) { return "{}"; }
    private String createGoogleAnalyticsPayload(String event, Map<String, Object> props) { return ""; }
    private String createMixpanelPayload(String event, Map<String, Object> props) { return "{}"; }
}

class PaymentResult {
    private boolean success;
    private String transactionId;
    
    public PaymentResult(boolean success, String transactionId) {
        this.success = success;
        this.transactionId = transactionId;
    }
    
    public boolean isSuccess() { return success; }
    public String getTransactionId() { return transactionId; }
}

class NotificationResult {
    private boolean success;
    private List<String> errors;
    
    public NotificationResult(boolean success, List<String> errors) {
        this.success = success;
        this.errors = errors;
    }
    
    public boolean isSuccess() { return success; }
    public List<String> getErrors() { return errors; }
}

class DataResult {
    private boolean success;
    private String data;
    
    public DataResult(boolean success, String data) {
        this.success = success;
        this.data = data;
    }
    
    public boolean isSuccess() { return success; }
    public String getData() { return data; }
}