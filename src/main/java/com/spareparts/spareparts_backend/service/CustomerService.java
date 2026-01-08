package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.*;

import java.util.List;

public interface CustomerService {
    // ================= CUSTOMER PROFILE =================
    CustomerResponseDto createCustomer(CustomerRequestDto requestDto);

    CustomerResponseDto updateCustomerProfile(
            Integer customerId,
            CustomerRequestDto requestDto
    );

    CustomerResponseDto getCustomerProfile(Integer customerId);

    List<CustomerResponseDto> getAllCustomers(); // ADMIN

    // ================= DELETE FLOW (ADMIN APPROVAL) =================
    void requestDeleteCustomer(Integer customerId);

    void approveDeleteCustomer(Integer customerId);

    void rejectDeleteCustomer(Integer customerId, String reason);

    // ================= STATUS / VALIDATION =================
    void validateActiveCustomer(Integer customerId);


}
