package com.spareparts.spareparts_backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.CustomerRequestDto;
import com.spareparts.spareparts_backend.dto.CustomerResponseDto;
import com.spareparts.spareparts_backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@CrossOrigin
public class CustomerController {
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    // --- CREATE PROFILE ---
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @RequestPart("customer") String customerJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws IOException {

        CustomerRequestDto dto = objectMapper.readValue(customerJson, CustomerRequestDto.class);

        return ResponseEntity.ok(customerService.createCustomer(dto, profileImage));
    }

    // --- UPDATE PROFILE ---
    @PutMapping(value = "/update/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<CustomerResponseDto> updateCustomerProfile(
            @PathVariable Integer customerId,
            @RequestPart("customer") String customerJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws IOException {

        CustomerRequestDto requestDto = objectMapper.readValue(customerJson, CustomerRequestDto.class);
        return ResponseEntity.ok(customerService.updateCustomerProfile(customerId, requestDto, profileImage));
    }

    // --- GET PROFILE ---
    @GetMapping("/get/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<CustomerResponseDto> getCustomerProfile(@PathVariable Integer customerId) {
        return ResponseEntity.ok(customerService.getCustomerProfile(customerId));
    }

    // --- DELETE REQUEST ---
    @PostMapping("/{customerId}/delete-request")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<Map<String, Object>> requestDeleteCustomer(@PathVariable Integer customerId) {
        customerService.requestDeleteCustomer(customerId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Delete request submitted successfully",
                "customerId", customerId
        ));
    }
}
