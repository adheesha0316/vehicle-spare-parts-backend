package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.*;
import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import com.spareparts.spareparts_backend.entity.PartnerSignedAgreement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PartnerService {
    // ================= PARTNER PROFILE =================

    PartnerResponseDto createPartnerProfile(Integer userId, PartnerRequestDto requestDto,
                                            MultipartFile nicFront,
                                            MultipartFile nicBack,
                                            MultipartFile profileImage);

    PartnerResponseDto requestProfileUpdate(Integer partnerId, PartnerRequestDto requestDto,
                                            MultipartFile nicFront,
                                            MultipartFile nicBack,
                                            MultipartFile profileImage);

    PartnerResponseDto requestProfileDelete(Integer partnerId);

    /**
     * ADMIN approves partner profile
     * Admin ID is obtained from the logged-in user context
     */
    PartnerResponseDto approvePartnerProfile(Integer partnerId);

    /**
     * ADMIN rejects partner profile
     * Admin ID is obtained from the logged-in user context
     */
    PartnerResponseDto rejectPartnerProfile(Integer partnerId, String rejectionReason);


    /**
     * Admin deletes partner profile permanently
     */
    void deletePartnerByAdmin(Integer partnerId);

    /**
     * Admin restores a previously deleted partner profile
     */
    void restorePartnerByAdmin(Integer partnerId);

    PartnerResponseDto getPartnerByUserId(Integer userId);

    PartnerResponseDto getPartnerById(Integer partnerId);

    List<PartnerResponseDto> getAllPartners();

    // ================= PARTNER AGREEMENT =================

    byte[] downloadAgreement(Integer partnerId);

    PartnerResponseDto acceptAgreement(Integer partnerId, MultipartFile signedAgreement);

    PartnerAgreement uploadAgreement(MultipartFile agreementFile, String version);

    void removeAgreement(Integer agreementId);

    PartnerAgreement uploadNewAgreementVersion(MultipartFile agreementFile, String version);

    // ================= SPARE ITEM REQUEST =================

    SpareItemResponseDto createSpareItemRequest(Integer partnerId, SpareItemRequestDto requestDto,
                                                List<MultipartFile> images);

    SpareItemResponseDto requestSpareItemUpdate(Integer partnerId, Integer spareItemId,
                                                SpareItemRequestDto requestDto,
                                                List<MultipartFile> images);

    SpareItemResponseDto requestSpareItemDelete(Integer partnerId, Integer spareItemId);

    /**
     * Approve or reject spare item request
     * Can be performed by ADMIN or MANAGER
     */
    SpareItemResponseDto approveOrRejectSpareItem(Integer spareItemId, boolean approve,
                                                  String rejectionReason, Integer approverId);

    // ================= SPARE ITEM GETTER =================

    List<SpareItemResponseDto> getApprovedSpareItemsByPartner(Integer partnerId);

    List<SpareItemResponseDto> getRejectedSpareItemsByPartner(Integer partnerId);

    List<SpareItemResponseDto> getPendingSpareItemsByPartner(Integer partnerId);


    // ================= Agreement =================
    PartnerAgreement generateAgreementPdf(String partnerName, String companyName,String conditions, String version, boolean saveToDb);

    String getCurrentAgreementConditions();

    void removeAllOldAgreements();

    // ================= PARTNER SIGNED AGREEMENTS =================


    /**
     * Get all signed agreements for a partner.
     */
    List<PartnerSignedAgreement> getSignedAgreementsByPartner(Integer partnerId);

    /**
     * Get the latest signed agreement for a partner.
     */
    PartnerSignedAgreement getLatestSignedAgreementByPartner(Integer partnerId);


    // ================= PARTNER SIGNED AGREEMENT APPROVAL =================

    /**
     * Approve a partner signed agreement
     * Can be performed by ADMIN or MANAGER
     */
    PartnerSignedAgreement approveSignedAgreement(
            Integer signedAgreementId,
            Integer approver
    );

    /**
     * Reject a partner signed agreement
     * Can be performed by ADMIN or MANAGER
     */
    PartnerSignedAgreement rejectSignedAgreement(
            Integer signedAgreementId,
            Integer approver,
            String rejectionReason
    );

    PartnerSignedAgreementDto mapToDto(PartnerSignedAgreement entity);


    // ================= IMPORTANT UTILS =================

    void validateAgreementAccepted(Integer partnerId);

    PartnerAgreement getLatestAgreement();
}
