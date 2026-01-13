package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.OrderResponseDto;
import com.spareparts.spareparts_backend.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    // ================= PLACE ORDER =================
    OrderResponseDto placeOrder(Integer customerId);

    // ================= CUSTOMER =================
    List<OrderResponseDto> getOrderHistory(Integer customerId);

    OrderResponseDto getOrderDetails(Integer orderId);

    void cancelOrder(
            Integer customerId,
            Integer orderId,
            String reason
    );

    // ================= ADMIN / PARTNER =================
    void updateOrderStatus(
            Integer orderId,
            OrderStatus status
    );

    List<OrderResponseDto> getAllOrders();

    // ================= INTERNAL =================
    boolean canCancelOrder(Integer orderId);
}
