package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.OwnershipStatus;
import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpareItemResponseDto {

    private Integer spareItemId;
    private String name;
    private String brand;
    private String description;
    private String category;
    private Double price;

    // IN_STOCK, OUT_OF_STOCK, LOW_STOCK
    private String stockStatus;

    private List<String> imagePaths;

    // PENDING, APPROVED, DELETED
    private SpareItemStatus status;

    // PLATFORM_OWNER or PARTNER
    private OwnershipStatus ownership;

    // Only for PARTNER-owned items (null for PLATFORM_OWNER)
    private Integer partnerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Admin / Manager who approved
    private Integer approvedByUserId;
}
