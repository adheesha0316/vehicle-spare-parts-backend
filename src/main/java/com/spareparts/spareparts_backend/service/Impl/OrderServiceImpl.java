package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.OrderItemDto;
import com.spareparts.spareparts_backend.dto.OrderResponseDto;
import com.spareparts.spareparts_backend.entity.*;
import com.spareparts.spareparts_backend.enums.*;
import com.spareparts.spareparts_backend.exception.BadRequestException;
import com.spareparts.spareparts_backend.exception.OrderCancellationNotAllowedException;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.*;
import com.spareparts.spareparts_backend.service.OrderService;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepo userRepo;
    private final SpareItemRepo spareItemRepo;
    private final CartRepo cartRepo;
    private final CustomerRepo customerRepo;

    // ================= PLACE ORDER =================

    @Override
    @Transactional
    public OrderResponseDto placeOrder(Integer customerId) {
        // 1. Get and Validate Customer
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer account is not active");
        }

        // 2. Get and Validate Cart
        Cart cart = cartRepo.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        // 3. Create Order Instance
        // Fix: Use 'orderItems' to match your Entity field name
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .statusUpdates(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. Process Items, Validate Stock, and Reduce Inventory
        for (CartItem cartItem : cart.getItems()) {
            SpareItem spareItem = cartItem.getSpareItem();

            // --- CRITICAL CHECK ---
            if (spareItem.isDeleted() ||
                    spareItem.getStatus() != SpareItemStatus.APPROVED ||
                    spareItem.getQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Item '" + spareItem.getName() + "' is no longer available in the requested quantity.");
            }

            // --- REDUCE STOCK ---
            spareItem.setQuantity(spareItem.getQuantity() - cartItem.getQuantity());
            spareItemRepo.save(spareItem); // Inventory එක update කිරීම

            // Calculate Totals
            BigDecimal unitPrice = BigDecimal.valueOf(spareItem.getPrice());
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .spareItem(spareItem)
                    .spareItemName(spareItem.getName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        // 5. Add Initial Status History
        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(OrderStatus.PENDING_PAYMENT.name())
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // 6. Save Everything and Clear Cart
        Order savedOrder = orderRepo.save(order);

        cart.getItems().clear();
        cartRepo.save(cart);

        return mapToResponse(savedOrder);
    }

    // ================= CUSTOMER =================

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrderHistory(Integer customerId) {
        return orderRepo.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId)
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
        for (OrderItem item : order.getOrderItems()) {
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
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // 1. Get current logged-in user
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Security Check
        if (currentUser.getRole() == Role.PARTNER) {
            boolean ownsProductInOrder = order.getOrderItems().stream()
                    .anyMatch(item -> item.getSpareItem().getPartner().getUser().getEmail().equals(currentUserEmail));
            if (!ownsProductInOrder) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update this order status.");
            }
        }

        // 3. Update Status
        order.setStatus(status);

        // 4. Audit Log (History)
        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(status.name())
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        orderRepo.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void assignCourierToOrder(Integer orderId, Integer courierId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Set the Courier and move to the logistics phase
        order.setCourierId(courierId);

        // Using the status defined in your Enum
        order.setStatus(OrderStatus.COURIER_ASSIGNED);

        order.getStatusUpdates().add(
                OrderStatusUpdate.builder()
                        .order(order)
                        .status(OrderStatus.COURIER_ASSIGNED.name())
                        .timestamp(LocalDateTime.now())
                        .reason("Assigned to Courier ID: " + courierId)
                        .build()
        );

        orderRepo.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getPlatformOrders() {
        return orderRepo.findPlatformOrders().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getPartnerOrders(Integer partnerId) {
        return orderRepo.findByPartnerId(partnerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= INTERNAL =================

    @Override
    public boolean canCancelOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Only PENDING or CONFIRMED orders can be cancelled
        return order.getStatus() == OrderStatus.PENDING_PAYMENT || order.getStatus() == OrderStatus.CONFIRMED;
    }

    // ================= MAPPER =================

    // ================= MAPPER FIX =================
    private OrderResponseDto mapToResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getOrderItems().stream() // Changed to 'getOrderItems()'
                .map(item -> OrderItemDto.builder()
                        .spareItemId(item.getSpareItem().getSpareItemId())
                        .spareItemName(item.getSpareItemName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build()
                )
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getCustomerId())
                .customerName(order.getCustomer().getUser().getUsername())
                .orderDate(order.getCreatedAt())
                .orderStatus(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(itemDtos)
                .build();
    }
}
