package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.CategoryResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SpareItemService {

    // ---------------- CREATE ----------------
    SpareItemResponseDto createSpareItem(Integer managerId, SpareItemRequestDto requestDto, List<MultipartFile> images);

    // ---------------- UPDATE ----------------
    SpareItemResponseDto updateSpareItemByManager(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images);
    SpareItemResponseDto updateSpareItemByAdmin(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images);

    // ---------------- DELETE / RESTORE ----------------
    void deleteSpareItem(Integer spareItemId);  // Soft delete
    SpareItemResponseDto restoreSpareItem(Integer spareItemId);

    // ---------------- APPROVE ----------------
    SpareItemResponseDto approveSpareItemUpdate(Integer spareItemId, Integer adminId);

    // ---------------- GET ----------------
    SpareItemResponseDto getSpareItemById(Integer spareItemId);
    List<SpareItemResponseDto> getAllApprovedSpareItems(); // For customers
    List<SpareItemResponseDto> getAllSpareItemsForAdmin(); // For admin
    List<SpareItemResponseDto> getSpareItemsByManager(Integer managerId); // For manager
    List<CategoryResponseDto> getAllCategories();

}
