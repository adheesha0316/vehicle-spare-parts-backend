package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.PartnerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerDto {
    private Integer partnerId;
    private Integer userId;

    // Personal info
    private String fullName;
    private String phone;
    private String nicNumber;

    // Shop info
    private String shopName;
    private String shopAddress;
    private String branchName;

    // Images
    private String nicFrontImage;
    private String nicBackImage;
    private String profileImage;

    // Status & approval
    private PartnerStatus status;
    private Integer approvedByAdminId;

    // Agreement
    private Integer agreedAgreementId;
    private Boolean agreementAccepted;
    private LocalDateTime agreementSignedAt;
}
