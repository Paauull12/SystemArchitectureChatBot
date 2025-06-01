package affclients;

import afferentcoupling.CentralMessagingService;

public class StockUpdater {
    private final CentralMessagingService messagingService;

    public StockUpdater(CentralMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void updateProductStock(String productId, int quantityChange) {
        // ... 
        if (quantityChange < 0 && Math.abs(quantityChange) > 50) {
            messagingService.sendAlert("InventoryService", "Significant stock decrease for product " + productId + ": " + quantityChange);
        }
        messagingService.logActivity("Stock updated for product " + productId + " by " + quantityChange);
        messagingService.publishEvent("StockUpdate", "{ \"productId\": \"" + productId + "\", \"change\": " + quantityChange + " }");
    }
}