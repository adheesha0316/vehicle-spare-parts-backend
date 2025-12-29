package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.AdminApprovalStatus;
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
    private String brand;
    private String description;
    private String category;
    private Double price;

    // e.g., "IN_STOCK", "OUT_OF_STOCK" (can change to enum later)
    private String stockStatus;

    // 1-5 uploaded image paths
    private List<String> imagePaths;

    // Item soft delete / active status
    private SpareItemStatus status; // PENDING, APPROVED, DELETED

    // Admin approval status for updates/deletes
    private AdminApprovalStatus approvalStatus; // PENDING, APPROVED, REJECTED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Reference to the manager who added it
    private Integer managerId;

    // Role of the user who added it (MANAGER / ADMIN)
    private String addedByRole;
}
