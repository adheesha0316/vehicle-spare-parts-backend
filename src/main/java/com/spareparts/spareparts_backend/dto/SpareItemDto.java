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
public class SpareItemDto {
    private Integer spareItemId;
    private String name;
    private String description;
    private String category;
    private Double price;
    private String stockStatus; // e.g., "IN_STOCK", "OUT_OF_STOCK"

    private List<String> imagePaths; // paths of uploaded images (1-5 images)

    private SpareItemStatus status; // PENDING, APPROVED, DELETED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer managerId; // reference to manager who added it
}
