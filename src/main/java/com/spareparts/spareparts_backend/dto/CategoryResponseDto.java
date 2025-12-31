package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryResponseDto {
    private String key;
    private String labelEn;
    private String labelSi;
}
