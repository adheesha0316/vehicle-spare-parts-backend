package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.CategoryResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import com.spareparts.spareparts_backend.service.SpareItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spare-item")
@RequiredArgsConstructor
@CrossOrigin
public class SpareItemController {

    private final SpareItemService spareItemService;


    // ---------------- GET ALL APPROVED ----------------
    @GetMapping("/all/approved")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SpareItemResponseDto>> getAllApproved() {
        List<SpareItemResponseDto> response = spareItemService.getAllApprovedSpareItems();
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

    // ---------------- GET CATEGORY ----------------
    @GetMapping("/categories/all")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(spareItemService.getAllCategories());
    }

    // ---------------- GET BY ID ----------------
    @GetMapping("/get/{spareItemId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SpareItemResponseDto> getById(@PathVariable Integer spareItemId) {
        SpareItemResponseDto response = spareItemService.getSpareItemById(spareItemId);
        return ResponseEntity.ok(response);
    }

    // ---------------- GET APPROVED CATEGORY ----------------
    @GetMapping("/all/approved/category")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SpareItemResponseDto>> getApprovedByCategory(@RequestParam String category) {
        List<SpareItemResponseDto> response = spareItemService.getApprovedByCategory(category);
        return ResponseEntity.ok(response);
    }

    // ---------------- SEARCH / FILTER (Optional Recommendation) ----------------
    // උදා: නම අනුව සෙවීමට අලුත් එකක්
    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SpareItemResponseDto>> searchSpareItems(@RequestParam String query) {
        return ResponseEntity.ok(spareItemService.searchSpareItems(query));
    }


}
