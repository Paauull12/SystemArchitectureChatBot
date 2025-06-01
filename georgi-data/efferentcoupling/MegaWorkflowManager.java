package com.company.megaworkflow;

import com.company.auth.AuthService;
import com.company.user.UserService;
import com.company.catalog.ProductService;
import com.company.inventory.InventoryService;
import com.company.cart.CartService;
import com.company.promotion.PromotionService;
import com.company.payment.PaymentGateway;
import com.company.shipping.ShippingService;
import com.company.notification.EmailService;
import com.company.notification.SmsService;
import com.company.logging.LoggingService;
import com.company.reporting.ReportingService;
import com.company.analytics.AnalyticsService;
import com.company.compliance.ComplianceService;
import com.company.support.SupportService;
import com.company.feedback.FeedbackService;
import com.company.loyalty.LoyaltyProgramService;

public class MegaWorkflowManager {

    private final AuthService authService;
    private final UserService userService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final PromotionService promotionService;
    private final PaymentGateway paymentGateway;
    private final ShippingService shippingService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final LoggingService loggingService;
    private final ReportingService reportingService;
    private final AnalyticsService analyticsService;
    private final ComplianceService complianceService;
    private final SupportService supportService;
    private final FeedbackService feedbackService;
    private final LoyaltyProgramService loyaltyProgramService;

    public MegaWorkflowManager(
        AuthService authService,
        UserService userService,
        ProductService productService,
        InventoryService inventoryService,
        CartService cartService,
        PromotionService promotionService,
        PaymentGateway paymentGateway,
        ShippingService shippingService,
        EmailService emailService,
        SmsService smsService,
        LoggingService loggingService,
        ReportingService reportingService,
        AnalyticsService analyticsService,
        ComplianceService complianceService,
        SupportService supportService,
        FeedbackService feedbackService,
        LoyaltyProgramService loyaltyProgramService
    ) {
        this.authService = authService;
        this.userService = userService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.cartService = cartService;
        this.promotionService = promotionService;
        this.paymentGateway = paymentGateway;
        this.shippingService = shippingService;
        this.emailService = emailService;
        this.smsService = smsService;
        this.loggingService = loggingService;
        this.reportingService = reportingService;
        this.analyticsService = analyticsService;
        this.complianceService = complianceService;
        this.supportService = supportService;
        this.feedbackService = feedbackService;
        this.loyaltyProgramService = loyaltyProgramService;
    }

    public void executeOrderWorkflow(String userId, String paymentMethodId, String promoCode) {
        if (!authService.isAuthenticated(userId)) {
            loggingService.log("User authentication failed: " + userId);
            return;
        }

        var userProfile = userService.getUserProfile(userId);
        var cart = cartService.getCartForUser(userId);
        var products = productService.getProducts(cart.getProductIds());

        if (!inventoryService.reserveStock(products)) {
            loggingService.log("Stock reservation failed for user: " + userId);
            supportService.notifyOutOfStock(userId, products);
            return;
        }

        double subtotal = cart.calculateSubtotal(products);
        double discount = promotionService.calculateDiscount(promoCode, subtotal);
        double totalAmount = subtotal - discount;

        totalAmount -= loyaltyProgramService.applyPoints(userId, totalAmount);

        paymentGateway.processPayment(paymentMethodId, totalAmount);

        shippingService.scheduleDelivery(userProfile.getShippingAddress(), products);

        emailService.sendOrderConfirmation(userProfile.getEmail(), totalAmount);
        smsService.sendOrderNotification(userProfile.getPhoneNumber());

        loggingService.log("Order workflow completed for user: " + userId);

        reportingService.recordSale(userId, totalAmount);

        analyticsService.trackPurchase(userId, products);

        complianceService.ensureCompliance(userId, cart, paymentMethodId);

        feedbackService.requestFeedback(userId);
    }
}
