package com.spareparts.spareparts_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.*;
import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import com.spareparts.spareparts_backend.entity.PartnerSignedAgreement;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/partner")
@RequiredArgsConstructor
@CrossOrigin
public class PartnerController {

    private final PartnerService partnerService;
    private final ObjectMapper mapper;

    // ================= PARTNER PROFILE =================

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'PARTNER')")
    public ResponseEntity<PartnerResponseDto> createPartnerProfile(
            @RequestParam Integer userId,
            @RequestPart("partner") String partnerJson,
            @RequestPart MultipartFile nicFront,
            @RequestPart MultipartFile nicBack,
            @RequestPart(required = false) MultipartFile profileImage
    ) throws Exception {

        PartnerRequestDto dto =
                mapper.readValue(partnerJson, PartnerRequestDto.class);

        return ResponseEntity.ok(
                partnerService.createPartnerProfile(
                        userId, dto, nicFront, nicBack, profileImage
                )
        );
    }


    @PutMapping(value = "/{partnerId}/update",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<PartnerResponseDto> requestProfileUpdate(
            @PathVariable Integer partnerId,
            @RequestPart("partner") String partnerJson,
            @RequestPart(required = false) MultipartFile nicFront,
            @RequestPart(required = false) MultipartFile nicBack,
            @RequestPart(required = false) MultipartFile profileImage
    ) throws Exception {

        PartnerRequestDto dto =
                mapper.readValue(partnerJson, PartnerRequestDto.class);

        return ResponseEntity.ok(
                partnerService.requestProfileUpdate(
                        partnerId, dto, nicFront, nicBack, profileImage
                )
        );
    }

    @PutMapping("/request-delete/{partnerId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<PartnerResponseDto> requestProfileDelete(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.requestProfileDelete(partnerId)
        );
    }


    // ================= APPROVE PARTNER =================
    @PutMapping("/{partnerId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerResponseDto> approvePartner(
            @PathVariable Integer partnerId) {
        PartnerResponseDto response = partnerService.approvePartnerProfile(partnerId);
        return ResponseEntity.ok(response);
    }

    // ================= REJECT PARTNER =================
    @PutMapping("/{partnerId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerResponseDto> rejectPartner(
            @PathVariable Integer partnerId,
            @RequestParam String reason) {
        PartnerResponseDto response = partnerService.rejectPartnerProfile(partnerId, reason);
        return ResponseEntity.ok(response);
    }


    // ================= ADMIN ACTIONS =================
    @DeleteMapping("/admin/delete/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePartnerByAdmin(@PathVariable Integer partnerId) {
        partnerService.deletePartnerByAdmin(partnerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/restore/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restorePartnerByAdmin(@PathVariable Integer partnerId) {
        partnerService.restorePartnerByAdmin(partnerId);
        return ResponseEntity.ok().build();
    }

    // ================= GET PARTNER =================

    @GetMapping("/user/get/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PartnerResponseDto> getPartnerByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(
                partnerService.getPartnerByUserId(userId)
        );
    }

    @GetMapping("/get/{partnerId}")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public ResponseEntity<PartnerResponseDto> getPartnerById(@PathVariable Integer partnerId) {
        return ResponseEntity.ok(
                partnerService.getPartnerById(partnerId)
        );
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PartnerResponseDto>> getAllPartners() {
        return ResponseEntity.ok(
                partnerService.getAllPartners()
        );
    }

    // ================= PARTNER AGREEMENT =================
    @GetMapping("/agreement/download/{partnerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public ResponseEntity<?> downloadAgreement(@PathVariable Integer partnerId) {
        try {
            byte[] file = partnerService.downloadAgreement(partnerId);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=partner-agreement-" + partnerId + ".pdf"
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Failed to download agreement",
                            "error", e.getMessage()
                    ));
        }
    }


    // ================= ACCEPT PARTNER AGREEMENT =================
    @PostMapping(
            value = "/agreement/accept/{partnerId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<PartnerResponseDto> acceptAgreement(
            @PathVariable Integer partnerId,
            @RequestPart("signedAgreement") MultipartFile signedAgreement
    ) {
        try {
            PartnerResponseDto partnerDto = partnerService.acceptAgreement(partnerId, signedAgreement);
            return ResponseEntity.ok(partnerDto);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * ADMIN & MANAGER ONLY
     */
    @GetMapping("/{partnerId}/signed-agreements")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<PartnerSignedAgreementDto>> getSignedAgreementsByPartner(
            @PathVariable Integer partnerId
    ) {
        List<PartnerSignedAgreementDto> dtos = partnerService
                .getSignedAgreementsByPartner(partnerId) // returns List<PartnerSignedAgreement>
                .stream()
                .map(partnerService::mapToDto) // convert each entity to DTO
                .toList();

        return ResponseEntity.ok(dtos);
    }


    /**
     * ADMIN & MANAGER ONLY
     */
    @GetMapping("/{partnerId}/signed-agreements/latest")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PartnerSignedAgreement> getLatestSignedAgreementByPartner(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getLatestSignedAgreementByPartner(partnerId)
        );
    }



    // ================= ADMIN AGREEMENT =================

    @PostMapping("/agreement/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateAgreement(
            @RequestParam String conditions,
            @RequestParam String version
    ) {
        try {
            PartnerAgreement agreement =
                    partnerService.generateAgreementPdf(
                            "Common Partner",   // placeholder partner name
                            "Company Name",     // placeholder company name
                            conditions,
                            version,
                            true
                    );

            return ResponseEntity.ok(agreement);

        } catch (RuntimeException e) {

            // Version duplicate case
            if (e.getMessage().contains("version")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message", e.getMessage()
                        ));
            }

            // Other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Failed to generate agreement",
                            "error", e.getMessage()
                    ));
        }
    }


    // Get the current agreement's conditions
    @GetMapping("/agreement/current/conditions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public ResponseEntity<?> getCurrentConditions() {
        try {
            String conditions = partnerService.getCurrentAgreementConditions();
            return ResponseEntity.ok(Map.of(
                    "conditions", conditions
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message", e.getMessage()
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "message", "Failed to read agreement PDF",
                            "error", e.getMessage()
                    ));
        }
    }


    @PostMapping(value = "/admin/agreement/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerAgreement> uploadAgreement(
            @RequestPart MultipartFile agreementFile,
            @RequestParam String version
    ) {
        return ResponseEntity.ok(
                partnerService.uploadAgreement(agreementFile, version)
        );
    }

    @DeleteMapping("/admin/agreement/{agreementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeAgreement(@PathVariable Integer agreementId) {
        partnerService.removeAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/admin/agreement/upload-new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerAgreement> uploadNewAgreementVersion(
            @RequestPart MultipartFile agreementFile,
            @RequestParam String version
    ) {
        return ResponseEntity.ok(
                partnerService.uploadNewAgreementVersion(agreementFile, version)
        );
    }

    // ================= ADMIN / PARTNER AGREEMENT =================

    @GetMapping("/agreement/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public ResponseEntity<PartnerAgreement> getLatestAgreement() {
        try {
            PartnerAgreement latestAgreement = partnerService.getLatestAgreement();
            return ResponseEntity.ok(latestAgreement);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(null);
        }
    }


    // ================= SPARE ITEM =================

    @PostMapping(value = "/{partnerId}/spare-items",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<SpareItemResponseDto> createSpareItemRequest(
            @PathVariable Integer partnerId,
            @RequestPart("spareItem") String spareItemJson,
            @RequestPart(required = false) List<MultipartFile> images
    ) throws Exception {

        SpareItemRequestDto dto =
                mapper.readValue(spareItemJson, SpareItemRequestDto.class);

        return ResponseEntity.ok(
                partnerService.createSpareItemRequest(
                        partnerId, dto, images
                )
        );
    }

    @PutMapping(value = "/{partnerId}/spare-items/{spareItemId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<SpareItemResponseDto> RequestSpareItemUpdate(
            @PathVariable Integer partnerId,
            @PathVariable Integer spareItemId,
            @RequestPart("spareItem") String spareItemJson,
            @RequestPart(required = false) List<MultipartFile> images
    ) throws Exception {

        SpareItemRequestDto dto =
                mapper.readValue(spareItemJson, SpareItemRequestDto.class);

        return ResponseEntity.ok(
                partnerService.requestSpareItemUpdate(
                        partnerId, spareItemId, dto, images
                )
        );
    }


    @DeleteMapping("/{partnerId}/spare-items/{spareItemId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<SpareItemResponseDto> requestSpareItemDelete(
            @PathVariable Integer partnerId,
            @PathVariable Integer spareItemId
    ) {
        return ResponseEntity.ok(
                partnerService.requestSpareItemDelete(partnerId, spareItemId)
        );
    }

    // ================= APPROVAL =================

    @PutMapping("/spare-items/{spareItemId}/approve-reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SpareItemResponseDto> approveOrRejectSpareItem(
            @PathVariable Integer spareItemId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String rejectionReason,
            @RequestParam Integer approverId
    ) {
        return ResponseEntity.ok(
                partnerService.approveOrRejectSpareItem(spareItemId, approve, rejectionReason, approverId)
        );
    }

    // ================= SPARE ITEM LISTS =================

    @GetMapping("/{partnerId}/spare-items/approved")
    @PreAuthorize("hasAnyRole('PARTNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<SpareItemResponseDto>> getApprovedSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getApprovedSpareItemsByPartner(partnerId)
        );
    }

    @GetMapping("/{partnerId}/spare-items/rejected")
    @PreAuthorize("hasAnyRole('PARTNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<SpareItemResponseDto>> getRejectedSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getRejectedSpareItemsByPartner(partnerId)
        );
    }

    @GetMapping("/{partnerId}/spare-items/pending")
    @PreAuthorize("hasAnyRole('PARTNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<SpareItemResponseDto>> getPendingSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getPendingSpareItemsByPartner(partnerId)
        );
    }

    // ================= DELETE OLD AGREEMENTS =================
    @DeleteMapping("/agreement/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupOldAgreements() {
        partnerService.removeAllOldAgreements();
        return ResponseEntity.ok(
                Map.of("message", "Old agreement versions removed successfully")
        );
    }


}
