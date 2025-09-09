package com.example.TTECHT.enumuration;

public enum CancellationReason {
    // System/Business reasons
    OUT_OF_STOCK("Product out of stock"),
    PAYMENT_FAILED("Payment processing failed"),
    DELIVERY_DELAY("Delivery delay beyond acceptable time"),
    DUPLICATE_ORDER("Duplicate order placed"),
    FRAUD_DETECTION("Fraud detection triggered"),
    INCORRECT_ADDRESS("Incorrect delivery address"),
    SYSTEM_ERROR("System error occurred"),
    ADMIN_DECISION("Administrative decision"),
    QUALITY_ISSUE("Product quality issue"),
    SHIPPING_RESTRICTION("Shipping restriction to destination"),
    
    // User/Customer decision reasons
    CUSTOMER_CHANGED_MIND("Customer changed mind"),
    FOUND_BETTER_PRICE("Found better price elsewhere"),
    NO_LONGER_NEEDED("No longer needed the product"),
    ORDERED_WRONG_ITEM("Ordered wrong item by mistake"),
    ORDERED_WRONG_SIZE("Ordered wrong size"),
    ORDERED_WRONG_COLOR("Ordered wrong color"),
    FINANCIAL_CONSTRAINTS("Financial constraints"),
    DELIVERY_TOO_SLOW("Delivery is too slow"),
    DELIVERY_TOO_EXPENSIVE("Delivery cost is too expensive"),
    RECEIVED_AS_GIFT("Received the item as a gift"),
    PRODUCT_REVIEWS_NEGATIVE("Read negative product reviews"),
    STORE_POLICY_CONCERNS("Concerns about store return/exchange policy"),
    PREFER_IN_STORE_PURCHASE("Decided to purchase in physical store"),
    FAMILY_MEMBER_OBJECTION("Family member objected to purchase"),
    BUDGET_REALLOCATED("Reallocated budget to other priorities"),
    IMPULSE_PURCHASE_REGRET("Regret impulse purchase decision"),
    PRODUCT_AVAILABILITY_ELSEWHERE("Found product available elsewhere sooner"),
    PROMOTIONAL_OFFER_ENDED("Promotional offer ended"),
    CHANGE_OF_PLANS("Change of plans or circumstances"),
    OTHER("Other reason");

    private final String description;

    CancellationReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
