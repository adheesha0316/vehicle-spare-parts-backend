package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.CategoryResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import com.spareparts.spareparts_backend.entity.Manager;
import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.SpareItemCategory;
import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import com.spareparts.spareparts_backend.enums.StockStatus;
import com.spareparts.spareparts_backend.repo.ManagerRepo;
import com.spareparts.spareparts_backend.repo.SpareItemRepo;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.SpareItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SpareItemServiceImpl implements SpareItemService {

    private final SpareItemRepo spareItemRepo;
    private final ManagerRepo managerRepo;
    private final ModelMapper modelMapper;
    private final UserRepo userRepo;

    private static final String UPLOAD_DIR = "uploads/spareItem/";

    // ================= CREATE =================

    @Override
    public SpareItemResponseDto createSpareItem(Integer managerId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        Manager manager = managerRepo.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        SpareItem spareItem = SpareItem.builder()
                .name(requestDto.getName())
                .brand(requestDto.getBrand())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .price(requestDto.getPrice())
                .quantity(requestDto.getQuantity())
                .stockStatus(StockStatus.valueOf(requestDto.getStockStatus()))
                .status(SpareItemStatus.APPROVED) // Manager add → no admin approval needed
                .images(storeImages(images))
                .manager(manager)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);    }

    // ================= UPDATE (MANAGER) =================

    @Override
    public SpareItemResponseDto updateSpareItemByManager(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        SpareItem spareItem = getActiveSpareItem(spareItemId);

        spareItem.setPendingName(requestDto.getName());
        spareItem.setPendingBrand(requestDto.getBrand());
        spareItem.setPendingDescription(requestDto.getDescription());
        spareItem.setPendingPrice(requestDto.getPrice());
        spareItem.setPendingQuantity(requestDto.getQuantity());

        spareItem.setPendingCategory(requestDto.getCategory());

        if (images != null && !images.isEmpty()) {
            validateImages(images);
            spareItem.setPendingImages(storeImages(images));
        }

        spareItem.setStatus(SpareItemStatus.UPDATE_PENDING);
        spareItem.setUpdatedAt(LocalDateTime.now());

        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);
    }

    // ================= UPDATE (ADMIN) =================

    @Override
    public SpareItemResponseDto updateSpareItemByAdmin(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        SpareItem spareItem = getActiveSpareItem(spareItemId);

        spareItem.setName(requestDto.getName());
        spareItem.setBrand(requestDto.getBrand());
        spareItem.setDescription(requestDto.getDescription());
        spareItem.setCategory(requestDto.getCategory());
        spareItem.setPrice(requestDto.getPrice());
        spareItem.setQuantity(requestDto.getQuantity());

        if (images != null && !images.isEmpty()) {
            validateImages(images);
            spareItem.setImages(storeImages(images));
        }

        spareItem.setStatus(SpareItemStatus.APPROVED);
        spareItem.setUpdatedAt(LocalDateTime.now());

        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);
    }

    // ================= DELETE / RESTORE =================

    @Override
    public void deleteSpareItem(Integer spareItemId) {
        SpareItem spareItem = getActiveSpareItem(spareItemId);
        spareItem.setStatus(SpareItemStatus.DELETED);
        spareItem.setUpdatedAt(LocalDateTime.now());
        spareItemRepo.save(spareItem);
    }


    @Override
    public SpareItemResponseDto restoreSpareItem(Integer spareItemId) {
        SpareItem spareItem = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new RuntimeException("Spare item not found"));

        if (spareItem.getStatus() != SpareItemStatus.DELETED) {
            throw new RuntimeException("Spare item is not deleted");
        }

        spareItem.setStatus(SpareItemStatus.APPROVED);
        spareItem.setUpdatedAt(LocalDateTime.now());
        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);
    }

    // ================= APPROVE UPDATE =================

    @Override
    public SpareItemResponseDto approveSpareItemUpdate(Integer spareItemId, Integer adminId) {
        // Get the spare item (must exist and not be deleted)
        SpareItem spareItem = getActiveSpareItem(spareItemId);

        // Get the admin approving this update
        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Apply pending changes if they exist
        if (spareItem.getPendingName() != null) {
            spareItem.setName(spareItem.getPendingName());
        }
        if (spareItem.getPendingBrand() != null) {
            spareItem.setBrand(spareItem.getPendingBrand());
        }
        if (spareItem.getPendingDescription() != null) {
            spareItem.setDescription(spareItem.getPendingDescription());
        }
        if (spareItem.getPendingPrice() != null) {
            spareItem.setPrice(spareItem.getPendingPrice());
        }
        if (spareItem.getPendingQuantity() != null) {
            spareItem.setQuantity(spareItem.getPendingQuantity());
        }
        if (spareItem.getPendingCategory() != null) {
            spareItem.setCategory(spareItem.getPendingCategory());
        }
        if (spareItem.getPendingImages() != null && !spareItem.getPendingImages().isEmpty()) {
            spareItem.setImages(new ArrayList<>(spareItem.getPendingImages()));
        }

        // Clear pending fields
        spareItem.setPendingName(null);
        spareItem.setPendingBrand(null);
        spareItem.setPendingDescription(null);
        spareItem.setPendingPrice(null);
        spareItem.setPendingQuantity(null);
        spareItem.setPendingCategory(null);
        spareItem.setPendingImages(new ArrayList<>());

        // Set status and approved admin
        spareItem.setStatus(SpareItemStatus.APPROVED);
        spareItem.setApprovedByAdmin(admin);
        spareItem.setUpdatedAt(LocalDateTime.now());

        // Save changes
        spareItemRepo.save(spareItem);

        // Return the response DTO
        return mapToResponseDto(spareItem);
    }

    // ================= GET =================

    @Override
    public SpareItemResponseDto getSpareItemById(Integer spareItemId) {
        return mapToResponseDto(getActiveSpareItem(spareItemId));
    }

    @Override
    public List<SpareItemResponseDto> getAllApprovedSpareItems() {
        List<SpareItem> items = spareItemRepo.findByStatusNot(SpareItemStatus.DELETED);
        return items.stream().map(this::mapToResponseDto).toList();
    }

    @Override
    public List<SpareItemResponseDto> getAllSpareItemsForAdmin() {
        // Directly map all spare items to DTOs without multiple streams
        List<SpareItem> items = spareItemRepo.findAll();
        List<SpareItemResponseDto> response = new ArrayList<>(items.size());
        for (SpareItem item : items) {
            response.add(mapToResponseDto(item));
        }
        return response;
    }

    @Override
    public List<SpareItemResponseDto> getSpareItemsByManager(Integer managerId) {
        // Filter by manager and map to DTOs
        List<SpareItem> items = spareItemRepo.findAll();
        List<SpareItemResponseDto> response = new ArrayList<>();
        for (SpareItem item : items) {
            if (item.getManager() != null && item.getManager().getManagerId().equals(managerId)) {
                response.add(mapToResponseDto(item));
            }
        }
        return response;
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return Arrays.stream(SpareItemCategory.values())
                .map(category -> new CategoryResponseDto(
                        category.name(),        // key
                        category.getLabelEn(),  // English label
                        category.getLabelSi()   // Sinhala label
                ))
                .toList();
    }


    // ================= HELPERS =================
    private SpareItem getActiveSpareItem(Integer id) {
        return spareItemRepo.findBySpareItemIdAndStatusNot(id, SpareItemStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Spare item not found"));
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) throw new RuntimeException("At least 1 image is required");
        if (images.size() > 5) throw new RuntimeException("Maximum 5 images allowed");
    }

    private List<String> storeImages(List<MultipartFile> images) {
        List<String> paths = new ArrayList<>();
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            for (MultipartFile file : images) {
                String filename = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
                Path path = Paths.get(UPLOAD_DIR + filename);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                paths.add(path.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store images", e);
        }
        return paths;
    }

    private SpareItemResponseDto mapToResponseDto(SpareItem item) {
        return new SpareItemResponseDto(
                item.getSpareItemId(),
                item.getName(),
                item.getBrand(),
                item.getDescription(),
                item.getCategory().name(),
                item.getPrice(),
                item.getStockStatus() != null ? item.getStockStatus().name() : null,
                item.getImages(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getManager() != null ? item.getManager().getManagerId() : null,
                item.getApprovedByAdmin() != null ? item.getApprovedByAdmin().getUserId() : null
        );
    }
}
