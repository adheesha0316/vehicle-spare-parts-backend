package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.CategoryResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SpareItemService {

    // ---------------- CREATE ----------------
    // Create platform item (admin/manager)
    SpareItemResponseDto createPlatformSpareItem(SpareItemRequestDto requestDto, List<MultipartFile> images);

    // Create partner item
    SpareItemResponseDto createPartnerSpareItem(Integer partnerId, SpareItemRequestDto requestDto, List<MultipartFile> images);

    // ---------------- UPDATE ----------------
    SpareItemResponseDto updateSpareItemByManager(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images);
    SpareItemResponseDto updateSpareItemByAdmin(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images);

    // ---------------- DELETE / RESTORE ----------------
    void deleteSpareItem(Integer spareItemId);  // Soft delete
    SpareItemResponseDto restoreSpareItem(Integer spareItemId);

    // ---------------- APPROVE ----------------
    SpareItemResponseDto approveSpareItemUpdate(Integer spareItemId, Integer approverId);

    // ---------------- GET ----------------
    SpareItemResponseDto getSpareItemById(Integer spareItemId);
    List<SpareItemResponseDto> getAllApprovedSpareItems(); // For customers
    List<SpareItemResponseDto> getApprovedByCategory(String categoryKey);
    List<SpareItemResponseDto> getAllSpareItemsForAdmin(); // For admin
    List<CategoryResponseDto> getAllCategories();

    //--------------- SEARCH ---------------
    List<SpareItemResponseDto> searchSpareItems(String query);

    // ---------------- OWNERSHIP ----------------
    List<SpareItemResponseDto> findPlatformItems();          // All items owned by PLATFORM_OWNER
    List<SpareItemResponseDto> findPartnerItems(Integer partnerId);  // All items owned by a speci
}
