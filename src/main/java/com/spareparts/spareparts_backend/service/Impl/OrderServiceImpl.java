package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.OrderItemDto;
import com.spareparts.spareparts_backend.dto.OrderResponseDto;
import com.spareparts.spareparts_backend.entity.*;
import com.spareparts.spareparts_backend.enums.CustomerStatus;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.enums.StockStatus;
import com.spareparts.spareparts_backend.exception.OrderCancellationNotAllowedException;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.CartRepo;
import com.spareparts.spareparts_backend.repo.CustomerRepo;
import com.spareparts.spareparts_backend.repo.OrderItemRepo;
import com.spareparts.spareparts_backend.repo.OrderRepo;
import com.spareparts.spareparts_backend.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CartRepo cartRepo;
    private final CustomerRepo customerRepo;

    // ================= PLACE ORDER =================

    @Override
    public OrderResponseDto placeOrder(Integer customerId) {
        // ================= GET CUSTOMER =================
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer account is not active");
        }

        // ================= GET CART =================
        Cart cart = cartRepo.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        // ================= CREATE ORDER =================
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO) // Initialize totalAmount
                .items(new ArrayList<>())     // Initialize items
                .statusUpdates(new ArrayList<>()) // Initialize statusUpdates
                .createdAt(LocalDateTime.now())
                .build();

        order = orderRepo.save(order);

        // ================= CREATE ORDER ITEMS =================
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            BigDecimal unitPrice = BigDecimal.valueOf(cartItem.getSpareItem().getPrice());
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .spareItem(cartItem.getSpareItem())
                    .spareItemName(cartItem.getSpareItem().getName()) // Snapshot of name
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            order.getItems().add(orderItem); // Add to order's items list
        }

        order.setTotalAmount(totalAmount); // Set final total amount

        // ================= STATUS HISTORY =================
        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(OrderStatus.PENDING.name())
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // ================= CLEAR CART =================
        cart.getItems().clear();
        cartRepo.save(cart); // Persist cart clearing

        // ================= MAP TO RESPONSE =================
        return mapToResponse(order);
    }

    // ================= CUSTOMER =================

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrderHistory(Integer customerId) {
        return orderRepo.findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderDetails(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToResponse(order);
    }

    @Override
    public void cancelOrder(Integer customerId, Integer orderId, String reason) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check ownership
        if (!order.getCustomer().getCustomerId().equals(customerId)) {
            throw new SecurityException("Unauthorized order access");
        }

        // Prevent double cancellation / invalid status
        if (!canCancelOrder(orderId)) {
            throw new OrderCancellationNotAllowedException(
                    "Order cannot be cancelled at this stage (status: " + order.getStatus() + ")"
            );
        }

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);

        // Add status update for audit
        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(OrderStatus.CANCELLED.name())
                        .timestamp(LocalDateTime.now())
                        .reason(reason)
                        .build()
        );

        // Restock items in the order
        for (OrderItem item : order.getItems()) {
            SpareItem spareItem = item.getSpareItem();
            // Add back the quantity to stock
            spareItem.setQuantity(spareItem.getQuantity() + item.getQuantity());
            // Optional: update stock status
            if (spareItem.getQuantity() > 0) {
                spareItem.setStockStatus(StockStatus.IN_STOCK);
            }
        }

        // Save changes
        orderRepo.save(order);
    }

    // ================= ADMIN / PARTNER =================

    @Override
    public void updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);

        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(status.name())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= INTERNAL =================

    @Override
    public boolean canCancelOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Only PENDING or CONFIRMED orders can be cancelled
        return order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED;
    }

    // ================= MAPPER =================

    private OrderResponseDto mapToResponse(Order order) {

        List<OrderItemDto> items = orderItemRepo
                .findByOrder_OrderId(order.getOrderId())
                .stream()
                .map(item -> OrderItemDto.builder()
                        .spareItemId(item.getSpareItem().getSpareItemId())
                        .spareItemName(item.getSpareItem().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice().doubleValue())
                        .totalPrice(item.getTotalPrice().doubleValue())
                        .build()
                )
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getCustomerId())
                .orderDate(order.getCreatedAt())
                .orderStatus(order.getStatus().name()) // enum → String
                .totalAmount(order.getTotalAmount().doubleValue())
                .items(items)
                .build();

    }
}
