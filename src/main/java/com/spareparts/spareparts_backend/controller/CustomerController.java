package com.spareparts.spareparts_backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.CustomerRequestDto;
import com.spareparts.spareparts_backend.dto.CustomerResponseDto;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.exception.BadRequestException;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.service.CustomerService;
import com.spareparts.spareparts_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@CrossOrigin
public class CustomerController {
    private final CustomerService customerService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()") // allow any logged-in user
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @RequestPart("customer") String customerJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            // Parse JSON string to DTO
            CustomerRequestDto dto = objectMapper.readValue(customerJson, CustomerRequestDto.class);

            // Get current logged-in user
            User currentUser = userService.getCurrentUserEntity();

            if (customerService.existsByUserId(currentUser.getUserId())) {
                throw new BadRequestException("Customer profile already exists");
            }

            // Create customer profile
            CustomerResponseDto createdCustomer = customerService.createCustomer(dto, profileImage);

            // Upgrade user role to CUSTOMER if not already
            if (currentUser.getRole() != Role.CUSTOMER) {
                currentUser.setRole(Role.CUSTOMER);
                userService.saveUser(currentUser);
            }

            return ResponseEntity.ok(createdCustomer);

        } catch (IOException e) {
            throw new BadRequestException("Invalid JSON format for customer");
        }
    }




    // ================= UPDATE CUSTOMER PROFILE =================
    @PutMapping(
            value = "/update/{customerId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("""
    hasRole('ADMIN')
    or (hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId))
""")
    public ResponseEntity<CustomerResponseDto> updateCustomerProfile(
            @PathVariable Integer customerId,
            @RequestPart("customer") String customerJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            // Parse JSON to DTO
            CustomerRequestDto requestDto;
            try {
                requestDto = objectMapper.readValue(customerJson, CustomerRequestDto.class);
            } catch (IOException e) {
                throw new BadRequestException("Invalid JSON format for customer");
            }
            // Update customer profile
            CustomerResponseDto updatedCustomer = customerService.updateCustomerProfile(
                    customerId,
                    requestDto,
                    profileImage
            );

            // Return updated customer response
            return ResponseEntity.ok(updatedCustomer);

        } catch (ResourceNotFoundException e) {
            throw e; // Let GlobalExceptionHandler handle NOT_FOUND
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage()); // Convert to clean 400 JSON
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }


    // ================= GET CUSTOMER PROFILE =================
    @GetMapping("/get/{customerId}")
    @PreAuthorize("""
        hasRole('ADMIN') or (hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId))
    """)
    public ResponseEntity<CustomerResponseDto> getCustomerProfile(
            @PathVariable Integer customerId
    ) {
        try {
            CustomerResponseDto customer = customerService.getCustomerProfile(customerId);
            return ResponseEntity.ok(customer);

        } catch (ResourceNotFoundException e) {
            throw e; // handled by GlobalExceptionHandler
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }



    // ================= ADMIN : GET ALL =================
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // ================= DELETE FLOW =================

    // Customer requests deletion
    @PostMapping("/{customerId}/delete-request")
    @PreAuthorize("hasRole('CUSTOMER') and @customerSecurity.isOwner(#customerId)")
    public ResponseEntity<Map<String, Object>> requestDeleteCustomer(
            @PathVariable Integer customerId
    ) {
        customerService.requestDeleteCustomer(customerId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Delete request submitted successfully",
                "customerId", customerId
        ));
    }

    // Admin approves deletion
    @PostMapping("/{customerId}/delete-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveDeleteCustomer(
            @PathVariable Integer customerId
    ) {
        customerService.approveDeleteCustomer(customerId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Delete request approved",
                "customerId", customerId
        ));
    }

    // Admin rejects deletion
    @PostMapping("/{customerId}/delete-reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectDeleteCustomer(
            @PathVariable Integer customerId,
            @RequestBody(required = false) Map<String, String> request
    ) {
        // Safe defaulting
        String reason = request != null && request.get("reason") != null
                ? request.get("reason")
                : "Rejected by admin";

        customerService.rejectDeleteCustomer(customerId, reason);

        // Map.of is now SAFE because no null values
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Delete request rejected",
                "customerId", customerId,
                "reason", reason
        ));
    }




}
