package com.spareparts.spareparts_backend.dto;

import java.time.LocalDateTime;

public class LoyaltyTransactionResponseDto {
    private Integer transactionId;

    private Integer customerId;

    private int points;                    // + earned / - redeemed

    private String transactionType;        // EARNED, REDEEMED, EXPIRED

    private String sourceType;             // ORDER, PROMOTION, REFERRAL

    private Integer sourceId;              // orderId / promoId

    private int balanceAfter;              // loyalty points after txn

    private String description;

    private LocalDateTime createdAt;
}
