package com.spareparts.spareparts_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpareItemRequestDto {

    private String name;
    private String description;
    private String category;
    private Double price;
    private String stockStatus;       // e.g., "IN_STOCK" or "OUT_OF_STOCK"
    private List<String> imagePaths;  // for update/add (or MultipartFile if via upload)
}

