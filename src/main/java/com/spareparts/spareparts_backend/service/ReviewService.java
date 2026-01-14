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


    List<ReviewResponseDto> getReviewsBySpareItem(Integer spareItemId);

    List<ReviewResponseDto> getCustomerReviews(Integer customerId);

    void deleteReview(
            Integer customerId,
            Integer reviewId
    );

    // ADMIN override
    void deleteReviewByAdmin(Integer reviewId);
}
