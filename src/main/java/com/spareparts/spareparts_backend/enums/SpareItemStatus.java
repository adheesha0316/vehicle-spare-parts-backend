package com.spareparts.spareparts_backend.enums;

public enum SpareItemStatus {
    PENDING,          // Newly created (Partner / Manager) – waiting approval
    APPROVED,         // Approved – visible to customers
    REJECTED,         // Rejected by Admin or Manager
    UPDATE_PENDING,   // Update requested – waiting approval
    DELETED           // Soft delete
}
