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

    // NEW: Assign a Courier to an Order (Used by Admin or Partner)
    void assignCourierToOrder(Integer orderId, Integer courierId);

    // NEW: Get Orders that the Platform (Admin) needs to fulfill
    List<OrderResponseDto> getPlatformOrders();

    // NEW: Get Orders for a specific Partner
    List<OrderResponseDto> getPartnerOrders(Integer partnerId);

    // ================= INTERNAL =================
    boolean canCancelOrder(Integer orderId);
}
