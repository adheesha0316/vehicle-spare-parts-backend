package com.spareparts.spareparts_backend.enums;

public enum OrderStatus {
    PENDING,        // Order created but not yet confirmed
    CONFIRMED,      // Order confirmed by the system/partner
    PROCESSING,     // Partner is preparing the order
    SHIPPED,        // Order shipped to customer
    OUT_FOR_DELIVERY, // Courier out for delivery
    DELIVERED,      // Order successfully delivered
    CANCELLED,      // Order cancelled by customer or system
    RETURN_REQUESTED, // Customer requested a return
    RETURNED,       // Order returned
    FAILED          // Payment or delivery failed
}
