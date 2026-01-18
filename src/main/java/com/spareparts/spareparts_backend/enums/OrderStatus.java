package com.spareparts.spareparts_backend.enums;

public enum OrderStatus {
    // Phase 1: Payment
    PENDING_PAYMENT, PAID, CONFIRMED,

    // Phase 2: Partner fulfillment
    PROCESSING, READY_FOR_PICKUP,

    // Phase 3: Logistics
    COURIER_ASSIGNED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY,

    // Phase 4: Final
    DELIVERED, CANCELLED,

    // Phase 5: Reverse Logistics
    RETURN_REQUESTED, RETURN_PICKED_UP, RETURNED, REFUNDED, FAILED
}
