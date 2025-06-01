package affclients;

import afferentcoupling.CentralMessagingService;

public class EmailSender {
    private final CentralMessagingService messagingService;

    public EmailSender(CentralMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void sendWelcomeEmail(String userEmail) {
        // ...
        messagingService.sendMessage(userEmail, "Welcome to our service!");
        messagingService.logActivity("Sent welcome email to " + userEmail);
    }

    public void sendPasswordResetEmail(String userEmail, String resetLink) {
        messagingService.sendMessage(userEmail, "Your password reset link: " + resetLink);
        messagingService.logActivity("Sent password reset email to " + userEmail);
    }
}