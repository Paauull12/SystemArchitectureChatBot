package affclients;

import afferentcoupling.CentralMessagingService;

public class UserLoginManager {
    private final CentralMessagingService messagingService;

    public UserLoginManager(CentralMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public boolean loginUser(String username, String password) {
        // ...
        messagingService.logActivity("User '" + username + "' attempted login.");
        if ("user".equals(username) && "pass".equals(password)) {
            messagingService.sendMessage(username, "Welcome back!");
            return true;
        }
        messagingService.sendAlert("AuthService", "Failed login for " + username);
        return false;
    }
}