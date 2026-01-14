package com.spareparts.spareparts_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.CategoryResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import com.spareparts.spareparts_backend.service.SpareItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spare-item")
@RequiredArgsConstructor
@CrossOrigin
public class SpareItemController {

    private final SpareItemService spareItemService;
    private final ObjectMapper mapper;


    // ---------------- CREATE ----------------
    // PLATFORM OWNER item (ADMIN / MANAGER)
    @PostMapping("/create/platform")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SpareItemResponseDto> createPlatformSpareItem(
            @RequestPart("spareItem") String spareItemJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {

        SpareItemRequestDto requestDto =
                mapper.readValue(spareItemJson, SpareItemRequestDto.class);

        return ResponseEntity.ok(
                spareItemService.createPlatformSpareItem(requestDto, images)
        );
    }

    // PARTNER item
    @PostMapping("/create/partner/{partnerId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<SpareItemResponseDto> createPartnerSpareItem(
            @PathVariable Integer partnerId,
            @RequestPart("spareItem") String spareItemJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {

        SpareItemRequestDto requestDto =
                mapper.readValue(spareItemJson, SpareItemRequestDto.class);

        return ResponseEntity.ok(
                spareItemService.createPartnerSpareItem(partnerId, requestDto, images)
        );
    }

    // ---------------- UPDATE BY MANAGER ----------------
    @PutMapping("/update/manager/{spareItemId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SpareItemResponseDto> updateByManager(
            @PathVariable Integer spareItemId,
            @RequestPart("spareItem") String spareItemJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws JsonProcessingException {

        // Parse JSON string to DTO
        SpareItemRequestDto requestDto = mapper.readValue(spareItemJson, SpareItemRequestDto.class);

        // Optional: check if the item exists / status
        SpareItemResponseDto updatedSpareItem = spareItemService.updateSpareItemByManager(spareItemId, requestDto, images);

        return ResponseEntity.ok(updatedSpareItem);
    }

    // ---------------- UPDATE BY ADMIN ----------------
    @PutMapping("/update/admin/{spareItemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpareItemResponseDto> updateByAdmin(
            @PathVariable Integer spareItemId,
            @RequestPart("spareItem") String spareItemJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws JsonProcessingException {

        // Parse JSON string into DTO
        SpareItemRequestDto requestDto = mapper.readValue(spareItemJson, SpareItemRequestDto.class);

        SpareItemResponseDto response = spareItemService.updateSpareItemByAdmin(spareItemId, requestDto, images);

        return ResponseEntity.ok(response);
    }

    // ---------------- DELETE (ADMIN) ----------------
    @DeleteMapping("/delete/{spareItemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSpareItem(@PathVariable Integer spareItemId) {
        spareItemService.deleteSpareItem(spareItemId);
        return ResponseEntity.ok("Spare item deleted successfully");
    }

    // ---------------- RESTORE (ADMIN) ----------------
    @PatchMapping("/restore/{spareItemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpareItemResponseDto> restoreSpareItem(@PathVariable Integer spareItemId) {
        SpareItemResponseDto response = spareItemService.restoreSpareItem(spareItemId);
        return ResponseEntity.ok(response);
    }

    // ---------------- APPROVE MANAGER UPDATE ----------------
    @PatchMapping("/approve/{spareItemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpareItemResponseDto> approveSpareItemUpdate(
            @PathVariable Integer spareItemId,
            @RequestParam Integer adminId
    ) {
        SpareItemResponseDto response = spareItemService.approveSpareItemUpdate(spareItemId, adminId);
        return ResponseEntity.ok(response);
    }

    // ---------------- GET BY ID ----------------
    @GetMapping("/get/{spareItemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SpareItemResponseDto> getById(@PathVariable Integer spareItemId) {
        SpareItemResponseDto response = spareItemService.getSpareItemById(spareItemId);
        return ResponseEntity.ok(response);
    }

    // ---------------- GET ALL APPROVED ----------------
    @GetMapping("/all/approved")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER')")
    public ResponseEntity<List<SpareItemResponseDto>> getAllApproved() {
        List<SpareItemResponseDto> response = spareItemService.getAllApprovedSpareItems();
        return ResponseEntity.ok(response);
    }

    // ---------------- GET ALL FOR ADMIN ----------------
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SpareItemResponseDto>> getAllForAdmin() {
        List<SpareItemResponseDto> response = spareItemService.getAllSpareItemsForAdmin();
        return ResponseEntity.ok(response);
    }

    // ================= OWNERSHIP =================

    @GetMapping("/platform")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SpareItemResponseDto>> getPlatformItems() {
        return ResponseEntity.ok(
                spareItemService.findPlatformItems()
        );
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public ResponseEntity<List<SpareItemResponseDto>> getPartnerItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                spareItemService.findPartnerItems(partnerId)
        );
    }

    // ---------------- GET CATEGORY ----------------
    @GetMapping("/categories/all")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(spareItemService.getAllCategories());
    }

    // ---------------- GET APPROVED CATEGORY ----------------
    @GetMapping("/all/approved/category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER')")
    public ResponseEntity<List<SpareItemResponseDto>> getApprovedByCategory(@RequestParam String category) {
        List<SpareItemResponseDto> response = spareItemService.getApprovedByCategory(category);
        return ResponseEntity.ok(response);
    }


}
