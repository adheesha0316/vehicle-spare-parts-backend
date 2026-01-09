package com.spareparts.spareparts_backend.controller;


import com.spareparts.spareparts_backend.dto.CustomerRequestDto;
import com.spareparts.spareparts_backend.dto.CustomerResponseDto;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@CrossOrigin
public class CustomerController {
    private final CustomerService customerService;

    // ================= CREATE CUSTOMER =================
// CUSTOMER self-registration
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")  // Only logged-in users with CUSTOMER role
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @RequestPart("data") CustomerRequestDto dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            CustomerResponseDto createdCustomer = customerService.createCustomer(dto, profileImage);
            return ResponseEntity.ok(createdCustomer);

        } catch (IOException e) {
            // File storage problem
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (IllegalStateException e) {
            // Already exists
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }



    // ================= UPDATE CUSTOMER PROFILE =================
    @PutMapping(value = "/update/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> updateCustomerProfile(
            @PathVariable Integer customerId,
            @RequestPart("data") CustomerRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            // Call service method to update profile (handles optional profile image)
            CustomerResponseDto updatedCustomer =
                    customerService.updateCustomerProfile(customerId, requestDto, profileImage);

            return ResponseEntity.ok(updatedCustomer);

        } catch (IOException e) {
            // File storage or reading issue
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

        } catch (ResourceNotFoundException e) {
            // Customer not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (IllegalStateException e) {
            // Customer inactive or validation failed
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }



    // ================= GET PROFILE =================
    @GetMapping("/get/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<CustomerResponseDto> getCustomerProfile(
            @PathVariable Integer customerId
    ) {
        return ResponseEntity.ok(
                customerService.getCustomerProfile(customerId)
        );
    }

    // ================= ADMIN : GET ALL =================
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // ================= DELETE FLOW =================
    // CUSTOMER requests delete
    @PostMapping("/{customerId}/delete-request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> requestDeleteCustomer(
            @PathVariable Integer customerId
    ) {
        customerService.requestDeleteCustomer(customerId);
        return ResponseEntity.ok().build();
    }

    // ADMIN approves delete
    @PostMapping("/{customerId}/delete-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveDeleteCustomer(
            @PathVariable Integer customerId
    ) {
        customerService.approveDeleteCustomer(customerId);
        return ResponseEntity.ok().build();
    }

    // ADMIN rejects delete
    @PostMapping("/{customerId}/delete-reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectDeleteCustomer(
            @PathVariable Integer customerId,
            @RequestParam String reason
    ) {
        customerService.rejectDeleteCustomer(customerId, reason);
        return ResponseEntity.ok().build();
    }
}
