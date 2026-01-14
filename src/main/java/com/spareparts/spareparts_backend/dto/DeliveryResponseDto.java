package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseDto {
    private Integer deliveryId;
    private Integer orderId;
    private OrderStatus status;
    private String trackingNumber;
    private String courierName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
