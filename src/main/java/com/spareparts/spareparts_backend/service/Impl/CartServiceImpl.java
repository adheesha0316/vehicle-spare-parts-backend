package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.CartItemDto;
import com.spareparts.spareparts_backend.dto.CartRequestDto;
import com.spareparts.spareparts_backend.dto.CartResponseDto;
import com.spareparts.spareparts_backend.dto.UpdateCartItemDto;
import com.spareparts.spareparts_backend.entity.Cart;
import com.spareparts.spareparts_backend.entity.CartItem;
import com.spareparts.spareparts_backend.entity.Customer;
import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.enums.CustomerStatus;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.CartItemRepo;
import com.spareparts.spareparts_backend.repo.CartRepo;
import com.spareparts.spareparts_backend.repo.CustomerRepo;
import com.spareparts.spareparts_backend.repo.SpareItemRepo;
import com.spareparts.spareparts_backend.service.CartService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final CustomerRepo customerRepo;
    private final SpareItemRepo spareItemRepo;


    // ================= ADD TO CART =================

    @Override
    @Transactional
    public void addToCart(Integer customerId, CartRequestDto requestDto) {
        // ------------------- VALIDATE CUSTOMER -------------------
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "You don't have a customer account. Please create your customer profile first."
                        )
                );

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer account is not active");
        }

        // ------------------- GET OR CREATE CART -------------------
        Cart cart = cartRepo.findByCustomer(customer)
                .orElseGet(() -> cartRepo.save(
                        Cart.builder()
                                .customer(customer)
                                .items(new ArrayList<>())
                                .build()
                ));

        // ------------------- VALIDATE SPARE ITEM -------------------
        SpareItem spareItem = spareItemRepo.findById(requestDto.getSpareItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Spare item not found"));

        // ------------------- FIND EXISTING CART ITEM -------------------
        CartItem cartItem = cartItemRepo.findByCartAndSpareItem(cart, spareItem)
                .orElse(null);

        // ------------------- ADD OR UPDATE -------------------
        if (cartItem != null) {
            // Item already in cart → increase quantity
            cartItem.setQuantity(cartItem.getQuantity() + requestDto.getQuantity());
        } else {
            // New cart item → create
            cartItemRepo.save(
                    CartItem.builder()
                            .cart(cart)
                            .spareItem(spareItem)
                            .quantity(requestDto.getQuantity())
                            .build()
            );
        }
    }

    // ================= UPDATE CART ITEM =================

    @Override
    public void updateCartItem(Integer customerId, Integer cartItemId, UpdateCartItemDto dto) {
        CartItem cartItem = cartItemRepo.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateOwnership(customerId, cartItem);

        // Update spare item if provided
        if (dto.getSpareItemId() != null && !dto.getSpareItemId().equals(cartItem.getSpareItem().getSpareItemId())) {
            SpareItem newSpareItem = spareItemRepo.findById(dto.getSpareItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Spare item not found"));
            cartItem.setSpareItem(newSpareItem);
        }

        // Update quantity
        if (dto.getQuantity() <= 0) {
            cartItemRepo.delete(cartItem);
        } else {
            cartItem.setQuantity(dto.getQuantity());
        }
    }

    // ================= REMOVE FROM CART =================

    @Override
    public void removeFromCart(Integer customerId, Integer cartItemId) {
        CartItem cartItem = cartItemRepo.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateOwnership(customerId, cartItem);
        cartItemRepo.delete(cartItem);
    }

    // ================= VIEW CART =================

    @Override
    @Transactional(readOnly = true)
    public List<CartResponseDto> viewCart(Integer customerId) {
        Cart cart = cartRepo.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItemDto> items = cart.getItems().stream()
                .map(item -> {
                    BigDecimal unitPrice = BigDecimal.valueOf(item.getSpareItem().getPrice());
                    BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                    return CartItemDto.builder()
                            .cartItemId(item.getId())
                            .spareItemId(item.getSpareItem().getSpareItemId())
                            .spareItemName(item.getSpareItem().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(unitPrice)
                            .subTotal(subTotal)
                            .build();
                })
                .collect(Collectors.toList());

        return List.of(
                CartResponseDto.builder()
                        .cartId(cart.getCartId())
                        .items(items)
                        .build()
        );
    }

    // ================= CLEAR CART =================

    @Override
    public void clearCart(Integer customerId) {
        Cart cart = cartRepo.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepo.deleteByCart(cart);
    }

    // ================= CHECK EMPTY =================

    @Override
    @Transactional(readOnly = true)
    public boolean isCartEmpty(Integer customerId) {
        return cartRepo.findByCustomerCustomerId(customerId)
                .map(cart -> cart.getItems().isEmpty())
                .orElse(true);
    }

    // ================= SECURITY =================
    private void validateOwnership(Integer customerId, CartItem cartItem) {
        if (!cartItem.getCart().getCustomer().getCustomerId().equals(customerId)) {
            throw new SecurityException("Unauthorized cart access");
        }
    }
}
