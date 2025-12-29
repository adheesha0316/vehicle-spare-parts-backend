package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SpareItemService {
    // ================= CREATE =================

    // MANAGER / ADMIN
    SpareItemResponseDto createSpareItem(
            Integer managerId,
            SpareItemRequestDto requestDto,
            List<MultipartFile> images
    );

    // ================= UPDATE =================

    // MANAGER (requires ADMIN approval)
    SpareItemResponseDto updateSpareItemByManager(
            Integer spareItemId,
            SpareItemRequestDto requestDto,
            List<MultipartFile> images
    );

    // ADMIN (direct update)
    SpareItemResponseDto updateSpareItemByAdmin(
            Integer spareItemId,
            SpareItemRequestDto requestDto,
            List<MultipartFile> images
    );

    // ================= DELETE =================

    // ADMIN only (soft delete)
    void deleteSpareItem(Integer spareItemId);

    // ================= APPROVAL =================

    // ADMIN approves manager update
    SpareItemResponseDto approveSpareItemUpdate(Integer spareItemId);

    // ================= GET =================

    SpareItemResponseDto getSpareItemById(Integer spareItemId);

    List<SpareItemResponseDto> getAllApprovedSpareItems(); // CUSTOMER

    List<SpareItemResponseDto> getAllSpareItemsForAdmin(); // ADMIN

    List<SpareItemResponseDto> getSpareItemsByManager(Integer managerId);
}
