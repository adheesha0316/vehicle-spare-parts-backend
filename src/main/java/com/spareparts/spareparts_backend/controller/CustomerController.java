package com.spareparts.spareparts_backend.controller;


import com.spareparts.spareparts_backend.dto.CustomerRequestDto;
import com.spareparts.spareparts_backend.dto.CustomerResponseDto;
import com.spareparts.spareparts_backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@CrossOrigin
public class CustomerController {
    private final CustomerService customerService;

    // ================= CREATE CUSTOMER =================
    // CUSTOMER (self registration)
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @RequestBody CustomerRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.createCustomer(requestDto));
    }

    // ================= UPDATE PROFILE =================
    @PutMapping("/update/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> updateCustomerProfile(
            @PathVariable Integer customerId,
            @RequestBody CustomerRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                customerService.updateCustomerProfile(customerId, requestDto)
        );
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
