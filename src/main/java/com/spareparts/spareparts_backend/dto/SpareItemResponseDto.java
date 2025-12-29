package com.spareparts.spareparts_backend.dto;

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

    // e.g., "IN_STOCK" or "OUT_OF_STOCK"
    private String stockStatus;

    // URLs or relative paths of uploaded images
    private List<String> imagePaths;

    // Status: PENDING, APPROVED, DELETED
    private SpareItemStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Manager who added the item
    private Integer managerId;

    // Optional: who approved it (Admin id) if needed
    private Integer approvedByAdminId;
}
