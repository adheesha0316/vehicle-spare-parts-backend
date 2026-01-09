package com.spareparts.spareparts_backend.enums;

public enum CustomerStatus {
    ACTIVE,        // Normal customer – can buy, review, add to cart

    SUSPENDED,     // Temporarily blocked (fraud, abuse, too many cancellations)

    DELETE_REQUESTED,

    DELETED        // Account removed / soft delete
}
