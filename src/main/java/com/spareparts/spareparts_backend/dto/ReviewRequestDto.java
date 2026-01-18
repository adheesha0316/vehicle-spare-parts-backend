package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.ReviewTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private ReviewTargetType targetType; // PLATFORM, SPARE_ITEM, etc.
    private Integer spareItemId;         // targetType == SPARE_ITEM නම් පමණයි
    private Integer partnerId;           // targetType == PARTNER නම් පමණයි
    private Integer deliveryId;          // targetType == DELIVERY නම් පමණයි
    private int rating;                  // 1–5
    private String comment;
}
