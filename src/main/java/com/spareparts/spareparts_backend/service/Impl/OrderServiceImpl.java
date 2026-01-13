package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.OrderItemDto;
import com.spareparts.spareparts_backend.dto.OrderResponseDto;
import com.spareparts.spareparts_backend.entity.*;
import com.spareparts.spareparts_backend.enums.CustomerStatus;
import com.spareparts.spareparts_backend.enums.OrderStatus;
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
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer account is not active");
        }

        Cart cart = cartRepo.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));


        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        // ---------------- CREATE ORDER ----------------
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();


        order = orderRepo.save(order);

        // ---------------- CREATE ORDER ITEMS ----------------
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            BigDecimal unitPrice = BigDecimal.valueOf(cartItem.getSpareItem().getPrice());
            BigDecimal itemTotal =
                    unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .spareItem(cartItem.getSpareItem())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            orderItemRepo.save(orderItem);
        }

        order.setTotalAmount(totalAmount);

        // ---------------- STATUS HISTORY ----------------
        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(OrderStatus.PENDING.name())
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // ---------------- CLEAR CART ----------------
        cart.getItems().clear();

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
    public void cancelOrder(Integer customerId, Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getCustomerId().equals(customerId)) {
            throw new SecurityException("Unauthorized order access");
        }

        if (!canCancelOrder(orderId)) {
            throw new IllegalStateException("Order cannot be cancelled at this stage");
        }

        order.setStatus(OrderStatus.CANCELLED);

        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(OrderStatus.CANCELLED.name())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
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

        return order.getStatus() == OrderStatus.PENDING;
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
