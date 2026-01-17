package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.CartRequestDto;
import com.spareparts.spareparts_backend.dto.CartResponseDto;
import com.spareparts.spareparts_backend.dto.UpdateCartItemDto;
import com.spareparts.spareparts_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@CrossOrigin
public class CartController {
    private final CartService cartService;

    // ================= ADD TO CART =================
    @PostMapping("/{customerId}/add")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<Map<String, Object>> addToCart(
            @PathVariable Integer customerId,
            @RequestBody CartRequestDto requestDto
    ) {
        cartService.addToCart(customerId, requestDto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Item added to cart successfully"
        ));
    }

    // ================= UPDATE CART ITEM =================
    @PutMapping("/{customerId}/update/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable Integer customerId,
            @PathVariable Integer cartItemId,
            @RequestBody UpdateCartItemDto dto
    ) {
        cartService.updateCartItem(customerId, cartItemId, dto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart item updated"
        ));
    }

    // ================= REMOVE CART ITEM =================
    @DeleteMapping("/{customerId}/remove/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @PathVariable Integer customerId,
            @PathVariable Integer cartItemId
    ) {
        cartService.removeFromCart(customerId, cartItemId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart item removed"
        ));
    }

    // ================= VIEW CART =================
    @GetMapping("/{customerId}/view")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<List<CartResponseDto>> viewCart(
            @PathVariable Integer customerId
    ) {
        List<CartResponseDto> cart = cartService.viewCart(customerId);
        return ResponseEntity.ok(cart);
    }

    // ================= CLEAR CART =================
    @DeleteMapping("/{customerId}/clear")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<Map<String, Object>> clearCart(
            @PathVariable Integer customerId
    ) {
        cartService.clearCart(customerId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart cleared"
        ));
    }
}
