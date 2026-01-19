package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionResponseDto {
    private Integer transactionId;
    private Integer customerId;
    private BigDecimal amount;                 // + credit / - debit
    private String transactionType;        // TOP_UP, ORDER_PAYMENT, REFUND
    private String referenceType;           // ORDER, REFUND, MANUAL
    private Integer referenceId;            // orderId / refundId
    private BigDecimal balanceAfter;            // wallet balance after txn
    private String description;
    private LocalDateTime createdAt;
}
