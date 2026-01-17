package com.spareparts.spareparts_backend.enums;

public enum ManagerStatus {
    PENDING_APPROVAL,          // waiting admin approval
    APPROVED,         // active
    UPDATE_PENDING,   // updated, waiting admin
    REJECTED,
    SUSPENDED,
    DELETED           // soft deleted
}
