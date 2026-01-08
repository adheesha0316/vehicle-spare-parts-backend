package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.WalletTransactionResponseDto;

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

    List<WalletTransactionResponseDto> getTransactionHistory(
            Integer customerId
    );

    boolean hasSufficientBalance(
            Integer customerId,
            double amount
    );
}
