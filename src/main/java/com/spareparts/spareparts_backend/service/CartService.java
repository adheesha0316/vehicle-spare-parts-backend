package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.CartRequestDto;
import com.spareparts.spareparts_backend.dto.CartResponseDto;

import java.util.List;

public interface CartService {
    void addToCart(
            Integer customerId,
            CartRequestDto requestDto
    );

    void updateCartItem(
            Integer customerId,
            Integer cartItemId,
            int quantity
    );

    void removeFromCart(
            Integer customerId,
            Integer cartItemId
    );

    List<CartResponseDto> viewCart(Integer customerId);

    void clearCart(Integer customerId);

    boolean isCartEmpty(Integer customerId);
}
