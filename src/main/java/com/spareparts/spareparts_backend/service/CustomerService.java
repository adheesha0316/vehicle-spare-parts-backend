package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.*;

import java.util.List;

public interface CustomerService {
    // ===================== CUSTOMER PROFILE =====================
    CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto);  // self registration
    CustomerResponseDto updateCustomerProfile(Integer customerId, CustomerRequestDto customerRequestDto);

    // Delete requires admin approval
    void requestDeleteCustomer(Integer customerId);
    void approveDeleteCustomer(Integer customerId);
    void rejectDeleteCustomer(Integer customerId, String reason);

    CustomerResponseDto getCustomerProfile(Integer customerId);
    List<CustomerResponseDto> getAllCustomers();  // Admin use

    // ===================== CART OPERATIONS =====================
    void addToCart(Integer customerId, CartRequestDto cartRequestDto);
    void removeFromCart(Integer customerId, Integer cartItemId);
    List<CartRequestDto> viewCart(Integer customerId);

    // ===================== ORDERS =====================
    OrderResponseDto placeOrder(Integer customerId);  // checkout all cart items
    List<OrderResponseDto> getOrderHistory(Integer customerId);
    OrderResponseDto getOrderDetails(Integer orderId);
    void cancelOrder(Integer customerId, Integer orderId);  // if allowed
    void updateOrderStatus(Integer orderId, String status);  // Admin/Partner only
    List<OrderResponseDto> getAllOrders();  // Admin view

    // ===================== REVIEWS & COMMENTS =====================
    ReviewResponseDto addReview(Integer customerId, ReviewRequestDto reviewRequestDto);
    List<ReviewResponseDto> getCustomerReviews(Integer customerId);
    void deleteReview(Integer customerId, Integer reviewId);  // optional, admin override

    // ===================== WALLET & LOYALTY =====================
    double getWalletBalance(Integer customerId);
    void addWalletBalance(Integer customerId, double amount);
    void redeemWalletBalance(Integer customerId, double amount);

    int getLoyaltyPoints(Integer customerId);
    void addLoyaltyPoints(Integer customerId, int points);
    void redeemLoyaltyPoints(Integer customerId, int points);
}
