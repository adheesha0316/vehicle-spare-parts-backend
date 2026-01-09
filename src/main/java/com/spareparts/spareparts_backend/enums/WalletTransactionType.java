package com.spareparts.spareparts_backend.enums;

public enum WalletTransactionType {
    CREDIT,     // add money
    DEBIT,      // spend money
    REFUND,     // refund from order
    ADJUSTMENT  // admin/manual correction
}
