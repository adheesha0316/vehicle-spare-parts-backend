package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.PartnerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PartnerResponseDto {
    private Integer partnerId;
    private Integer userId;

    // Personal info
    private String fullName;
    private String phone;

    // Shop info
    private String shopName;
    private String shopAddress;
    private String branchName;

    // Profile
    private String profileImage;

    // Status
    private PartnerStatus status;

    // Agreement info
    private String agreementVersion;
    private Boolean agreementAccepted;
    private LocalDateTime agreementSignedAt;

    // Audit
    private Integer approvedByAdminId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
