package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.ReviewRequestDto;
import com.spareparts.spareparts_backend.dto.ReviewResponseDto;
import com.spareparts.spareparts_backend.security.CustomUserDetails;
import com.spareparts.spareparts_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
@CrossOrigin
public class ReviewController {
    private final ReviewService reviewService;

    // ================= CUSTOMER ACTIONS =================

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponseDto> addReview(
            @RequestBody ReviewRequestDto requestDto,
            Authentication authentication
    ) {
        Integer customerId = getUserId(authentication);
        return new ResponseEntity<>(reviewService.addReview(customerId, requestDto), HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Integer reviewId,
            @RequestBody ReviewRequestDto requestDto,
            Authentication authentication
    ) {
        Integer customerId = getUserId(authentication);
        return ResponseEntity.ok(reviewService.updateReview(customerId, reviewId, requestDto));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer reviewId,
            Authentication authentication
    ) {
        Integer customerId = getUserId(authentication);
        reviewService.deleteReview(customerId, reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews(Authentication authentication) {
        Integer customerId = getUserId(authentication);
        return ResponseEntity.ok(reviewService.getCustomerReviews(customerId));
    }

    // ================= PUBLIC ACTIONS =================

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByItem(@PathVariable Integer itemId) {
        return ResponseEntity.ok(reviewService.getReviewsBySpareItem(itemId));
    }

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByPartner(@PathVariable Integer partnerId) {
        return ResponseEntity.ok(reviewService.getReviewsByPartner(partnerId));
    }

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByDelivery(@PathVariable Integer deliveryId) {
        return ResponseEntity.ok(reviewService.getReviewsByDelivery(deliveryId));
    }

    // ================= PRIVATE HELPER =================

    private Integer getUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
