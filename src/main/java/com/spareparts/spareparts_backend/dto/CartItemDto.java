package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Integer cartItemId;
    private Integer spareItemId;
    private String spareItemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
