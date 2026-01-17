package com.spareparts.spareparts_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.*;
import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.service.OrderService;
import com.spareparts.spareparts_backend.service.PartnerService;
import com.spareparts.spareparts_backend.service.SpareItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SpareItemService spareItemService;
    private final OrderService orderService;
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
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
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
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
    public ResponseEntity<PartnerResponseDto> requestProfileDelete(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.requestProfileDelete(partnerId)
        );
    }

    // ================= PARTNER AGREEMENT =================
    @GetMapping("/agreement/download/{partnerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId))")
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
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
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

    // ================= ADMIN / PARTNER AGREEMENT =================

    @GetMapping("/agreement/latest")
    @PreAuthorize("hasRole('PARTNER')")
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
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
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
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
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
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
    public ResponseEntity<SpareItemResponseDto> requestSpareItemDelete(
            @PathVariable Integer partnerId,
            @PathVariable Integer spareItemId
    ) {
        return ResponseEntity.ok(
                partnerService.requestSpareItemDelete(partnerId, spareItemId)
        );
    }

    // ================= SPARE ITEM LISTS =================

    @GetMapping("/{partnerId}/spare-items/approved")
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
    public ResponseEntity<List<SpareItemResponseDto>> getApprovedSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getApprovedSpareItemsByPartner(partnerId)
        );
    }

    @GetMapping("/{partnerId}/spare-items/rejected")
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
    public ResponseEntity<List<SpareItemResponseDto>> getRejectedSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getRejectedSpareItemsByPartner(partnerId)
        );
    }

    @GetMapping("/{partnerId}/spare-items/pending")
    @PreAuthorize("hasRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
    public ResponseEntity<List<SpareItemResponseDto>> getPendingSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getPendingSpareItemsByPartner(partnerId)
        );
    }

    @GetMapping("/my-spare-items/{partnerId}")
    @PreAuthorize("hasAnyRole('PARTNER') and @partnerSecurity.isOwner(#partnerId)")
    public ResponseEntity<List<SpareItemResponseDto>> getPartnerItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                spareItemService.findPartnerItems(partnerId)
        );
    }

    //============== Order Controllers ==============
    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> updateOrderStatusByPartner(
            @PathVariable Integer orderId,
            @RequestParam OrderStatus status) {

        orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order status updated by Partner"
        ));
    }
}
