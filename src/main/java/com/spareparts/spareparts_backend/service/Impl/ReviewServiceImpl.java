package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.ReviewRequestDto;
import com.spareparts.spareparts_backend.dto.ReviewResponseDto;
import com.spareparts.spareparts_backend.entity.Customer;
import com.spareparts.spareparts_backend.entity.Review;
import com.spareparts.spareparts_backend.exception.BadRequestException;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.*;
import com.spareparts.spareparts_backend.service.ReviewService;
import org.springframework.security.access.AccessDeniedException;
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
    private final PartnerRepo partnerRepo;
    private final DeliveryRepo deliveryRepo;

    // ================= ADD REVIEW =================

    @Override
    public ReviewResponseDto addReview(Integer customerId, ReviewRequestDto requestDto) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // 1. Target එක අනුව validation සහ Object එක fetch කිරීම
        Review review = Review.builder()
                .customer(customer)
                .targetType(requestDto.getTargetType())
                .rating(requestDto.getRating())
                .comment(requestDto.getComment())
                .approved(false) // Default approval false (Admin must approve)
                .build();

        validateAndSetTarget(review, requestDto, customerId);

        return mapToResponse(reviewRepo.save(review));
    }

    // ================= UPDATE REVIEW =================

    @Override
    public ReviewResponseDto updateReview(Integer customerId, Integer reviewId, ReviewRequestDto requestDto) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Ownership Check
        if (!review.getCustomer().getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You can only update your own reviews");
        }

        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());
        review.setApproved(false); // Update කළ පසු නැවත Admin approval අවශ්‍යයි

        return mapToResponse(reviewRepo.save(review));
    }

    // ================= APPROVE REVIEW (ADMIN/MANAGER) =================
    @Override
    public void approveReview(Integer reviewId, boolean approve, String rejectionReason) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setApproved(approve);
        review.setRejectionReason(approve ? null : rejectionReason);
        reviewRepo.save(review);
    }

    // ================= HELPER: VALIDATE TARGET & DUPLICATES =================
    private void validateAndSetTarget(Review review, ReviewRequestDto dto, Integer customerId) {
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        switch (dto.getTargetType()) {
            case SPARE_ITEM -> {
                if (dto.getSpareItemId() == null) throw new BadRequestException("Spare Item ID is required");
                if (reviewRepo.existsByCustomer_CustomerIdAndSpareItem_SpareItemId(customerId, dto.getSpareItemId()))
                    throw new BadRequestException("You already reviewed this item");

                review.setSpareItem(spareItemRepo.findById(dto.getSpareItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found")));
            }
            case PARTNER -> {
                if (dto.getPartnerId() == null) throw new BadRequestException("Partner ID is required");
                review.setPartner(partnerRepo.findById(dto.getPartnerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Partner not found")));
            }
            case DELIVERY -> {
                if (dto.getDeliveryId() == null) throw new BadRequestException("Delivery ID is required");
                review.setDelivery(deliveryRepo.findById(dto.getDeliveryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Delivery record not found")));
            }
            case PLATFORM -> { /* No specific entity needed */ }
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByPartner(Integer partnerId) {
        return reviewRepo.findAll().stream() // Ideally add findByPartner to Repo
                .filter(r -> r.getPartner() != null && r.getPartner().getPartnerId().equals(partnerId))
                .filter(Review::isApproved)
                .map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByDelivery(Integer deliveryId) {
        return reviewRepo.findAll().stream() // Ideally add findByDelivery to Repo
                .filter(r -> r.getDelivery() != null && r.getDelivery().getDeliveryId().equals(deliveryId))
                .filter(Review::isApproved)
                .map(this::mapToResponse).toList();
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

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getAllReviews() {
        return reviewRepo.findAll().stream()
                .map(this::mapToResponse).toList();
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
        reviewRepo.deleteById(reviewId);
    }

    // ================= MAPPER =================
    private ReviewResponseDto mapToResponse(Review review) {
        Integer targetId = null;
        if (review.getSpareItem() != null) targetId = review.getSpareItem().getSpareItemId();
        else if (review.getPartner() != null) targetId = review.getPartner().getPartnerId();
        else if (review.getDelivery() != null) targetId = review.getDelivery().getDeliveryId();

        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .customerId(review.getCustomer().getCustomerId())
                .targetId(targetId)
                .targetType(review.getTargetType()) // Add this to your DTO if not present
                .reviewText(review.getComment())
                .rating(review.getRating())
                .approved(review.isApproved())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

}
