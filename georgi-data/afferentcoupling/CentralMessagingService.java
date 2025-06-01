package afferentcoupling;
public class CentralMessagingService {

    public void sendMessage(String recipient, String messageContent) {
        System.out.println("[CORE MESSAGE] Sending to " + recipient + ": " + messageContent);
    }

    public void sendAlert(String systemComponent, String alertMessage) {
        System.err.println("[SYSTEM ALERT] From " + systemComponent + ": " + alertMessage);
    }

    public void logActivity(String activityDetails) {
        System.out.println("[ACTIVITY LOG] " + activityDetails);
    }

    public void publishEvent(String eventType, String eventPayload) {
        System.out.println("[EVENT BUS] Publishing event '" + eventType + "': " + eventPayload);
    }
}