package com.spareparts.spareparts_backend.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.CategoryResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import com.spareparts.spareparts_backend.entity.Partner;
import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.OwnershipStatus;
import com.spareparts.spareparts_backend.enums.SpareItemCategory;
import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import com.spareparts.spareparts_backend.enums.StockStatus;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.PartnerRepo;
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
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class SpareItemServiceImpl implements SpareItemService {

    private final SpareItemRepo spareItemRepo;
    private final PartnerRepo partnerRepo;
    private final UserRepo userRepo;
    private final ModelMapper mapper;

    private static final String UPLOAD_DIR = "uploads/spareItem/";


    // ================= CREATE =================
    @Override
    public SpareItemResponseDto createPlatformSpareItem(SpareItemRequestDto requestDto, List<MultipartFile> images) {
        List<String> imagePaths = (images != null && !images.isEmpty()) ? storeImages(images) : new ArrayList<>();

        SpareItem spareItem = SpareItem.builder()
                .name(requestDto.getName())
                .brand(requestDto.getBrand())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .price(requestDto.getPrice())
                .quantity(requestDto.getQuantity())
                .stockStatus(parseStockStatus(requestDto.getStockStatus()))
                .status(SpareItemStatus.APPROVED)
                .ownership(OwnershipStatus.PLATFORM_OWNER)
                .images(imagePaths)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);
    }

    @Override
    public SpareItemResponseDto createPartnerSpareItem(Integer partnerId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        List<String> imagePaths = (images != null && !images.isEmpty()) ? storeImages(images) : new ArrayList<>();

        SpareItem spareItem = SpareItem.builder()
                .name(requestDto.getName())
                .brand(requestDto.getBrand())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .price(requestDto.getPrice())
                .quantity(requestDto.getQuantity())
                .stockStatus(parseStockStatus(requestDto.getStockStatus()))
                .status(SpareItemStatus.PENDING)
                .ownership(OwnershipStatus.PARTNER)
                .partner(partner)
                .images(imagePaths)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);
    }

    // ================= UPDATE (MANAGER) =================
    @Override
    public SpareItemResponseDto updateSpareItemByManager(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        SpareItem spareItem = getActiveSpareItem(spareItemId);

        applySpareItemUpdates(spareItem, requestDto, images);

        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);
    }

    // ================= UPDATE (ADMIN) =================

    @Override
    public SpareItemResponseDto updateSpareItemByAdmin(Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        SpareItem spareItem = getActiveSpareItem(spareItemId);

        applySpareItemUpdates(spareItem, requestDto, images);
        spareItem.setStatus(SpareItemStatus.APPROVED);

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
    public SpareItemResponseDto approveSpareItemUpdate(Integer spareItemId, Integer approverId) {
        SpareItem spareItem = getActiveSpareItem(spareItemId);

        User approver = userRepo.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        // Only allow ADMIN or MANAGER
        if (!(approver.getRole().name().equals("ADMIN") || approver.getRole().name().equals("MANAGER"))) {
            throw new RuntimeException("User is not authorized to approve");
        }

        spareItem.setStatus(SpareItemStatus.APPROVED);
        spareItem.setUpdatedAt(LocalDateTime.now());
        spareItem.setApprovedBy(approver);  // single field for both roles

        spareItemRepo.save(spareItem);
        return mapToResponseDto(spareItem);
    }

    // ================= GET =================

    @Override
    public SpareItemResponseDto getSpareItemById(Integer spareItemId) {
        SpareItem spareItem = spareItemRepo
                .findById(spareItemId)
                .filter(item -> item.getStatus() != SpareItemStatus.DELETED)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Spare item not found with id " + spareItemId
                        )
                );

        return mapToResponseDto(spareItem);
    }

    @Override
    public List<SpareItemResponseDto> getAllApprovedSpareItems() {
        return spareItemRepo.findByStatusNot(SpareItemStatus.DELETED)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    // ---------------- GET APPROVED BY CATEGORY ----------------
    @Override
    public List<SpareItemResponseDto> getApprovedByCategory(String categoryKey) {
        SpareItemCategory category;
        try {
            category = SpareItemCategory.valueOf(categoryKey.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid category: " + categoryKey);
        }

        return spareItemRepo.findByCategoryAndStatus(category, SpareItemStatus.APPROVED)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }


    @Override
    public List<SpareItemResponseDto> getAllSpareItemsForAdmin() {
        return spareItemRepo.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return Arrays.stream(SpareItemCategory.values())
                .map(category -> new CategoryResponseDto(
                        category.name(),        // key
                        category.getLabelEn(),  // English label
                        category.getLabelSi()   // Sinhalese label
                ))
                .toList();
    }

    @Override
    public List<SpareItemResponseDto> searchSpareItems(String query) {
        // Approved items විතරක් search එකට අහුකරමු
        List<SpareItem> items = spareItemRepo
                .findByItemNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
                        query, query, SpareItemStatus.APPROVED);

        return items.stream()
                .map(item -> mapper.map(item, SpareItemResponseDto.class)) // ModelMapper හෝ manual mapping
                .toList();
    }

    @Override
    public List<SpareItemResponseDto> findPlatformItems() {
        return spareItemRepo
                .findByOwnershipAndStatusNot(
                        OwnershipStatus.PLATFORM_OWNER,
                        SpareItemStatus.DELETED
                )
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public List<SpareItemResponseDto> findPartnerItems(Integer partnerId) {
        return spareItemRepo
                .findByOwnershipAndPartner_PartnerIdAndStatusNot(
                        OwnershipStatus.PARTNER,
                        partnerId,
                        SpareItemStatus.DELETED
                )
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }


    // ================= HELPERS =================
    private SpareItem getActiveSpareItem(Integer id) {
        return spareItemRepo.findBySpareItemIdAndStatusNot(id, SpareItemStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Spare item not found"));
    }

    private StockStatus parseStockStatus(String value) {
        try {
            return StockStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid stock status: " + value);
        }
    }

    private void applySpareItemUpdates(
            SpareItem spareItem,
            SpareItemRequestDto requestDto,
            List<MultipartFile> images
    ) {
        spareItem.setName(requestDto.getName());
        spareItem.setBrand(requestDto.getBrand());
        spareItem.setDescription(requestDto.getDescription());
        spareItem.setCategory(requestDto.getCategory());
        spareItem.setPrice(requestDto.getPrice());
        spareItem.setQuantity(requestDto.getQuantity());
        spareItem.setStockStatus(parseStockStatus(requestDto.getStockStatus()));

        if (images != null && !images.isEmpty()) {
            spareItem.setImages(storeImages(images));
        }

        spareItem.setUpdatedAt(LocalDateTime.now());
    }


    private List<String> storeImages(List<MultipartFile> images) {
        List<String> paths = new ArrayList<>();
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            for (MultipartFile file : images) {
                String filename = System.currentTimeMillis() + "_" +
                        StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                Path path = Paths.get(UPLOAD_DIR, filename);
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
                item.getOwnership(),
                item.getPartner() != null ? item.getPartner().getPartnerId() : null,
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getApprovedBy() != null ? item.getApprovedBy().getUserId() : null
        );
    }
}
