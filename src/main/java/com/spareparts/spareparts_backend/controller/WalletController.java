package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.WalletTransactionResponseDto;
import com.spareparts.spareparts_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@CrossOrigin
public class WalletController {
    private final WalletService walletService;

    // View current balance
    @GetMapping("/balance/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<Double> getBalance(@PathVariable Integer customerId) {
        return ResponseEntity.ok(walletService.getBalance(customerId));
    }

    // View transaction history
    @GetMapping("/history/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<List<WalletTransactionResponseDto>> getHistory(@PathVariable Integer customerId) {
        return ResponseEntity.ok(walletService.getTransactionHistory(customerId));
    }
}
