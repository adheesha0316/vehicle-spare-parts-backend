package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.ReviewRequestDto;
import com.spareparts.spareparts_backend.dto.ReviewResponseDto;

import java.util.List;

public interface ReviewService {
    ReviewResponseDto addReview(
            Integer customerId,
            ReviewRequestDto requestDto
    );

    ReviewResponseDto updateReview(
            Integer customerId,
            Integer reviewId,
            ReviewRequestDto requestDto
    );

    void approveReview(
            Integer reviewId,
            boolean approve,
            String rejectionReason
    );

    List<ReviewResponseDto> getReviewsBySpareItem(Integer spareItemId);

    List<ReviewResponseDto> getReviewsByPartner(Integer partnerId);

    List<ReviewResponseDto> getReviewsByDelivery(Integer deliveryId);

    List<ReviewResponseDto> getCustomerReviews(Integer customerId);

    List<ReviewResponseDto> getAllReviews();

    void deleteReview(
            Integer customerId,
            Integer reviewId
    );

    // ADMIN override
    void deleteReviewByAdmin(Integer reviewId);
}
