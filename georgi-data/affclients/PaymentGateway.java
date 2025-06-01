package affclients;

import afferentcoupling.CentralMessagingService;

public class PaymentGateway {
    private final CentralMessagingService messagingService;

    public PaymentGateway(CentralMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public boolean processPayment(String orderId, double amount) {
        // ...
        if (amount > 1000) {
            messagingService.sendAlert("PaymentService", "Large payment detected for order " + orderId + ": " + amount);
        }
        messagingService.logActivity("Processed payment for order " + orderId + ", amount " + amount);
        messagingService.publishEvent("PaymentSuccess", "{ \"orderId\": \"" + orderId + "\", \"amount\": " + amount + " }");
        return true;
    }
}
