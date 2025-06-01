package com.example.orders;

public class OrderService {

    public boolean placeOrder(String userId, String productId, int quantity) {
        System.out.println("Placing order for user " + userId + " with product " + productId + " quantity " + quantity);
        // ...
        return true;
    }

    public boolean cancelOrder(String orderId) {
        System.out.println("Cancelling order " + orderId);
        // ...
        return true;
    }

    public String getOrderStatus(String orderId) {
        // ...
        return "Processing";
    }
}
