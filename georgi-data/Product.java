package com.example.catalog;

import java.time.LocalDate;
import java.util.Objects;

public class Product {

    private String productId;
    private String name;
    private double price;
    private int stockQuantity;
    private LocalDate manufacturingDate;

    public Product(String productId, String name, double price, int stockQuantity, LocalDate manufacturingDate) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.manufacturingDate = manufacturingDate;
    }


    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    public boolean isAvailable() {
        return this.stockQuantity > 0;
    }

    public double getPriceAfterTax(double taxRate) {
        return this.price * (1 + taxRate);
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        } else {
            System.out.println("Not enough stock for product: " + name);
        }
    }
}

package com.example.inventory;

import com.example.catalog.Product;

public class InventoryManager {
    public void checkStockStatus(Product product) {
        if (product.isAvailable()) { 
            System.out.println(product.getName() + " is in stock.");
        } else {
            System.out.println(product.getName() + " is out of stock.");
        }
    }

    public void updateStock(Product product, int quantityChange) {
        product.decreaseStock(quantityChange);
        System.out.println(product.getName() + " stock updated to: " + product.getStockQuantity());
    }
}

package com.example.shoppingcart;

import com.example.catalog.Product;

public class CartService {
    public void addProductToCart(Product product, int quantity) {
        if (product.isAvailable() && product.getStockQuantity() >= quantity) { 
            System.out.println(quantity + " of " + product.getName() + " added to cart.");
        } else {
            System.out.println("Cannot add " + product.getName() + " to cart: insufficient stock or not available.");
        }
    }

    public double calculateItemTotal(Product product, int quantity, double taxRate) {
        return product.getPriceAfterTax(taxRate) * quantity;
    }
}

package com.example.pricing;
import com.example.catalog.Product;

public class PricingService {
    public double getFinalPrice(Product product, double taxRate) {
        return product.getPriceAfterTax(taxRate);
    }
}