package com.example.TTECHT.constant;

public class OrderConstants {
    
    // Order number prefix
    public static final String ORDER_NUMBER_PREFIX = "ORD-TTECHT";
    
    // Default values
    public static final String DEFAULT_CREATED_BY = "system";
    public static final String DEFAULT_STOCK_CODE = "STK";
    public static final Double DEFAULT_DISCOUNT_PRICE = 0.0;
    
    // Order status
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_PROCESSING = "PROCESSING";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    
    private OrderConstants() {
        // Prevent instantiation
    }
} 