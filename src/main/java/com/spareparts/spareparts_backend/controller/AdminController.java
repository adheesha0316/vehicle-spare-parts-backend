package com.spareparts.spareparts_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.*;
import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import com.spareparts.spareparts_backend.entity.PartnerSignedAgreement;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.security.CustomUserDetails;
import com.spareparts.spareparts_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin
public class AdminController {
    private final PartnerService partnerService;
    private final ManagerService managerService;
    private final CustomerService customerService;
    private final SpareItemService spareItemService;
    private final OrderService orderService;
    private final CourierService courierService;
    private final UserService userService;
    private final ObjectMapper mapper;


    //=============== User Controller ================

    // ---------------- GET ALL USERS (ADMIN) ---------------- //
    @GetMapping("/user/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDtoReturn>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size
    ) {
        Page<UserDtoReturn> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    // ---------------- APPROVE USER (ADMIN) ---------------- //
    @PutMapping("/user/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtoReturn> approveUser(@PathVariable Integer id) {
        UserDtoReturn user = userService.approveUser(id);
        return ResponseEntity.ok(user);
    }

    // ---------------- DISAPPROVE USER (ADMIN) ---------------- //
    @PutMapping("/user/disapprove/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtoReturn> disapproveUser(@PathVariable Integer id) {
        UserDtoReturn user = userService.disapproveUser(id);
        return ResponseEntity.ok(user);
    }

    // ---------------- CHANGE USER ROLE (ADMIN) ---------------- //
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtoReturn> changeUserRole(
            @PathVariable Integer id,
            @RequestParam Role role
    ) {
        UserDtoReturn updatedUser = userService.changeUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }


    //================= Partner Controller ==============
    // ================= APPROVE PARTNER =================
    @PutMapping("/partner/approve/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerResponseDto> approvePartner(
            @PathVariable Integer partnerId) {
        PartnerResponseDto response = partnerService.approvePartnerProfile(partnerId);
        return ResponseEntity.ok(response);
    }

    // ================= REJECT PARTNER =================
    @PutMapping("/partner/reject/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerResponseDto> rejectPartner(
            @PathVariable Integer partnerId,
            @RequestParam String reason) {
        PartnerResponseDto response = partnerService.rejectPartnerProfile(partnerId, reason);
        return ResponseEntity.ok(response);
    }


    // ================= ADMIN ACTIONS =================
    @DeleteMapping("/partner/delete/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePartnerByAdmin(@PathVariable Integer partnerId) {
        partnerService.deletePartnerByAdmin(partnerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/partner/restore/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restorePartnerByAdmin(@PathVariable Integer partnerId) {
        partnerService.restorePartnerByAdmin(partnerId);
        return ResponseEntity.ok().build();
    }

    // ================= GET PARTNER =================

    @GetMapping("/get/partner/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PartnerResponseDto> getPartnerByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(
                partnerService.getPartnerByUserId(userId)
        );
    }

    @GetMapping("/get/partner/{partnerId}")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public ResponseEntity<PartnerResponseDto> getPartnerById(@PathVariable Integer partnerId) {
        return ResponseEntity.ok(
                partnerService.getPartnerById(partnerId)
        );
    }

    @GetMapping("/partner/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PartnerResponseDto>> getAllPartners() {
        return ResponseEntity.ok(
                partnerService.getAllPartners()
        );
    }

    @GetMapping("/partner/{partnerId}/spare-items")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<SpareItemResponseDto>> getPartnerItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                spareItemService.findPartnerItems(partnerId)
        );
    }

    /**
     * ADMIN & MANAGER ONLY
     */
    @GetMapping("/{partnerId}/signed-agreements")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<PartnerSignedAgreementDto>> getSignedAgreementsByPartner(
            @PathVariable Integer partnerId
    ) {
        List<PartnerSignedAgreementDto> dtos = partnerService
                .getSignedAgreementsByPartner(partnerId) // returns List<PartnerSignedAgreement>
                .stream()
                .map(partnerService::mapToDto) // convert each entity to DTO
                .toList();

        return ResponseEntity.ok(dtos);
    }


    /**
     * ADMIN & MANAGER ONLY
     */
    @GetMapping("/{partnerId}/signed-agreements/latest")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PartnerSignedAgreement> getLatestSignedAgreementByPartner(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getLatestSignedAgreementByPartner(partnerId)
        );
    }



    // ================= ADMIN AGREEMENT =================
    @PostMapping("/agreement/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateAgreement(
            @RequestParam String conditions,
            @RequestParam String version
    ) {
        try {
            PartnerAgreement agreement =
                    partnerService.generateAgreementPdf(
                            "Common Partner",   // placeholder partner name
                            "Company Name",     // placeholder company name
                            conditions,
                            version,
                            true
                    );

            return ResponseEntity.ok(agreement);

        } catch (RuntimeException e) {

            // Version duplicate case
            if (e.getMessage().contains("version")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message", e.getMessage()
                        ));
            }

            // Other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Failed to generate agreement",
                            "error", e.getMessage()
                    ));
        }
    }


    // Get the current agreement's conditions
    @GetMapping("/agreement/current/conditions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public ResponseEntity<?> getCurrentConditions() {
        try {
            String conditions = partnerService.getCurrentAgreementConditions();
            return ResponseEntity.ok(Map.of(
                    "conditions", conditions
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message", e.getMessage()
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "message", "Failed to read agreement PDF",
                            "error", e.getMessage()
                    ));
        }
    }

    @PostMapping(value = "/admin/agreement/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerAgreement> uploadAgreement(
            @RequestPart MultipartFile agreementFile,
            @RequestParam String version
    ) {
        return ResponseEntity.ok(
                partnerService.uploadAgreement(agreementFile, version)
        );
    }

    @DeleteMapping("/admin/agreement/{agreementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeAgreement(@PathVariable Integer agreementId) {
        partnerService.removeAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/admin/agreement/upload-new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerAgreement> uploadNewAgreementVersion(
            @RequestPart MultipartFile agreementFile,
            @RequestParam String version
    ) {
        return ResponseEntity.ok(
                partnerService.uploadNewAgreementVersion(agreementFile, version)
        );
    }

    // ================= ADMIN / PARTNER AGREEMENT =================

    @GetMapping("/agreement/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public ResponseEntity<PartnerAgreement> getLatestAgreement() {
        try {
            PartnerAgreement latestAgreement = partnerService.getLatestAgreement();
            return ResponseEntity.ok(latestAgreement);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(null);
        }
    }

    // ================= APPROVAL =================

    @PutMapping("/partner/spare-items/approve-reject/{spareItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SpareItemResponseDto> approveOrRejectSpareItem(
            @PathVariable Integer spareItemId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String rejectionReason,
            Authentication authentication // Authentication object eka ganna
    ) {
        // 1. Get the email from the authentication object
        String email = authentication.getName();

        // 2. Fetch the actual User entity from your database using email
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer approverId = userDetails.getUserId();

        return ResponseEntity.ok(
                partnerService.approveOrRejectSpareItem(spareItemId, approve, rejectionReason, approverId)
        );
    }

    // ================= SPARE ITEM LISTS =================

    @GetMapping("/partners/spare-items/approved/{partnerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SpareItemResponseDto>> getPartnerApprovedSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getApprovedSpareItemsByPartner(partnerId)
        );
    }

    @GetMapping("/partners/spare-items/rejected/{partnerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SpareItemResponseDto>> getPartnerRejectedSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getRejectedSpareItemsByPartner(partnerId)
        );
    }

    @GetMapping("/partner/spare-items/pending/{partnerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SpareItemResponseDto>> getPartnerPendingSpareItems(
            @PathVariable Integer partnerId
    ) {
        return ResponseEntity.ok(
                partnerService.getPendingSpareItemsByPartner(partnerId)
        );
    }

    // ================= DELETE OLD AGREEMENTS =================
    @DeleteMapping("/agreement/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupOldAgreements() {
        partnerService.removeAllOldAgreements();
        return ResponseEntity.ok(
                Map.of("message", "Old agreement versions removed successfully")
        );
    }


    //=================== Manager Controllers ===================
    // ============================
    // DELETE MANAGER PROFILE (ADMIN)
    // ============================
    @DeleteMapping("/delete/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteManager(@PathVariable Integer managerId) {
        try {
            managerService.softDeleteManager(managerId);
            return ResponseEntity.ok("Manager deleted successfully");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // RESTORE MANAGER PROFILE (ADMIN)
    // ============================
    @PatchMapping("/restore/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restoreManager(@PathVariable Integer managerId) {
        try {
            managerService.restoreManagerProfile(managerId);
            return ResponseEntity.ok("Manager restored successfully");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // GET MANAGER BY ID (ADMIN)
    // ============================
    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManagerDto> getManagerById(@PathVariable Integer managerId) {
        try {
            ManagerDto manager = managerService.getManagerById(managerId);
            return ResponseEntity.ok(manager);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // GET ALL MANAGERS (ADMIN)
    // ============================
    @GetMapping("/manager/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManagerDto>> getAllManagers() {
        List<ManagerDto> managers = managerService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    // ============================
    // APPROVE MANAGER PROFILE (ADMIN)
    // ============================
    @PutMapping("/approve/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManagerDto> approveManager(@PathVariable Integer managerId) {
        try {
            ManagerDto approved = managerService.approveManagerProfile(managerId);
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // DOWNLOAD NIC IMAGES (ADMIN)
    // ============================
    @GetMapping("/downloadNIC/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadNICImages(@PathVariable Integer managerId) {
        try {
            Resource resource = managerService.downloadNICImagesAsZip(managerId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=NIC_Images.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ================= ADMIN : CUSTOMER MANAGEMENT =================
    //=================== Customer Controllers ===================

    @GetMapping("/customer/get/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CustomerResponseDto> getCustomerByIdForAdmin(
            @PathVariable Integer customerId
    ) {
        return ResponseEntity.ok(customerService.getCustomerProfile(customerId));
    }

    // ================= ADMIN : GET ALL =================
    @GetMapping("/customer/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }


    @PutMapping(value = "/customer/update/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponseDto> updateCustomerByAdmin(
            @PathVariable Integer customerId,
            @RequestPart("customer") String customerJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws Exception {

        CustomerRequestDto dto = mapper.readValue(customerJson, CustomerRequestDto.class);
        return ResponseEntity.ok(customerService.updateCustomerProfile(customerId, dto, profileImage));
    }

    // ================= DELETE FLOW =================

    // Admin approves deletion
    @PostMapping("/customer/delete-approve/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveDeleteCustomer(
            @PathVariable Integer customerId
    ) {
        customerService.approveDeleteCustomer(customerId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Delete request approved",
                "customerId", customerId
        ));
    }

    // Admin rejects deletion
    @PostMapping("/customer/delete-reject/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectDeleteCustomer(
            @PathVariable Integer customerId,
            @RequestBody(required = false) Map<String, String> request
    ) {
        // Safe defaulting
        String reason = request != null && request.get("reason") != null
                ? request.get("reason")
                : "Rejected by admin";

        customerService.rejectDeleteCustomer(customerId, reason);

        // Map.of is now SAFE because no null values
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Delete request rejected",
                "customerId", customerId,
                "reason", reason
        ));
    }

    //=================== SpareItem Controllers ===================
    // ---------------- CREATE ----------------
    // PLATFORM OWNER item (ADMIN / MANAGER)
    @PostMapping("/create/spare-items/platform")
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
    @PutMapping("/update/{spareItemId}")
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

    // ---------------- GET ALL FOR ADMIN ----------------
    @GetMapping("/spare-item/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SpareItemResponseDto>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size
    ) {
        Page<SpareItemResponseDto> response = spareItemService.getAllSpareItemsForAdmin(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spare-items/platform-owned")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SpareItemResponseDto>> getPlatformOwnedItems() {
        // Logic to return items where partnerId is NULL or specifically marked as Platform Items
        return ResponseEntity.ok(spareItemService.getPlatformItems());
    }

    //============== Order Controllers ==============
    @GetMapping("/orders/all")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<OrderResponseDto>> allOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateOrderStatusByAdmin(
            @PathVariable Integer orderId,
            @RequestParam OrderStatus status) {

        orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order status updated successfully by Admin"
        ));
    }

    //================= courier Controller ==============
    // 1. Get all couriers (Filtered by Admin to see who is pending/verified)
    @GetMapping("/courier/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourierResponseDto>> getAllCouriers() {
        return ResponseEntity.ok(courierService.getAllCouriers());
    }

    // get Courier's complete details
    @GetMapping("/couriers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourierResponseDto> getCourierProfileForAdmin(@PathVariable Integer id) {
        return ResponseEntity.ok(courierService.getCourierById(id));
    }

    // 2. APPROVE / REJECT Registration or Profile Update
    // status=true means Approved/Verified. status=false means Rejected/Unverified.
    @PatchMapping("/courier/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyCourier(@PathVariable Integer id, @RequestParam boolean status) {
        courierService.verifyCourier(id, status);
        String action = status ? "approved and verified" : "rejected/unverified";
        return ResponseEntity.ok("Courier has been " + action);
    }

    // 3. APPROVE DELETION (Hard Delete)
    // Use this when a courier requests deactivation and you want to remove them permanently
    @DeleteMapping("/courier/{id}/approve-deletion")
    public ResponseEntity<String> approveDeletion(@PathVariable Integer id) {
        courierService.deleteCourier(id);
        return ResponseEntity.ok("Courier account and all associated data deleted permanently.");
    }

    // 4. REJECT DELETION (Re-activate)
    // If a courier requested deactivation but you want to keep them active
    @PatchMapping("/courier/{id}/reject-deletion")
    public ResponseEntity<String> rejectDeletion(@PathVariable Integer id) {
        courierService.updateActiveStatus(id, true);
        return ResponseEntity.ok("Deletion request rejected. Courier account is now active again.");
    }

    // 5. MANUAL SUSPENSION
    // Admin can manually disable any courier at any time for policy violations
    @PatchMapping("/courier/{id}/active-status")
    public ResponseEntity<String> toggleActiveStatus(@PathVariable Integer id, @RequestParam boolean status) {
        courierService.updateActiveStatus(id, status);
        return ResponseEntity.ok("Courier active status set to: " + status);
    }

    // 6. RECOMMEND COURIERS FOR A SPECIFIC ORDER
    @GetMapping("/courier/recommend-for-order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<CourierResponseDto>> getRecommendedCouriers(@PathVariable Integer orderId) {
        return ResponseEntity.ok(courierService.getSuitableCouriersForOrder(orderId));
    }

}
