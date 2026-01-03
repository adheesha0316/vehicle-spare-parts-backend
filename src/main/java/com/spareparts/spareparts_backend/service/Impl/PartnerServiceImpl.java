package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.PartnerRequestDto;
import com.spareparts.spareparts_backend.dto.PartnerResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import com.spareparts.spareparts_backend.entity.Partner;
import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.PartnerStatus;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import com.spareparts.spareparts_backend.enums.StockStatus;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.PartnerAgreementRepo;
import com.spareparts.spareparts_backend.repo.PartnerRepo;
import com.spareparts.spareparts_backend.repo.SpareItemRepo;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepo partnerRepo;
    private final PartnerAgreementRepo agreementRepo;
    private final SpareItemRepo spareItemRepo;
    private final UserRepo userRepo;

    private final Path ROOT_DIR = Paths.get("uploads/partner");

    @Autowired
    public PartnerServiceImpl(
            PartnerRepo partnerRepo,
            PartnerAgreementRepo agreementRepo,
            SpareItemRepo spareItemRepo,
            UserRepo userRepo
    ) {
        this.partnerRepo = partnerRepo;
        this.agreementRepo = agreementRepo;
        this.spareItemRepo = spareItemRepo;
        this.userRepo = userRepo;
    }



    // ================= PARTNER PROFILE ================
    @Override
    public PartnerResponseDto createPartnerProfile(Integer userId, PartnerRequestDto requestDto, MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Manager check
        if(user.getRole().equals(Role.MANAGER)) {
            throw new RuntimeException("Manager cannot register as Partner");
        }

        // Check if email already registered as Partner
        if(partnerRepo.existsByUser_Email(user.getEmail())) {
            throw new RuntimeException("This email is already registered as a Partner");
        }

        if (partnerRepo.existsByUser_UserId(userId)) {
            throw new RuntimeException("Partner profile already exists");
        }

        Partner partner = Partner.builder()
                .user(user)
                .fullName(requestDto.getFullName())
                .phone(requestDto.getPhone())
                .nicNumber(requestDto.getNicNumber())
                .shopName(requestDto.getShopName())
                .shopAddress(requestDto.getShopAddress())
                .branchName(requestDto.getBranchName())
                .nicFrontImage(saveFile(null, nicFront, "nicFront"))
                .nicBackImage(saveFile(null, nicBack, "nicBack"))
                .profileImage(saveFile(null, profileImage, "profile"))
                .status(PartnerStatus.PENDING)
                .agreementSignedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        partnerRepo.save(partner);
        return mapToDto(partner);
    }

    @Override
    public PartnerResponseDto requestProfileUpdate(Integer partnerId, PartnerRequestDto requestDto, MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        partner.setPendingFullName(requestDto.getFullName());
        partner.setPendingPhone(requestDto.getPhone());
        partner.setPendingNicNumber(requestDto.getNicNumber());
        partner.setPendingShopName(requestDto.getShopName());
        partner.setPendingShopAddress(requestDto.getShopAddress());
        partner.setPendingNicFrontImage(saveFile(partnerId, nicFront, "pending_nicFront"));
        partner.setPendingNicBackImage(saveFile(partnerId, nicBack, "pending_nicBack"));
        partner.setPendingProfileImage(saveFile(partnerId, profileImage, "pending_profile"));
        partner.setUpdatedAt(LocalDateTime.now());

        partnerRepo.save(partner);
        return mapToDto(partner);
    }

    @Override
    public PartnerResponseDto requestProfileDelete(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        partner.setStatus(PartnerStatus.PENDING); // pending deletion for admin approval
        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);

        return mapToDto(partner);
    }

    @Override
    public void deletePartnerByAdmin(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        partner.setStatus(PartnerStatus.DELETED);
        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);
    }

    @Override
    public void restorePartnerByAdmin(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        if (partner.getStatus() != PartnerStatus.DELETED)
            throw new RuntimeException("Partner is not deleted");
        partner.setStatus(PartnerStatus.APPROVED);
        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);
    }

    @Override
    public PartnerResponseDto getPartnerByUserId(Integer userId) {
        Partner partner = partnerRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        return mapToDto(partner);
    }

    @Override
    public PartnerResponseDto getPartnerById(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Partner not found with id " + partnerId
                        )
                );

        return mapToDto(partner);
    }

    @Override
    public List<PartnerResponseDto> getAllPartners() {
        return partnerRepo.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ================= AGREEMENTS =================
    @Override
    public byte[] downloadAgreement(Integer agreementId) {
        PartnerAgreement agreement = agreementRepo.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        try {
            return Files.readAllBytes(Paths.get(agreement.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Error reading agreement file", e);
        }
    }

    @Override
    public PartnerResponseDto acceptAgreement(Integer partnerId, MultipartFile signedAgreement) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        PartnerAgreement latest = getLatestAgreement();
        if (latest == null) throw new RuntimeException("No agreement exists");

        String filePath = saveFile(partnerId, signedAgreement,
                "signed_agreement_" + partnerId + "_v" + latest.getVersion());

        partner.setAgreedAgreement(latest);
        partner.setAgreementSignedAt(LocalDateTime.now());
        partnerRepo.save(partner);

        return mapToDto(partner);
    }

    @Override
    public PartnerAgreement uploadAgreement(MultipartFile agreementFile, String version) {
        String filePath = saveFile(0, agreementFile, "admin_agreement_v" + version);

        PartnerAgreement agreement = PartnerAgreement.builder()
                .filePath(filePath)
                .version(version)
                .createdAt(LocalDateTime.now())
                .build();

        return agreementRepo.save(agreement);
    }

    @Override
    public void removeAgreement(Integer agreementId) {
        PartnerAgreement agreement = agreementRepo.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        try {
            Files.deleteIfExists(Paths.get(agreement.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Error deleting agreement file", e);
        }
        agreementRepo.delete(agreement);
    }

    @Override
    public PartnerAgreement uploadNewAgreementVersion(MultipartFile agreementFile, String version) {
        return uploadAgreement(agreementFile, version);
    }

    // ================= SPARE ITEM =================

    @Override
    public SpareItemResponseDto createSpareItemRequest(Integer partnerId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        // HERE is the correct place
        StockStatus stockStatus =
                StockStatus.valueOf(requestDto.getStockStatus().toUpperCase());

        SpareItem item = SpareItem.builder()
                .name(requestDto.getName())
                .brand(requestDto.getBrand())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .price(requestDto.getPrice())
                .quantity(requestDto.getQuantity())
                .stockStatus(stockStatus)   // ENUM here
                .status(SpareItemStatus.PENDING)
                .partner(partner)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (images != null && !images.isEmpty()) {
            List<String> imagePaths = images.stream()
                    .map(img -> saveFile(partnerId, img, "spare"))
                    .toList();
            item.setImages(imagePaths);
        }

        spareItemRepo.save(item);
        return mapToDto(item);
    }

    @Override
    public SpareItemResponseDto requestSpareItemUpdate(Integer partnerId, Integer spareItemId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new RuntimeException("Spare item not found"));

        // HERE is the correct place
        StockStatus stockStatus =
                StockStatus.valueOf(requestDto.getStockStatus().toUpperCase());

        item.setPendingName(requestDto.getName());
        item.setPendingBrand(requestDto.getBrand());
        item.setPendingDescription(requestDto.getDescription());
        item.setPendingCategory(requestDto.getCategory());
        item.setPendingPrice(requestDto.getPrice());
        item.setPendingQuantity(requestDto.getQuantity());
        item.setStockStatus(stockStatus);

        if (images != null && !images.isEmpty()) {
            List<String> pendingImages = images.stream()
                    .map(img -> saveFile(partnerId, img, "pending_spare_item"))
                    .collect(Collectors.toList());
            item.setPendingImages(pendingImages);
        }

        item.setStatus(SpareItemStatus.UPDATE_PENDING);
        item.setUpdatedAt(LocalDateTime.now());

        spareItemRepo.save(item);
        return mapToDto(item);
    }

    @Override
    public SpareItemResponseDto requestSpareItemDelete(Integer partnerId, Integer spareItemId) {
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new RuntimeException("SpareItem not found"));

        item.setStatus(SpareItemStatus.PENDING); // pending delete
        item.setUpdatedAt(LocalDateTime.now());
        spareItemRepo.save(item);

        return mapToDto(item);
    }

    @Override
    public SpareItemResponseDto approveOrRejectSpareItem(Integer spareItemId, boolean approve, String rejectionReason, Integer approverId) {
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new RuntimeException("SpareItem not found"));

        User approver = userRepo.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        if (approve) {
            item.setStatus(SpareItemStatus.APPROVED);
            item.setApprovedBy(approver);
        } else {
            item.setStatus(SpareItemStatus.DELETED); // rejected
            item.setApprovedBy(approver);
        }

        item.setUpdatedAt(LocalDateTime.now());
        spareItemRepo.save(item);

        return mapToDto(item);
    }

    @Override
    public List<SpareItemResponseDto> getApprovedSpareItemsByPartner(Integer partnerId) {
        return spareItemRepo.findAll().stream()
                .filter(i -> i.getStatus() == SpareItemStatus.APPROVED)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpareItemResponseDto> getRejectedSpareItemsByPartner(Integer partnerId) {
        return spareItemRepo.findAll().stream()
                .filter(i -> i.getStatus() == SpareItemStatus.DELETED)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpareItemResponseDto> getPendingSpareItemsByPartner(Integer partnerId) {
        return spareItemRepo.findAll()
                .stream()
                .filter(i -> i.getStatus() == SpareItemStatus.PENDING ||
                        i.getStatus() == SpareItemStatus.UPDATE_PENDING)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void validateAgreementAccepted(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        if (partner.getAgreedAgreement() == null) {
            throw new RuntimeException("Partner must accept latest agreement first");
        }
    }

    @Override
    public PartnerAgreement getLatestAgreement() {
        return agreementRepo.findTopByOrderByCreatedAtDesc().orElse(null);

    }

    // ================= UTILS =================

    private String saveFile(Integer partnerId, MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) return null;
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : "";
            String name = prefix + "_" + timestamp + ext;

            Path dir = (partnerId != null) ? ROOT_DIR.resolve(String.valueOf(partnerId))
                    : ROOT_DIR.resolve("admin");

            if (!Files.exists(dir)) Files.createDirectories(dir);

            Path target = dir.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private PartnerResponseDto mapToDto(Partner partner) {
        return new PartnerResponseDto(
                partner.getPartnerId(),
                partner.getUser().getUserId(),
                partner.getFullName(),
                partner.getPhone(),
                partner.getShopName(),
                partner.getShopAddress(),
                partner.getBranchName(),
                partner.getProfileImage(),
                partner.getStatus(),
                partner.getAgreedAgreement() != null
                        ? partner.getAgreedAgreement().getVersion()
                        : null,
                partner.getAgreedAgreement() != null,
                partner.getAgreementSignedAt(),
                partner.getApprovedByAdmin() != null
                        ? partner.getApprovedByAdmin().getUserId()
                        : null,
                partner.getCreatedAt(),
                partner.getUpdatedAt()
        );
    }


    private SpareItemResponseDto mapToDto(SpareItem item) {
        return new SpareItemResponseDto(
                item.getSpareItemId(),
                item.getName(),
                item.getBrand(),
                item.getDescription(),
                item.getCategory().name(),
                item.getPrice(),
                item.getStockStatus().name(),
                item.getImages(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getManager() != null ? item.getManager().getManagerId() : null,
                item.getApprovedBy() != null ? item.getApprovedBy().getUserId() : null
        );
    }
}
