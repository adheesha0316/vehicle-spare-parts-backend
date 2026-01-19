package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.LoyaltyTransactionResponseDto;

import java.util.List;

public interface LoyaltyPointsService {
    int getPoints(Integer customerId);

    void earnPoints(
            Integer customerId,
            int points,
            String reason
    );

    void redeemPoints(
            Integer customerId,
            int points,
            String reason
    );

    List<LoyaltyTransactionResponseDto> getPointHistory(
            Integer customerId
    );

    boolean hasEnoughPoints(
            Integer customerId,
            int points
    );
}
