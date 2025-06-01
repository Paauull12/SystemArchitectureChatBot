package ecommerce.coordinator;

// 12 imports representing dependencies
import ecommerce.auth.UserSessionManager;
import ecommerce.billing.PaymentGateway;
import ecommerce.catalog.ProductCatalog;
import ecommerce.customer.CustomerProfileService;
import ecommerce.discount.DiscountEngine;
import ecommerce.email.EmailService;
import ecommerce.inventory.InventoryManager;
import ecommerce.logging.AuditLogger;
import ecommerce.notification.SmsService;
import ecommerce.order.OrderRepository;
import ecommerce.shipping.ShippingCalculator;
import ecommerce.tax.TaxCalculator;

public class OrderProcessingCoordinator {

    private final UserSessionManager sessionManager;
    private final ProductCatalog productCatalog;
    private final CustomerProfileService profileService;
    private final InventoryManager inventoryManager;
    private final DiscountEngine discountEngine;
    private final TaxCalculator taxCalculator;
    private final ShippingCalculator shippingCalculator;
    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final AuditLogger auditLogger;

    public OrderProcessingCoordinator(
            UserSessionManager sessionManager,
            ProductCatalog productCatalog,
            CustomerProfileService profileService,
            InventoryManager inventoryManager,
            DiscountEngine discountEngine,
            TaxCalculator taxCalculator,
            ShippingCalculator shippingCalculator,
            PaymentGateway paymentGateway,
            OrderRepository orderRepository,
            EmailService emailService,
            SmsService smsService,
            AuditLogger auditLogger
    ) {
        this.sessionManager = sessionManager;
        this.productCatalog = productCatalog;
        this.profileService = profileService;
        this.inventoryManager = inventoryManager;
        this.discountEngine = discountEngine;
        this.taxCalculator = taxCalculator;
        this.shippingCalculator = shippingCalculator;
        this.paymentGateway = paymentGateway;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.auditLogger = auditLogger;
    }

    public void processOrder(String userId, String productId, int quantity) {
        var user = sessionManager.getUserSession(userId);
        var product = productCatalog.getProduct(productId);
        var profile = profileService.getCustomerProfile(userId);

        if (!inventoryManager.isInStock(productId, quantity)) {
            smsService.send(user.getPhoneNumber(), "Product out of stock: " + product.getName());
            return;
        }

        double basePrice = product.getPrice() * quantity;
        double discount = discountEngine.calculateDiscount(profile, product);
        double tax = taxCalculator.calculateTax(product, profile.getLocation());
        double shipping = shippingCalculator.calculateShipping(product, profile.getAddress());

        double finalAmount = basePrice - discount + tax + shipping;

        paymentGateway.charge(user.getPaymentMethod(), finalAmount);
        inventoryManager.reserve(productId, quantity);

        orderRepository.saveOrder(userId, productId, quantity, finalAmount);

        emailService.send(user.getEmail(), "Order Confirmation", "Thanks for your order!");
        smsService.send(user.getPhoneNumber(), "Order placed successfully.");

        auditLogger.log("Order placed by " + userId + " for " + quantity + " x " + productId);
    }
}
