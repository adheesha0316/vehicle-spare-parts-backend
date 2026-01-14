package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequestDto {
    @NotNull(message = "Order ID is required")
    private Integer orderId;

    @NotNull(message = "Status is required")
    private OrderStatus status;

    @Size(max = 100, message = "Courier name must not exceed 100 characters")
    private String courierName; // optional
}
