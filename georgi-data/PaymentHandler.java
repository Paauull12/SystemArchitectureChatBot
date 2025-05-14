public class PaymentHandler {
    public void handlePayment() {
        PayPalGateway gateway = new PayPalGateway();
        gateway.charge(100.0);
    }
}