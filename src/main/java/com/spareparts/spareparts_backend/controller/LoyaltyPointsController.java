package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.LoyaltyTransactionResponseDto;
import com.spareparts.spareparts_backend.service.LoyaltyPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
@CrossOrigin
public class LoyaltyPointsController {

    private final LoyaltyPointsService loyaltyPointsService;

    @GetMapping("/balance/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<Integer> getBalance(@PathVariable Integer customerId) {
        return ResponseEntity.ok(loyaltyPointsService.getPoints(customerId));
    }

    @GetMapping("/history/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<List<LoyaltyTransactionResponseDto>> getHistory(@PathVariable Integer customerId) {
        return ResponseEntity.ok(loyaltyPointsService.getPointHistory(customerId));
    }

    @PostMapping("/admin/earn")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> earnPoints(
            @RequestParam Integer customerId,
            @RequestParam int points,
            @RequestParam String reason) {
        loyaltyPointsService.earnPoints(customerId, points, reason);
        return ResponseEntity.ok("Points added successfully");
    }

    @PostMapping("/admin/redeem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> redeemPoints(
            @RequestParam Integer customerId,
            @RequestParam int points,
            @RequestParam String reason) {
        loyaltyPointsService.redeemPoints(customerId, points, reason);
        return ResponseEntity.ok("Points redeemed successfully");
    }
}
