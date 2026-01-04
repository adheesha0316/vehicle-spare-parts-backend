package com.spareparts.spareparts_backend.service.Impl;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
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
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.spareparts.spareparts_backend.dto.PartnerRequestDto;
import com.spareparts.spareparts_backend.dto.PartnerResponseDto;
import com.spareparts.spareparts_backend.dto.SpareItemRequestDto;
import com.spareparts.spareparts_backend.dto.SpareItemResponseDto;
import com.spareparts.spareparts_backend.entity.Partner;
import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.*;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.PartnerAgreementRepo;
import com.spareparts.spareparts_backend.repo.PartnerRepo;
import com.spareparts.spareparts_backend.repo.SpareItemRepo;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.PartnerService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

import java.util.stream.Collectors;

@Service
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepo partnerRepo;
    private final PartnerAgreementRepo agreementRepo;
    private final SpareItemRepo spareItemRepo;
    private final UserRepo userRepo;
    private final PartnerAgreementRepo partnerAgreementRepo;

    private final Path ROOT_DIR = Paths.get("uploads/partner");

    @Autowired
    public PartnerServiceImpl(PartnerRepo partnerRepo, PartnerAgreementRepo agreementRepo, SpareItemRepo spareItemRepo, UserRepo userRepo, PartnerAgreementRepo partnerAgreementRepo) {
        this.partnerRepo = partnerRepo;
        this.agreementRepo = agreementRepo;
        this.spareItemRepo = spareItemRepo;
        this.userRepo = userRepo;
        this.partnerAgreementRepo = partnerAgreementRepo;
    }


    // ================= PARTNER PROFILE ================
    @Override
    public PartnerResponseDto createPartnerProfile(Integer userId, PartnerRequestDto requestDto, MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage) {
        // Fetch user or throw exception if not found
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

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
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));


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
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));


        partner.setStatus(PartnerStatus.PENDING); // pending deletion for admin approval
        partner.setUpdatedAt(LocalDateTime.now());
        partnerRepo.save(partner);

        return mapToDto(partner);
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
        try {
            // ================= Fetch Partner =================
            Partner partner = partnerRepo.findById(partnerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));

            // ================= Get latest active agreement =================
            PartnerAgreement latestAgreement = agreementRepo
                    .findByIsLatestTrueAndStatus(PartnerAgreementStatus.REQUIRED)
                    .orElseThrow(() -> new ResourceNotFoundException("No active agreement found"));

            // ================= Check agreement file exists =================
            Path filePath = Paths.get(latestAgreement.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Agreement file does not exist at path: " + latestAgreement.getFilePath());
            }

            // ================= Dynamically generate PDF =================
            // Using your existing generateAgreementPdf method, passing partner details
            PartnerAgreement generatedAgreement = generateAgreementPdf(
                    partner.getFullName(),         // Partner full name
                    partner.getShopName(),         // Shop name (company name equivalent)
                    latestAgreement.getConditions(), // Agreement conditions
                    latestAgreement.getVersion()     // Version
            );

            // ================= Return PDF bytes =================
            Path generatedFilePath = Paths.get(generatedAgreement.getFilePath());
            if (!Files.exists(generatedFilePath)) {
                throw new RuntimeException("Generated PDF file does not exist at path: " + generatedAgreement.getFilePath());
            }

            return Files.readAllBytes(generatedFilePath);

        } catch (ResourceNotFoundException e) {
            throw e; // propagate not found exceptions
        } catch (IOException e) {
            throw new RuntimeException("Error reading agreement PDF file", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download agreement", e);
        }
    }

    @Override
    public PartnerResponseDto acceptAgreement(Integer partnerId, MultipartFile signedAgreement) {

        // Validate file
        if (signedAgreement == null || signedAgreement.isEmpty()) {
            throw new IllegalArgumentException("Signed agreement file is required");
        }

        // Fetch Partner
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Partner not found with id " + partnerId)
                );

        // Prevent double signing
        if (partner.getAgreedAgreement() != null) {
            throw new IllegalStateException("Agreement already signed");
        }

        // Get ACTIVE latest agreement (NEVER null)
        PartnerAgreement latestAgreement =
                agreementRepo
                        .findByIsLatestTrueAndStatus(PartnerAgreementStatus.REQUIRED)
                        .orElseThrow(() -> new ResourceNotFoundException("No active agreement found"));


        // Save signed PDF
        String filePath = saveFile(
                partnerId,
                signedAgreement,
                "signed_agreement_" + partnerId + "_v" + latestAgreement.getVersion()
        );

        // Update Partner
        partner.setAgreedAgreement(latestAgreement);
        partner.setAgreementSignedAt(LocalDateTime.now());
        partner.setSignedAgreementPath(filePath);
        partner.setAgreementAccepted(true);

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

    @Override
    public SpareItemResponseDto createSpareItemRequest(Integer partnerId, SpareItemRequestDto requestDto, List<MultipartFile> images) {
        // Fetch partner or throw exception if not found
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id " + partnerId));

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
        // Fetch the spare item or throw exception if not found
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Spare item not found with id " + spareItemId));

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
        // Fetch the spare item or throw exception
        SpareItem item = spareItemRepo.findById(spareItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Spare item not found with id " + spareItemId));

        // Fetch the approver user or throw exception
        User approver = userRepo.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Approver not found with id " + approverId));

        // Update status based on approval
        if (approve) {
            item.setStatus(SpareItemStatus.APPROVED);
            item.setRejectionReason(null); // clear rejection reason if previously set
        } else {
            item.setStatus(SpareItemStatus.DELETED); // rejected
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
    public PartnerAgreement generateAgreementPdf(String partnerName, String companyName,String conditions, String version) {
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
