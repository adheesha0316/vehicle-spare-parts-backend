package com.spareparts.spareparts_backend.enums;

public enum SpareItemStatus {
    PENDING,      // waiting admin approval
    APPROVED,     // visible to customers
    UPDATE_PENDING,
    DELETED
}
