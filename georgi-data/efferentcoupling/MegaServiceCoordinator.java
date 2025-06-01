package com.company.megaservice;

import com.company.auth.AuthenticationService;
import com.company.user.UserProfileService;
import com.company.catalog.ProductCatalog;
import com.company.inventory.StockManager;
import com.company.cart.ShoppingCartManager;
import com.company.promotion.DiscountService;
import com.company.payment.PaymentGateway;
import com.company.shipping.ShippingService;
import com.company.notification.EmailNotification;
import com.company.notification.SmsNotification;
import com.company.logging.ActivityLogger;
import com.company.reporting.FinancialReportService;
import com.company.analytics.UserBehaviorAnalytics;
import com.company.compliance.DataComplianceService;

public class MegaServiceCoordinator {

    private final AuthenticationService authenticationService;
    private final UserProfileService userProfileService;
    private final ProductCatalog productCatalog;
    private final StockManager stockManager;
    private final ShoppingCartManager shoppingCartManager;
    private final DiscountService discountService;
    private final PaymentGateway paymentGateway;
    private final ShippingService shippingService;
    private final EmailNotification emailNotification;
    private final SmsNotification smsNotification;
    private final ActivityLogger activityLogger;
    private final FinancialReportService financialReportService;
    private final UserBehaviorAnalytics userBehaviorAnalytics;
    private final DataComplianceService dataComplianceService;

    public MegaServiceCoordinator(
            AuthenticationService authenticationService,
            UserProfileService userProfileService,
            ProductCatalog productCatalog,
            StockManager stockManager,
            ShoppingCartManager shoppingCartManager,
            DiscountService discountService,
            PaymentGateway paymentGateway,
            ShippingService shippingService,
            EmailNotification emailNotification,
            SmsNotification smsNotification,
            ActivityLogger activityLogger,
            FinancialReportService financialReportService,
            UserBehaviorAnalytics userBehaviorAnalytics,
            DataComplianceService dataComplianceService
    ) {
        this.authenticationService = authenticationService;
        this.userProfileService = userProfileService;
        this.productCatalog = productCatalog;
        this.stockManager = stockManager;
        this.shoppingCartManager = shoppingCartManager;
        this.discountService = discountService;
        this.paymentGateway = paymentGateway;
        this.shippingService = shippingService;
        this.emailNotification = emailNotification;
        this.smsNotification = smsNotification;
        this.activityLogger = activityLogger;
        this.financialReportService = financialReportService;
        this.userBehaviorAnalytics = userBehaviorAnalytics;
        this.dataComplianceService = dataComplianceService;
    }

    public void handleUserCheckout(String userId, String paymentMethodId, String promoCode) {
        if (!authenticationService.isAuthenticated(userId)) {
            System.out.println("User authentication failed.");
            return;
        }

        var userProfile = userProfileService.getUserProfile(userId);
        var cart = shoppingCartManager.getCartForUser(userId);
        var products = productCatalog.getProducts(cart.getProductIds());

        if (!stockManager.reserveStock(products)) {
            System.out.println("Some products are out of stock.");
            return;
        }

        double subtotal = cart.calculateSubtotal(products);
        double discount = discountService.calculateDiscount(promoCode, subtotal);
        double totalAmount = subtotal - discount;

        paymentGateway.charge(paymentMethodId, totalAmount);

        shippingService.scheduleDelivery(userProfile.getAddress(), products);

        emailNotification.sendReceipt(userProfile.getEmail(), totalAmount);
        smsNotification.sendConfirmation(userProfile.getPhoneNumber());

        activityLogger.log("User " + userId + " completed checkout with total: " + totalAmount);

        financialReportService.recordTransaction(userId, totalAmount);

        userBehaviorAnalytics.trackPurchase(userId, products);

        dataComplianceService.ensureCompliance(userId, cart, paymentMethodId);
    }
}