package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemDto {
    private Integer spareItemId;
    private int quantity;

}
