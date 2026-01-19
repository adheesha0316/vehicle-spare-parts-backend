package com.spareparts.spareparts_backend.enums;

public enum WalletReferenceType {
    ORDER,         // Linked to an Order ID
    REFUND,        // Linked to a Refund record
    PROMOTION,     // Linked to a marketing campaign
    MANUAL         // No specific automated reference
}
