package ecommerce.checkout;

import ecommerce.auth.Authenticator;
import ecommerce.user.UserService;
import ecommerce.catalog.ProductService;
import ecommerce.cart.CartService;
import ecommerce.payment.PaymentProcessor;
import ecommerce.notification.EmailNotifier;
import ecommerce.notification.SmsNotifier;
import ecommerce.logging.TransactionLogger;
import ecommerce.promotion.CouponService;
import ecommerce.shipping.DeliveryScheduler;
import ecommerce.order.OrderService;

public class CheckoutCoordinator {

    private final Authenticator authenticator;
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final CouponService couponService;
    private final PaymentProcessor paymentProcessor;
    private final DeliveryScheduler deliveryScheduler;
    private final OrderService orderService;
    private final EmailNotifier emailNotifier;
    private final SmsNotifier smsNotifier;
    private final TransactionLogger logger;

    public CheckoutCoordinator(
            Authenticator authenticator,
            UserService userService,
            ProductService productService,
            CartService cartService,
            CouponService couponService,
            PaymentProcessor paymentProcessor,
            DeliveryScheduler deliveryScheduler,
            OrderService orderService,
            EmailNotifier emailNotifier,
            SmsNotifier smsNotifier,
            TransactionLogger logger
    ) {
        this.authenticator = authenticator;
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.couponService = couponService;
        this.paymentProcessor = paymentProcessor;
        this.deliveryScheduler = deliveryScheduler;
        this.orderService = orderService;
        this.emailNotifier = emailNotifier;
        this.smsNotifier = smsNotifier;
        this.logger = logger;
    }

    public void completeCheckout(String userId, String couponCode) {
        if (!authenticator.isAuthenticated(userId)) {
            System.out.println("User not authenticated.");
            return;
        }

        var user = userService.getUserById(userId);
        var cart = cartService.getCartForUser(userId);
        var products = productService.getProducts(cart.getProductIds());

        double subtotal = cart.calculateSubtotal(products);
        double discount = couponService.applyCoupon(couponCode, subtotal);
        double finalAmount = subtotal - discount;

        paymentProcessor.processPayment(user.getPaymentInfo(), finalAmount);
        var order = orderService.createOrder(userId, cart, finalAmount);
        deliveryScheduler.scheduleDelivery(order);

        emailNotifier.sendOrderConfirmation(user.getEmail(), order);
        smsNotifier.send(user.getPhone(), "Your order has been placed.");

        logger.logTransaction("Order completed for user " + userId);
    }
}
