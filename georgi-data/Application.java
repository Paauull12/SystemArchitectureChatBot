import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Application {
    public void handleRequest(String userType, int productId, int quantity) {
        // Auth logic
        if (!userType.equals("admin")) throw new SecurityException("Access denied");

        // Business logic
        double price = getProductPrice(productId) * quantity;
        double discount = applyDiscount(userType, price);
        double finalPrice = price - discount;

        // DB logic
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/db")) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO orders (...)");
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // UI Response
        System.out.println("Order placed successfully!");
    }

    private double getProductPrice(int productId) { return 100.0; }
    private double applyDiscount(String userType, double price) { return 10.0; }
}