package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.ReviewTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Integer reviewId;
    private Integer customerId;
    private String customerName;
    private ReviewTargetType targetType;
    private Integer targetId;       // optional: service or partner/product being reviewed
    private String reviewText;
    private int rating;              // 1 to 5 stars
    private boolean approved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
