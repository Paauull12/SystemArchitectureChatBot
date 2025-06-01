package affclients;

import afferentcoupling.CentralMessagingService;

public class AuditLogger {
    private final CentralMessagingService messagingService;

    public AuditLogger(CentralMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void logAuditEntry(String user, String action, String details) {
        String auditMessage = "AUDIT: User '" + user + "' performed '" + action + "'. Details: " + details;
        messagingService.logActivity(auditMessage);
    }
}