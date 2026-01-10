package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.*;
import com.spareparts.spareparts_backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CustomerService {
    // ================= CUSTOMER PROFILE =================
    CustomerResponseDto createCustomer(CustomerRequestDto requestDto, MultipartFile profileImage) throws IOException;


    CustomerResponseDto updateCustomerProfile(
            Integer customerId,
            CustomerRequestDto requestDto,
            MultipartFile profileImage
    ) throws IOException;


    CustomerResponseDto getCustomerProfile(Integer customerId);

    List<CustomerResponseDto> getAllCustomers(); // ADMIN

    // ================= DELETE FLOW (ADMIN APPROVAL) =================
    void requestDeleteCustomer(Integer customerId);

    void approveDeleteCustomer(Integer customerId);

    void rejectDeleteCustomer(Integer customerId, String reason);

    // ================= STATUS / VALIDATION =================
    void validateActiveCustomer(Integer customerId);


}
