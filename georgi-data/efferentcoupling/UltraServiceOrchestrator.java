package com.company.ultraservice;

import com.company.auth.AuthService;
import com.company.user.UserDataService;
import com.company.catalog.ProductService;
import com.company.inventory.InventoryService;
import com.company.cart.CartService;
import com.company.promotion.PromotionService;
import com.company.payment.PaymentService;
import com.company.shipping.ShippingService;
import com.company.notification.EmailService;
import com.company.notification.SmsService;
import com.company.logging.LoggerService;
import com.company.reporting.ReportService;
import com.company.analytics.AnalyticsService;
import com.company.compliance.ComplianceService;
import com.company.support.CustomerSupportService;

public class UltraServiceOrchestrator {

    private final AuthService authService;
    private final UserDataService userDataService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final PromotionService promotionService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final LoggerService loggerService;
    private final ReportService reportService;
    private final AnalyticsService analyticsService;
    private final ComplianceService complianceService;
    private final CustomerSupportService customerSupportService;

    public UltraServiceOrchestrator(
        AuthService authService,
        UserDataService userDataService,
        ProductService productService,
        InventoryService inventoryService,
        CartService cartService,
        PromotionService promotionService,
        PaymentService paymentService,
        ShippingService shippingService,
        EmailService emailService,
        SmsService smsService,
        LoggerService loggerService,
        ReportService reportService,
        AnalyticsService analyticsService,
        ComplianceService complianceService,
        CustomerSupportService customerSupportService
    ) {
        this.authService = authService;
        this.userDataService = userDataService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.cartService = cartService;
        this.promotionService = promotionService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.emailService = emailService;
        this.smsService = smsService;
        this.loggerService = loggerService;
        this.reportService = reportService;
        this.analyticsService = analyticsService;
        this.complianceService = complianceService;
        this.customerSupportService = customerSupportService;
    }

    public void processCompleteOrder(String userId, String paymentMethod, String promoCode) {
        if (!authService.isUserAuthenticated(userId)) {
            loggerService.log("Authentication failed for user: " + userId);
            return;
        }

        var userProfile = userDataService.getUserProfile(userId);
        var cart = cartService.getCart(userId);
        var products = productService.getProducts(cart.getProductIds());

        if (!inventoryService.reserveStock(products)) {
            loggerService.log("Stock reservation failed for user: " + userId);
            customerSupportService.notifyOutOfStock(userId, products);
            return;
        }

        double subtotal = cart.calculateSubtotal(products);
        double discount = promotionService.calculateDiscount(promoCode, subtotal);
        double totalAmount = subtotal - discount;

        paymentService.charge(paymentMethod, totalAmount);

        shippingService.scheduleShipment(userProfile.getShippingAddress(), products);

        emailService.sendOrderConfirmation(userProfile.getEmail(), totalAmount);
        smsService.sendOrderAlert(userProfile.getPhoneNumber());

        loggerService.log("Order processed for user: " + userId);

        reportService.recordSale(userId, totalAmount);

        analyticsService.trackPurchase(userId, products);

        complianceService.validateTransaction(userId, cart, paymentMethod);
    }
}