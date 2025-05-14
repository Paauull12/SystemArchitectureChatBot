import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Order {
    private int orderId;
}

public class LargeMethodExample {
    public void processOrder(Order order) {
        validateOrder(order);
        calculateDiscount(order);
        updateInventory(order);
        sendConfirmationEmail(order);
        generateInvoice(order);
        saveToDatabase(order);
    }
    private void validateOrder(Order order) { /*...*/ }
    private void calculateDiscount(Order order) { /*...*/ }
    private void updateInventory(Order order) { /*...*/ }
    private void sendConfirmationEmail(Order order) { /*...*/ }
    private void generateInvoice(Order order) { /*...*/ }
    private void saveToDatabase(Order order) { /*...*/ }
}