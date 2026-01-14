package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.ReviewRequestDto;
import com.spareparts.spareparts_backend.dto.ReviewResponseDto;
import com.spareparts.spareparts_backend.entity.Customer;
import com.spareparts.spareparts_backend.entity.Review;
import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.CustomerRepo;
import com.spareparts.spareparts_backend.repo.ReviewRepo;
import com.spareparts.spareparts_backend.repo.SpareItemRepo;
import com.spareparts.spareparts_backend.service.ReviewService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepo reviewRepo;
    private final CustomerRepo customerRepo;
    private final SpareItemRepo spareItemRepo;

    // ================= ADD REVIEW =================

    @Override
    public ReviewResponseDto addReview(Integer customerId, ReviewRequestDto requestDto) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        SpareItem spareItem = spareItemRepo.findById(requestDto.getSpareItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Spare item not found"));

        // Prevent duplicate reviews
        if (reviewRepo.existsByCustomer_CustomerIdAndSpareItem_SpareItemId(
                customerId, spareItem.getSpareItemId())) {
            throw new IllegalStateException("You have already reviewed this item");
        }

        if (requestDto.getRating() < 1 || requestDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .customer(customer)
                .spareItem(spareItem)
                .rating(requestDto.getRating())
                .comment(requestDto.getComment())
                .build();

        reviewRepo.save(review);

        return mapToResponse(review);
    }

    // ================= UPDATE REVIEW =================

    @Override
    public ReviewResponseDto updateReview(Integer customerId, Integer reviewId, ReviewRequestDto requestDto) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Ownership check
        if (!review.getCustomer().getCustomerId().equals(customerId)) {
            throw new SecurityException("Unauthorized review update");
        }

        // Prevent changing target spare item
        if (!review.getSpareItem().getSpareItemId()
                .equals(requestDto.getSpareItemId())) {
            throw new IllegalStateException("Cannot change reviewed spare item");
        }

        // Rating validation
        if (requestDto.getRating() < 1 || requestDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Update allowed fields only
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());

        reviewRepo.save(review);

        return mapToResponse(review);
    }

    // ================= GET BY SPARE ITEM =================
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsBySpareItem(Integer spareItemId) {
        return reviewRepo.findBySpareItem_SpareItemId(spareItemId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= CUSTOMER REVIEWS =================

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getCustomerReviews(Integer customerId) {
        return reviewRepo.findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= DELETE (CUSTOMER) =================
    @Override
    public void deleteReview(Integer customerId, Integer reviewId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getCustomer().getCustomerId().equals(customerId)) {
            throw new SecurityException("Unauthorized review deletion");
        }

        reviewRepo.delete(review);
    }

    // ================= DELETE (ADMIN) =================
    @Override
    public void deleteReviewByAdmin(Integer reviewId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        reviewRepo.delete(review);
    }

    // ================= MAPPER =================
    private ReviewResponseDto mapToResponse(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .customerId(review.getCustomer().getCustomerId())
                .spareItemId(review.getSpareItem().getSpareItemId())
                .reviewText(review.getComment())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

}
