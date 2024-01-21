package com.conversion.currencyConverter;

public enum Currency{
    Dollar("Dollar"), Euro("Euro"), Pound("Pound");

    private final String name;

    Currency(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}