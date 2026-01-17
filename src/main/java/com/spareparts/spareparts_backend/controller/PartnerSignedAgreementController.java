package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.PartnerSignedAgreementDto;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partner-signed-agreement")
@RequiredArgsConstructor
@CrossOrigin
public class PartnerSignedAgreementController {
    private final PartnerService partnerService;
    private final UserRepo userRepo;

    // ================= APPROVE SIGNED AGREEMENT =================
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PartnerSignedAgreementDto> approveSignedAgreement(
            @PathVariable("id") Integer signedAgreementId,
            Authentication authentication
    ) {
        User approver = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        var approved = partnerService.approveSignedAgreement(
                signedAgreementId,
                approver.getUserId()
        );

        return ResponseEntity.ok(partnerService.mapToDto(approved));
    }

    // ================= REJECT SIGNED AGREEMENT =================
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PartnerSignedAgreementDto> rejectSignedAgreement(
            @PathVariable("id") Integer signedAgreementId,
            @RequestParam String reason,
            Authentication authentication
    ) {
        User approver = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        var rejected = partnerService.rejectSignedAgreement(
                signedAgreementId,
                approver.getUserId(),
                reason
        );

        return ResponseEntity.ok(partnerService.mapToDto(rejected));
    }



}
