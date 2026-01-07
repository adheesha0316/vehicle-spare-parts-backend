package com.spareparts.spareparts_backend.service.Impl;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.spareparts.spareparts_backend.dto.*;
import com.spareparts.spareparts_backend.entity.*;
import com.spareparts.spareparts_backend.enums.*;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.*;
import com.spareparts.spareparts_backend.service.PartnerService;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepo partnerRepo;
    private final PartnerAgreementRepo agreementRepo;
    private final SpareItemRepo spareItemRepo;
    private final UserRepo userRepo;
    private final PartnerAgreementRepo partnerAgreementRepo;
    private final ModelMapper modelMapper;
    private final PartnerSignedAgreementRepo partnerSignedAgreementRepo;

    private final Path ROOT_DIR = Paths.get("uploads/partner");

    @Autowired
    public PartnerServiceImpl(PartnerRepo partnerRepo, PartnerAgreementRepo agreementRepo, SpareItemRepo spareItemRepo, UserRepo userRepo, PartnerAgreementRepo partnerAgreementRepo, ModelMapper modelMapper, PartnerSignedAgreementRepo partnerSignedAgreementRepo) {
        this.partnerRepo = partnerRepo;
        this.agreementRepo = agreementRepo;
        this.spareItemRepo = spareItemRepo;
        this.userRepo = userRepo;
        this.partnerAgreementRepo = partnerAgreementRepo;
        this.modelMapper = modelMapper;
        this.partnerSignedAgreementRepo = partnerSignedAgreementRepo;
    }


    // ================= PARTNER PROFILE ================
    @Override
    public PartnerResponseDto createPartnerProfile(Integer userId, PartnerRequestDto requestDto, MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage) {
        // ===== Fetch User =====
        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id " + userId));

        // ===== Role check =====
        if (user.getRole() == Role.MANAGER) {
            throw new RuntimeException("Manager cannot register as Partner");
        }

        // ===== Duplicate checks =====
        if (partnerRepo.existsByUser_Email(user.getEmail())) {
            throw new RuntimeException("This email is already registered as a Partner");
        }

        if (partnerRepo.existsByUser_UserId(userId)) {
            throw new RuntimeException("Partner profile already exists");
        }

        // ===== Map DTO =====
        Partner partner = modelMapper.map(requestDto, Partner.class);
        partner.setUser(user);
        partner.setStatus(PartnerStatus.PENDING);
        partner.setCreatedAt(LocalDateTime.now());
        partner.setUpdatedAt(LocalDateTime.now());

        // ===== Store files (NEW STRUCTURE) =====
        if (nicFront != null && !nicFront.isEmpty()) {
            partner.setNicFrontImage(storeFile(nicFront, "nicFront"));
        }

        if (nicBack != null && !nicBack.isEmpty()) {
            partner.setNicBackImage(storeFile(nicBack, "nicBack"));
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            partner.setProfileImage(storeFile(profileImage, "profileImg"));
        }

        Partner savedPartner = partnerRepo.save(partner);
        return mapToDto(savedPartner);
    }

    @Override
    public PartnerResponseDto requestProfileUpdate(Integer partnerId, PartnerRequestDto requestDto, MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage) {
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));

        // ===== Only allow approved partners to request updates =====
        if (partner.getStatus() != PartnerStatus.APPROVED) {
            throw new RuntimeException("Partner profile is not approved. Cannot request updates.");
        }
        if (partner.getAgreedAgreement() == null) {
            throw new RuntimeException("You must accept the latest agreement before updating your profile.");
        }

        // ===== Set pending fields =====
        partner.setPendingFullName(requestDto.getFullName());
        partner.setPendingPhone(requestDto.getPhone());
        partner.setPendingNicNumber(requestDto.getNicNumber());
        partner.setPendingShopName(requestDto.getShopName());
        partner.setPendingShopAddress(requestDto.getShopAddress());

        // ===== Handle pending files =====
        if (nicFront != null && !nicFront.isEmpty()) {
            partner.setPendingNicFrontImage(saveFile(partnerId, nicFront, "pending_nicFront"));
        }
        if (nicBack != null && !nicBack.isEmpty()) {
            partner.setPendingNicBackImage(saveFile(partnerId, nicBack, "pending_nicBack"));
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            partner.setPendingProfileImage(saveFile(partnerId, profileImage, "pending_profile"));
        }

        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);

        return mapToDto(partner);
    }

    @Override
    public PartnerResponseDto requestProfileDelete(Integer partnerId) {
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));


        // ===== Only allow approved partners to request deletion =====
        if (partner.getStatus() != PartnerStatus.APPROVED) {
            throw new RuntimeException("Partner profile is not approved. Cannot request deletion.");
        }
        if (partner.getAgreedAgreement() == null) {
            throw new RuntimeException("You must accept the latest agreement before requesting profile deletion.");
        }

        // ===== Mark for pending deletion =====
        partner.setStatus(PartnerStatus.PENDING); // pending deletion for admin approval
        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);

        return mapToDto(partner);
    }

    @Override
    public PartnerResponseDto approvePartnerProfile(Integer partnerId) {
        // Fetch partner or throw exception
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with id " + partnerId
                ));

        // Approve partner
        partner.setStatus(PartnerStatus.APPROVED);
        partner.setUpdatedAt(LocalDateTime.now());

        // Save and return DTO
        return modelMapper.map(partnerRepo.save(partner), PartnerResponseDto.class);
    }

    @Override
    public PartnerResponseDto rejectPartnerProfile(Integer partnerId, String rejectionReason) {
        // Fetch partner or throw exception
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with id " + partnerId
                ));

        // Reject partner
        partner.setStatus(PartnerStatus.REJECTED);
        partner.setUpdatedAt(LocalDateTime.now());
        partner.setRejectionReason(rejectionReason); // optional: store reason

        // Save and return DTO
        return modelMapper.map(partnerRepo.save(partner), PartnerResponseDto.class);
    }


    @Override
    public void deletePartnerByAdmin(Integer partnerId) {
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));
        partner.setStatus(PartnerStatus.DELETED);
        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);
    }

    @Override
    public void restorePartnerByAdmin(Integer partnerId) {
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));
        if (partner.getStatus() != PartnerStatus.DELETED)
            throw new RuntimeException("Partner is not deleted");
        partner.setStatus(PartnerStatus.APPROVED);
        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);
    }

    @Override
    public PartnerResponseDto getPartnerByUserId(Integer userId) {
        Partner partner = partnerRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with userId " + userId
                ));
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
    public byte[] downloadAgreement(Integer partnerId) {
        // Fetch partner
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));

        // Profile approval check
        if (partner.getStatus() != PartnerStatus.APPROVED) {
            throw new IllegalStateException("Partner profile must be approved before downloading agreement");
        }

        // Agreement already accepted check
        if (Boolean.TRUE.equals(partner.getAgreementAccepted())) {
            throw new IllegalStateException("Agreement already accepted. Download is not allowed");
        }

        // Get latest agreement signed or generated for this partner
        String filePath;
        if (partner.getSignedAgreementPath() != null) {
            // Already signed / saved
            filePath = partner.getSignedAgreementPath();
        } else {
            // Use latest template
            PartnerAgreement latestAgreement = agreementRepo
                    .findByIsLatestTrueAndStatus(PartnerAgreementStatus.REQUIRED)
                    .orElseThrow(() -> new ResourceNotFoundException("No active agreement found"));

            filePath = latestAgreement.getFilePath();
        }

        // Normalize path (especially if DB stored backslashes)
        filePath = filePath.replace("\\", "/");

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalStateException("Agreement file does not exist at path: " + filePath);
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Error reading agreement PDF file", e);
        }
    }

    @Override
    public PartnerResponseDto acceptAgreement(Integer partnerId, MultipartFile signedAgreement) {

        // ================= Fetch Partner =================
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Partner not found with id " + partnerId)
                );

        // ================= Prevent double signing =================
        if (Boolean.TRUE.equals(partner.getAgreementAccepted())) {
            throw new IllegalStateException("Agreement already signed");
        }

        // ================= Get ACTIVE latest agreement =================
        PartnerAgreement latestAgreement =
                agreementRepo.findByIsLatestTrueAndStatus(PartnerAgreementStatus.REQUIRED)
                        .orElseThrow(() -> new ResourceNotFoundException("No active agreement found"));

        // ================= Save signed PDF =================
        // This method stores the file and returns the path
        String filePath = storeSignedAgreement(signedAgreement, partnerId, latestAgreement.getVersion());

        // ================= Save PartnerSignedAgreement record =================
        PartnerSignedAgreement signedAgreementEntity = PartnerSignedAgreement.builder()
                .partner(partner)
                .agreement(latestAgreement)
                .signedAt(LocalDateTime.now())
                .filePath(filePath)
                .version(latestAgreement.getVersion())
                .approvalStatus(AgreementApprovalStatus.PENDING) // initially pending approval
                .build();

        partnerSignedAgreementRepo.save(signedAgreementEntity);

        // ================= Update Partner =================
        partner.setAgreedAgreement(latestAgreement);
        partner.setAgreementSignedAt(LocalDateTime.now());
        partner.setSignedAgreementPath(filePath);
        partner.setAgreementAccepted(true);
        partner.setAgreementStatus(PartnerAgreementStatus.PENDING); // waiting admin approval

        partnerRepo.save(partner);

        // ================= Return DTO =================
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
        // Fetch the agreement or throw a ResourceNotFoundException if not found
        PartnerAgreement agreement = agreementRepo.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agreement not found with id " + agreementId));
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

    private void validateSpareItemActionAllowed(Partner partner) {
        // Partner profile must be approved
        if (partner.getStatus() != PartnerStatus.APPROVED) {
            throw new IllegalStateException(
                    "Partner profile is not approved yet."
            );
        }

        // Latest agreement must be accepted
        if (partner.getAgreedAgreement() == null ||
                Boolean.FALSE.equals(partner.getAgreementAccepted())) {

            throw new IllegalStateException(
                    "You must accept the latest agreement before creating, updating, or deleting spare items."
            );
        }
    }

    @Override
    public SpareItemResponseDto createSpareItemRequest(Integer partnerId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));

        // ===== Validate profile approval and agreement =====
        validateSpareItemActionAllowed(partner);

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
        // Fetch partner
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));


        // ===== Validate profile approval and agreement =====
        validateSpareItemActionAllowed(partner);

        // Fetch the spare item or throw exception if not found
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Spare item not found with id " + spareItemId));

        // Validate ownership
        if (!item.getPartner().getPartnerId().equals(partnerId)) {
            throw new RuntimeException("You cannot update a spare item that does not belong to you");
        }

        // Parse stock status safely
        StockStatus stockStatus;
        try {
            stockStatus = StockStatus.valueOf(requestDto.getStockStatus().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Invalid stock status: " + requestDto.getStockStatus());
        }

        // Set pending fields for update request
        item.setPendingName(requestDto.getName());
        item.setPendingBrand(requestDto.getBrand());
        item.setPendingDescription(requestDto.getDescription());
        item.setPendingCategory(requestDto.getCategory());
        item.setPendingPrice(requestDto.getPrice());
        item.setPendingQuantity(requestDto.getQuantity());
        item.setStockStatus(stockStatus); // current stock status

        // Handle pending images
        if (images != null && !images.isEmpty()) {
            List<String> pendingImages = images.stream()
                    .map(img -> saveFile(partnerId, img, "pending_spare_item"))
                    .collect(Collectors.toList());
            item.setPendingImages(pendingImages);
        }

        // Mark as update pending
        item.setStatus(SpareItemStatus.UPDATE_PENDING);
        item.setUpdatedAt(LocalDateTime.now());

        // Save and return DTO
        SpareItem updatedItem = spareItemRepo.save(item);
        return mapToDto(updatedItem);
    }

    @Override
    public SpareItemResponseDto requestSpareItemDelete(Integer partnerId, Integer spareItemId) {
        // Fetch partner
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));

        // ===== Validate profile approval and agreement =====
        validateSpareItemActionAllowed(partner);

        // Fetch the spare item or throw exception if not found
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Spare item not found with id " + spareItemId));

        // Optional: Validate that the item belongs to the requesting partner
        if (!item.getPartner().getPartnerId().equals(partnerId)) {
            throw new RuntimeException("You cannot delete a spare item that does not belong to you");
        }

        // Mark as pending deletion
        item.setStatus(SpareItemStatus.PENDING); // pending delete for admin approval
        item.setUpdatedAt(LocalDateTime.now());

        SpareItem updatedItem = spareItemRepo.save(item);
        return mapToDto(updatedItem);
    }

    @Override
    public SpareItemResponseDto approveOrRejectSpareItem(Integer spareItemId, boolean approve, String rejectionReason, Integer approverId) {
        // Fetch the spare item
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Spare item not found with id " + spareItemId));

        // Only pending items can be approved/rejected
        if (item.getStatus() != SpareItemStatus.PENDING && item.getStatus() != SpareItemStatus.UPDATE_PENDING) {
            throw new RuntimeException("Only pending or update-pending items can be approved or rejected");
        }

        // Fetch the approver
        User approver = userRepo.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id " + approverId));

        // Ensure approver has proper role
        if (approver.getRole() != Role.ADMIN && approver.getRole() != Role.MANAGER) {
            throw new RuntimeException("You are not authorized to approve or reject spare items");
        }

        if (approve) {
            item.setStatus(SpareItemStatus.APPROVED);
            item.setRejectionReason(null);

            // If this was an update request, copy pending fields to main fields
            if (item.getStatus() == SpareItemStatus.UPDATE_PENDING) {
                if (item.getPendingName() != null) item.setName(item.getPendingName());
                if (item.getPendingBrand() != null) item.setBrand(item.getPendingBrand());
                if (item.getPendingDescription() != null) item.setDescription(item.getPendingDescription());
                if (item.getPendingCategory() != null) item.setCategory(item.getPendingCategory());
                if (item.getPendingPrice() != null) item.setPrice(item.getPendingPrice());
                if (item.getPendingQuantity() != null) item.setQuantity(item.getPendingQuantity());
                if (item.getPendingImages() != null && !item.getPendingImages().isEmpty()) item.setImages(item.getPendingImages());

                // Clear pending fields
                item.setPendingName(null);
                item.setPendingBrand(null);
                item.setPendingDescription(null);
                item.setPendingCategory(null);
                item.setPendingPrice(null);
                item.setPendingQuantity(null);
                item.setPendingImages(null);
            }
        } else {
            item.setStatus(SpareItemStatus.DELETED);
            item.setRejectionReason(rejectionReason != null ? rejectionReason : "No reason provided");
        }

        item.setApprovedBy(approver);
        item.setUpdatedAt(LocalDateTime.now());

        SpareItem updatedItem = spareItemRepo.save(item);
        return mapToDto(updatedItem);
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
    public PartnerAgreement generateAgreementPdf(String partnerName, String companyName,String conditions, String version, boolean saveToDb) {
        try {
            // ================= Version duplicate check =================
            if (agreementRepo.existsByVersion(version)) {
                throw new RuntimeException(
                        "Agreement version already exists"
                );
            }

            // ================= Prepare file path =================
            Path agreementsDir = Paths.get("uploads/agreements");
            if (!Files.exists(agreementsDir)) Files.createDirectories(agreementsDir);

            String fileName = "partner-agreement-v" + version + ".pdf";
            Path filePath = agreementsDir.resolve(fileName);

            // ================= Create PDF =================
            PdfWriter writer = new PdfWriter(filePath.toString());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ================= Add page event for numbering =================
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event event) {
                    PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                    PdfPage page = docEvent.getPage();
                    PdfCanvas pdfCanvas = new PdfCanvas(page);
                    Rectangle pageSize = page.getPageSize();
                    int pageNumber = docEvent.getDocument().getPageNumber(page);
                    int totalPages = docEvent.getDocument().getNumberOfPages();

                    Canvas canvas = new Canvas(pdfCanvas, pageSize);
                    canvas.showTextAligned(
                            new Paragraph(String.format("Page %d of %d", pageNumber, totalPages))
                                    .setFontSize(9),
                            pageSize.getWidth() / 2,
                            20, // 20 units from bottom
                            TextAlignment.CENTER
                    );
                    canvas.close();
                }
            });

            // ================= Add company logo =================
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logo.png");
            if (logoStream != null) {
                ImageData logoData = ImageDataFactory.create(logoStream.readAllBytes());
                document.add(new Image(logoData)
                        .setWidth(120)
                        .setHeight(60)
                        .setMarginBottom(20)
                );
            } else {
                System.out.println("Logo not found in resources/logo.png");
            }

            // ================= Add title =================
            document.add(new Paragraph("PARTNER AGREEMENT")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20)
            );

            // ================= Agreement info table (COMMON) =================
            float[] columnWidths = {150F, 350F};
            Table infoTable = new Table(columnWidths).setMarginBottom(20);

            infoTable.addCell(new Cell().add(new Paragraph("Agreement Type").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph("Common Partner Agreement")));

            // Partner Name - Variable
            infoTable.addCell(new Cell().add(new Paragraph("Partner Name").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph("                        ")));

            // Company Name / Shop Name
            infoTable.addCell(new Cell().add(new Paragraph("Company / Shop Name").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph("                       ")));

            infoTable.addCell(new Cell().add(new Paragraph("Version").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph(version)));

            infoTable.addCell(new Cell().add(new Paragraph("Date").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph(LocalDate.now().toString())));

            document.add(infoTable);

            // ================= Conditions title =================
            document.add(new Paragraph("Conditions")
                    .setBold()
                    .setUnderline()
                    .setFontSize(14)
                    .setMarginBottom(2) // Reduced to stay close to the line
            );

            // Add a horizontal line (Separator)
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginBottom(12)
            );

            // ================= Normalize messy input =================
            String normalized = conditions
                    .replace("\\n", " ")
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            // ================= Split by numbering =================
            // Using a lookahead to split before digit + dot
            String[] blocks = normalized.split("(?=\\d+\\.)");

            int count = 1;

            for (String block : blocks) {
                // Clean up the block: remove leading "1. " if it exists
                block = block.replaceFirst("^\\d+\\.\\s*", "").trim();
                if (block.isEmpty()) continue;

                String headingText;
                String descriptionText;

                // Split into Heading and Description based on colon
                if (block.contains(":")) {
                    String[] parts = block.split(":", 2);
                    headingText = parts[0].trim();
                    descriptionText = parts[1].trim();
                } else {
                    headingText = "Condition " + count;
                    descriptionText = block;
                }

                // ---- Heading (Numbered) ----
                document.add(new Paragraph(count + ". " + headingText)
                        .setBold()
                        .setFontSize(12)
                        .setFixedLeading(14f) // Controls line spacing
                        .setMarginTop(8)
                        .setMarginBottom(0)   // Keep description close to heading
                );

                // ---- Description (Indented) ----
                document.add(new Paragraph(descriptionText)
                        .setFontSize(11)
                        .setMarginLeft(20)    // Creates the "hanging" look
                        .setMarginBottom(8)
                        .setFixedLeading(13f)
                        .setItalic()          // Optional: slight styling difference
                );

                count++;
            }



            // ================= Signatures =================
            Path signaturePath =
                    Paths.get("uploads/admin/signatures/admin-1.png");

            if (!Files.exists(signaturePath)) {
                throw new RuntimeException(
                        "Admin signature not found at " + signaturePath
                );
            }

            ImageData signatureData =
                    ImageDataFactory.create(Files.readAllBytes(signaturePath));

            Table signTable = new Table(new float[]{1, 1});
            signTable.setWidth(UnitValue.createPercentValue(100));
            signTable.setMarginTop(45);

            // ---- Admin ----
            Cell adminCell = new Cell().setBorder(Border.NO_BORDER);
            adminCell.add(new Image(signatureData)
                    .setWidth(120)
                    .setHeight(50)
            );
            adminCell.add(new Paragraph("System Owner / Director")
                    .setBold()
                    .setFontSize(12)
            );
            signTable.addCell(adminCell);

            // ---- Partner ----
            Cell partnerCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);

            partnerCell.add(new Paragraph("\n\n--------------------------"));
            partnerCell.add(new Paragraph("Partner")
                    .setBold()
                    .setFontSize(12)
            );
            signTable.addCell(partnerCell);

            document.add(signTable);

            // ================= Close =================
            document.close();

            // ================= DB update =================
            agreementRepo.updateLatestFalse();

            PartnerAgreement agreement = PartnerAgreement.builder()
                    .filePath(filePath.toString())
                    .version(version)
                    .conditions(conditions)
                    .createdAt(LocalDateTime.now())
                    .status(PartnerAgreementStatus.REQUIRED)
                    .isLatest(true)
                    .build();

            return agreementRepo.save(agreement);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to generate partner agreement PDF", e
            );
        }
    }

    /* ================= Helper cells ================= */

    private Cell cellBold(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setPadding(5);
    }

    private Cell cellNormal(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(5);
    }

    @Override
    public String getCurrentAgreementConditions() {
        try {
            // Get the current active/latest agreement from DB
            PartnerAgreement currentAgreement = partnerAgreementRepo
                    .findByIsLatestTrueAndStatus(PartnerAgreementStatus.REQUIRED)
                    .orElseThrow(() -> new ResourceNotFoundException("No active agreement found"));

            // Get the PDF file path
            String pdfPath = currentAgreement.getFilePath();
            File pdfFile = new File(pdfPath);

            if (!pdfFile.exists()) {
                throw new RuntimeException("PDF file not found at path: " + pdfPath);
            }

            // Load the PDF
            try (PDDocument document = PDDocument.load(pdfFile)) {
                // Extract text
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                return text; // This is the full text of the PDF
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeAllOldAgreements() {
        agreementRepo.deleteAllOldAgreements();
    }


    @Override
    public List<PartnerSignedAgreement> getSignedAgreementsByPartner(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Partner not found with id " + partnerId)
                );

        return partnerSignedAgreementRepo
                .findByPartnerOrderBySignedAtDesc(partner);

    }

    @Override
    public PartnerSignedAgreement getLatestSignedAgreementByPartner(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));

        return partnerSignedAgreementRepo.findTopByPartnerOrderBySignedAtDesc(partner)
                .orElseThrow(() -> new ResourceNotFoundException("No signed agreements found for partner " + partnerId));
    }

    @Override
    public PartnerSignedAgreement approveSignedAgreement(Integer signedAgreementId, Integer approverId) {
        PartnerSignedAgreement signedAgreement = partnerSignedAgreementRepo.findById(signedAgreementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Signed agreement not found with id " + signedAgreementId
                ));

        if (signedAgreement.getApprovalStatus() == AgreementApprovalStatus.APPROVED) {
            throw new IllegalStateException("Signed agreement already approved.");
        }

        // Fetch User entity
        User approver = userRepo.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id " + approverId));

        signedAgreement.setApprovalStatus(AgreementApprovalStatus.APPROVED);
        signedAgreement.setApprovedBy(approver); // pass User entity
        signedAgreement.setApprovedAt(LocalDateTime.now());
        signedAgreement.setRejectionReason(null);

        // Update Partner
        Partner partner = signedAgreement.getPartner();
        partner.setAgreedAgreement(signedAgreement.getAgreement());
        partner.setAgreementAccepted(true);
        partner.setAgreementSignedAt(signedAgreement.getSignedAt());
        partner.setAgreementStatus(PartnerAgreementStatus.APPROVED);

        partnerRepo.save(partner);

        return partnerSignedAgreementRepo.save(signedAgreement);
    }

    @Override
    public PartnerSignedAgreement rejectSignedAgreement(Integer signedAgreementId, Integer approverId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        PartnerSignedAgreement signedAgreement = partnerSignedAgreementRepo.findById(signedAgreementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Signed agreement not found with id " + signedAgreementId
                ));

        if (signedAgreement.getApprovalStatus() == AgreementApprovalStatus.APPROVED) {
            throw new IllegalStateException("Approved agreement cannot be rejected.");
        }

        // Fetch User entity
        User approver = userRepo.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id " + approverId));

        signedAgreement.setApprovalStatus(AgreementApprovalStatus.REJECTED);
        signedAgreement.setApprovedBy(approver); // pass User entity
        signedAgreement.setApprovedAt(LocalDateTime.now());
        signedAgreement.setRejectionReason(rejectionReason);

        // Update Partner
        Partner partner = signedAgreement.getPartner();
        partner.setAgreementAccepted(false);
        partner.setAgreementStatus(PartnerAgreementStatus.REJECTED);

        partnerRepo.save(partner);

        return partnerSignedAgreementRepo.save(signedAgreement);
    }

    @Override
    public void validateAgreementAccepted(Integer partnerId) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with id " + partnerId
                ));

        if (partner.getAgreedAgreement() == null) {
            throw new RuntimeException("Partner must accept the latest agreement before proceeding");
        }
    }

    @Override
    public PartnerAgreement getLatestAgreement() {
        return agreementRepo
                .findByIsLatestTrueAndStatus(PartnerAgreementStatus.REQUIRED)
                .orElseThrow(() -> new ResourceNotFoundException("No active agreement found"));

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

    private String storeFile(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) return null;

        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String ext = filename.contains(".")
                    ? filename.substring(filename.lastIndexOf("."))
                    : "";

            String storedFileName = timestamp + ext;

            // uploads/partner/{folderName}
            Path dir = ROOT_DIR.resolve(folderName);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path target = dir.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file in " + folderName, e);
        }
    }

    /**
     * Stores a partner's signed agreement file.
     *
     * Path:
     * uploads/partner/signed-agreements/{partnerId}/
     *
     * Filename:
     * signed_agreement_v{version}_{timestamp}.pdf
     */
    public String storeSignedAgreement(
            MultipartFile signedAgreement,
            Integer partnerId,
            String agreementVersion
    ) {
        if (signedAgreement == null || signedAgreement.isEmpty()) {
            throw new IllegalArgumentException("Signed agreement file is required");
        }

        try {
            // Extract extension
            String originalName = StringUtils.cleanPath(
                    Objects.requireNonNull(signedAgreement.getOriginalFilename())
            );

            String extension = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".pdf";

            // Timestamped file name
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "signed_agreement_v" + agreementVersion + "_" + timestamp + extension;

            // Directory: uploads/partner/signed-agreements/{partnerId}
            Path directory = ROOT_DIR.resolve("signed-agreements").resolve(String.valueOf(partnerId));
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // Save file
            Path targetPath = directory.resolve(fileName);
            Files.copy(
                    signedAgreement.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store signed agreement for partner " + partnerId, e
            );
        }
    }


    @Override
    public PartnerSignedAgreementDto mapToDto(PartnerSignedAgreement entity) {
        return PartnerSignedAgreementDto.builder()
                .id(entity.getId())
                .partnerId(entity.getPartner().getPartnerId())
                .agreementId(entity.getAgreement().getAgreementId())
                .agreementVersion(entity.getVersion())
                .filePath(entity.getFilePath())
                .signedAt(entity.getSignedAt())
                .approvalStatus(entity.getApprovalStatus())
                .approvedById(
                        entity.getApprovedBy() != null
                                ? entity.getApprovedBy().getUserId()
                                : null
                )
                .approvedAt(entity.getApprovedAt())
                .rejectionReason(entity.getRejectionReason())
                .build();
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
