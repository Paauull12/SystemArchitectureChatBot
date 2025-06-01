package com.enterprise.workflow;

import com.enterprise.auth.SessionValidator;
import com.enterprise.user.CustomerDirectory;
import com.enterprise.catalog.InventoryService;
import com.enterprise.catalog.ProductLookup;
import com.enterprise.cart.ShoppingCartService;
import com.enterprise.pricing.TaxCalculator;
import com.enterprise.discount.VoucherEngine;
import com.enterprise.payment.BillingGateway;
import com.enterprise.shipping.FulfillmentCenter;
import com.enterprise.notification.EmailService;
import com.enterprise.logging.EventTracker;
import com.enterprise.compliance.RegulatoryReporter;
import com.enterprise.analytics.UsageAnalytics;

public class EnterpriseOrderProcessor {

    private final SessionValidator sessionValidator;
    private final CustomerDirectory customerDirectory;
    private final InventoryService inventoryService;
    private final ProductLookup productLookup;
    private final ShoppingCartService shoppingCartService;
    private final TaxCalculator taxCalculator;
    private final VoucherEngine voucherEngine;
    private final BillingGateway billingGateway;
    private final FulfillmentCenter fulfillmentCenter;
    private final EmailService emailService;
    private final EventTracker eventTracker;
    private final RegulatoryReporter regulatoryReporter;
    private final UsageAnalytics usageAnalytics;

    public EnterpriseOrderProcessor(
        SessionValidator sessionValidator,
        CustomerDirectory customerDirectory,
        InventoryService inventoryService,
        ProductLookup productLookup,
        ShoppingCartService shoppingCartService,
        TaxCalculator taxCalculator,
        VoucherEngine voucherEngine,
        BillingGateway billingGateway,
        FulfillmentCenter fulfillmentCenter,
        EmailService emailService,
        EventTracker eventTracker,
        RegulatoryReporter regulatoryReporter,
        UsageAnalytics usageAnalytics
    ) {
        this.sessionValidator = sessionValidator;
        this.customerDirectory = customerDirectory;
        this.inventoryService = inventoryService;
        this.productLookup = productLookup;
        this.shoppingCartService = shoppingCartService;
        this.taxCalculator = taxCalculator;
        this.voucherEngine = voucherEngine;
        this.billingGateway = billingGateway;
        this.fulfillmentCenter = fulfillmentCenter;
        this.emailService = emailService;
        this.eventTracker = eventTracker;
        this.regulatoryReporter = regulatoryReporter;
        this.usageAnalytics = usageAnalytics;
    }

    public void processOrder(String userId, String sessionToken, String voucherCode) {
        if (!sessionValidator.validateSession(userId, sessionToken)) {
            System.out.println("Session validation failed.");
            return;
        }

        var user = customerDirectory.getCustomer(userId);
        var cart = shoppingCartService.loadCart(userId);
        var products = productLookup.getProducts(cart.getItemIds());

        if (!inventoryService.reserveItems(products)) {
            System.out.println("Insufficient stock.");
            return;
        }

        double subtotal = cart.calculateSubtotal(products);
        double tax = taxCalculator.calculate(subtotal, user.getLocation());
        double discount = voucherEngine.applyVoucher(voucherCode, subtotal);
        double total = subtotal + tax - discount;

        billingGateway.charge(user.getPaymentDetails(), total);
        var shipment = fulfillmentCenter.dispatch(user.getShippingInfo(), products);
        emailService.sendInvoice(user.getEmail(), total, shipment);
        
        eventTracker.record("OrderProcessed", userId);
        regulatoryReporter.reportTransaction(user, total);
        usageAnalytics.trackOrderMetrics(userId, total);
    }
}