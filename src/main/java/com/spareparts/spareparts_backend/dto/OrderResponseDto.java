package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Integer orderId;
    private Integer customerId;
    private LocalDateTime orderDate;
    private String orderStatus;  // e.g., PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    private double totalAmount;

    private List<OrderItemDto> items;  // list of ordered items

}
