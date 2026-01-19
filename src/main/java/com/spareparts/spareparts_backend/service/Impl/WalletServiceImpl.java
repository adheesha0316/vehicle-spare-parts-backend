package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.WalletTransactionResponseDto;
import com.spareparts.spareparts_backend.entity.Wallet;
import com.spareparts.spareparts_backend.entity.WalletTransaction;
import com.spareparts.spareparts_backend.enums.WalletReferenceType;
import com.spareparts.spareparts_backend.enums.WalletTransactionType;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.WalletRepo;
import com.spareparts.spareparts_backend.repo.WalletTransactionRepo;
import com.spareparts.spareparts_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepo walletRepo;
    private final WalletTransactionRepo transactionRepo;


    @Override
    public double getBalance(Integer customerId) {
        return walletRepo.findByCustomer_CustomerId(customerId)
                .map(wallet -> wallet.getBalance().doubleValue())
                .orElse(0.0);
    }

    @Override
    @Transactional
    public void credit(Integer customerId, double amount, String reason) {
        // Default to MANUAL and null ID if not provided
        this.credit(customerId, amount, reason, WalletReferenceType.MANUAL, null);
    }

    @Override
    @Transactional
    public void debit(Integer customerId, double amount, String reason) {
        // Default to ORDER and null ID (you should ideally pass the order ID here)
        this.debit(customerId, amount, reason, WalletReferenceType.ORDER, null);
    }

    @Override
    public void credit(Integer customerId, double amount, String reason, WalletReferenceType refType, Integer refId) {
        processWalletUpdate(
                customerId,
                BigDecimal.valueOf(amount),
                WalletTransactionType.CREDIT,
                refType,
                refId,
                reason
        );
    }

    @Override
    public void debit(Integer customerId, double amount, String reason, WalletReferenceType refType, Integer refId) {
        BigDecimal debitAmount = BigDecimal.valueOf(amount);
        Wallet wallet = walletRepo.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(debitAmount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        processWalletUpdate(
                customerId,
                debitAmount.negate(),
                WalletTransactionType.DEBIT,
                refType,
                refId,
                reason
        );
    }

    @Override
    public List<WalletTransactionResponseDto> getTransactionHistory(Integer customerId) {
        Wallet wallet = walletRepo.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for customer: " + customerId));

        return transactionRepo.findByWallet_WalletIdOrderByCreatedAtDesc(wallet.getWalletId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public boolean hasSufficientBalance(Integer customerId, double amount) {
        BigDecimal required = BigDecimal.valueOf(amount);
        BigDecimal current = walletRepo.findByCustomer_CustomerId(customerId)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        return current.compareTo(required) >= 0;
    }

    /**
     * Internal helper to handle balance updates and audit logging atomically.
     */
    private void processWalletUpdate(
            Integer customerId,
            BigDecimal amount,
            WalletTransactionType type,
            WalletReferenceType refType,
            Integer refId,
            String reason) {

        Wallet wallet = walletRepo.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet record missing for customer: " + customerId));

        // 1. Calculate and update new balance
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepo.save(wallet);

        // 2. Create and save the audit transaction
        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .referenceType(refType)
                .referenceId(refId)
                .balanceAfter(newBalance)
                .description(reason)
                .build();

        transactionRepo.save(tx);
    }

    private WalletTransactionResponseDto mapToDto(WalletTransaction entity) {
        return WalletTransactionResponseDto.builder()
                .transactionId(entity.getTransactionId())
                .customerId(entity.getWallet().getCustomer().getCustomerId())
                .amount(entity.getAmount())
                .transactionType(entity.getType().name())
                .referenceType(entity.getReferenceType() != null ? entity.getReferenceType().name() : null)
                .referenceId(entity.getReferenceId())
                .balanceAfter(entity.getBalanceAfter())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
