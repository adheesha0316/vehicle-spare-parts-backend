package com.spareparts.spareparts_backend.dto;

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
    private Integer serviceId;       // optional: service or partner/product being reviewed
    private String reviewText;
    private int rating;              // 1 to 5 stars
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
