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
    private String stockStatus;
    private List<String> imagePaths;
    private SpareItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer managerId;
    private Integer approvedByAdminId;
}
