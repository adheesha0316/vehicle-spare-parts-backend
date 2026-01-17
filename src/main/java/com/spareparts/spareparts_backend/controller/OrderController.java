package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.CancelOrderRequestDto;
import com.spareparts.spareparts_backend.dto.OrderResponseDto;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    // ================= PLACE ORDER =================
    @PostMapping("/{customerId}/place")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<OrderResponseDto> placeOrder(@PathVariable Integer customerId) {
        return ResponseEntity.ok(orderService.placeOrder(customerId));
    }

    // ================= CUSTOMER =================
    @GetMapping("/{customerId}/history")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<List<OrderResponseDto>> orderHistory(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(orderService.getOrderHistory(customerId));
    }

    @GetMapping("/details/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','MANAGER','PARTNER')")
    public ResponseEntity<OrderResponseDto> orderDetails(
            @PathVariable Integer orderId) {

        return ResponseEntity.ok(orderService.getOrderDetails(orderId));
    }

    @PutMapping("/{customerId}/cancel/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Integer customerId,
            @PathVariable Integer orderId,
            @Valid @RequestBody CancelOrderRequestDto requestDto) {

        orderService.cancelOrder(customerId, orderId, requestDto.getReason());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order cancelled successfully"
        ));
    }



    // ================= ADMIN / PARTNER =================
    @PutMapping("/status/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PARTNER')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer orderId,
            @RequestParam OrderStatus status) {

        orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order status updated"
        ));
    }


}
