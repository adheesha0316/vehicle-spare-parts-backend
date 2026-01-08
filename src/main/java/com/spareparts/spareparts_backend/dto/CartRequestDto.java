package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRequestDto {

    private Integer spareItemId;      // ID of the spare part
    private String spareItemName;     // Optional: for convenience
    private int quantity;
    private double unitPrice;
}
