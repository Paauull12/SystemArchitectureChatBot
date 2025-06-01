package com.example.finance;

public class CurrencyConverter {

    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals("USD") && toCurrency.equals("EUR")) {
            return amount * 0.85;
        }
        if (fromCurrency.equals("EUR") && toCurrency.equals("USD")) {
            return amount * 1.18;
        }
        return amount;
    }
}
