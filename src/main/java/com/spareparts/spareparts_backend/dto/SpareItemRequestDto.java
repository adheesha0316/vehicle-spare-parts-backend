package com.spareparts.spareparts_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpareItemRequestDto {

    private String name;
    private String description;
    private String category;
    private Double price;

    // e.g., "IN_STOCK", "OUT_OF_STOCK"
    private String stockStatus;

    // Uploaded images (1-5 images)
    private List<MultipartFile> images;

    // Optional: existing image paths (for update scenarios)
    private List<String> imagePaths;
}

