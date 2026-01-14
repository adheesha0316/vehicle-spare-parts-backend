package com.spareparts.spareparts_backend.dto;


import com.spareparts.spareparts_backend.enums.SpareItemCategory;
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
    private String brand;
    private String description;
    private SpareItemCategory category;
    private Double price;
    private Integer quantity;

    // IN_STOCK, OUT_OF_STOCK, LOW_STOCK
    private String stockStatus;

    // Used for update scenarios
    private List<String> existingImagePaths;
}

