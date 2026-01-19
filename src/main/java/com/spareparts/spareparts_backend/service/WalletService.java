package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.WalletTransactionResponseDto;
import com.spareparts.spareparts_backend.enums.WalletReferenceType;

import java.util.List;

public interface WalletService {
    double getBalance(Integer customerId);

    void credit(
            Integer customerId,
            double amount,
            String reason
    );

    void debit(
            Integer customerId,
            double amount,
            String reason
    );

    void credit(Integer customerId, double amount, String reason, WalletReferenceType refType, Integer refId);
    void debit(Integer customerId, double amount, String reason, WalletReferenceType refType, Integer refId);

    List<WalletTransactionResponseDto> getTransactionHistory(
            Integer customerId
    );

    boolean hasSufficientBalance(
            Integer customerId,
            double amount
    );
}
