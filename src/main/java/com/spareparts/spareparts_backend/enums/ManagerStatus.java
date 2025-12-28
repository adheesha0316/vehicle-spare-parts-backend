package com.spareparts.spareparts_backend.enums;

public enum ManagerStatus {
    PENDING,          // waiting admin approval
    APPROVED,         // active
    UPDATE_PENDING,   // updated, waiting admin
    REJECTED,
    SUSPENDED,
    DELETED           // soft deleted
}
