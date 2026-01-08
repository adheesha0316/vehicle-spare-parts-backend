package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private Integer targetId;   // productId / partnerId / orderId
    private int rating;         // 1–5
    private String comment;
}
