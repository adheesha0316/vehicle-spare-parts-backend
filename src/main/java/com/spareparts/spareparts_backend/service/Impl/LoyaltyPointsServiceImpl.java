package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.LoyaltyTransactionResponseDto;
import com.spareparts.spareparts_backend.entity.LoyaltyPoints;
import com.spareparts.spareparts_backend.entity.LoyaltyTransaction;
import com.spareparts.spareparts_backend.enums.LoyaltyTransactionType;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.LoyaltyPointsRepo;
import com.spareparts.spareparts_backend.repo.LoyaltyTransactionRepo;
import com.spareparts.spareparts_backend.service.LoyaltyPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyPointsServiceImpl implements LoyaltyPointsService {

    private final LoyaltyPointsRepo pointsRepo;
    private final LoyaltyTransactionRepo transactionRepo;

    @Override
    public int getPoints(Integer customerId) {
        return pointsRepo.findByCustomer_CustomerId(customerId)
                .map(LoyaltyPoints::getPoints)
                .orElse(0);
    }

    @Override
    @Transactional
    public void earnPoints(Integer customerId, int points, String reason) {
        processLoyaltyUpdate(customerId, points, LoyaltyTransactionType.EARNED, reason);
    }

    @Override
    @Transactional
    public void redeemPoints(Integer customerId, int points, String reason) {
        int currentBalance = getPoints(customerId);
        if (currentBalance < points) {
            throw new IllegalStateException("Insufficient points. Available: " + currentBalance);
        }
        processLoyaltyUpdate(customerId, -points, LoyaltyTransactionType.REDEEMED, reason);
    }

    @Override
    public List<LoyaltyTransactionResponseDto> getPointHistory(Integer customerId) {
        LoyaltyPoints loyalty = pointsRepo.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for customer: " + customerId));

        return transactionRepo.findByLoyaltyPoints_PointIdOrderByCreatedAtDesc(loyalty.getPointId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public boolean hasEnoughPoints(Integer customerId, int points) {
        return getPoints(customerId) >= points;
    }

    private void processLoyaltyUpdate(Integer customerId, int amount, LoyaltyTransactionType type, String reason) {
        LoyaltyPoints loyalty = pointsRepo.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty record not found"));

        int newBalance = loyalty.getPoints() + amount;
        loyalty.setPoints(newBalance);
        pointsRepo.save(loyalty);

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .loyaltyPoints(loyalty)
                .points(amount)
                .type(type)
                .pointsAfter(newBalance)
                .description(reason)
                .build();

        transactionRepo.save(tx);
    }

    private LoyaltyTransactionResponseDto mapToDto(LoyaltyTransaction entity) {
        return LoyaltyTransactionResponseDto.builder()
                .transactionId(entity.getTransactionId())
                .customerId(entity.getLoyaltyPoints().getCustomer().getCustomerId())
                .points(entity.getPoints())
                .transactionType(entity.getType().name())
                .balanceAfter(entity.getPointsAfter()) // Entity pointsAfter -> DTO balanceAfter
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
