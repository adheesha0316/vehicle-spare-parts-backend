package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.AgreementApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerSignedAgreementDto {
    private Integer id;
    private Integer partnerId;
    private Integer agreementId;
    private String agreementVersion;
    private String filePath;
    private LocalDateTime signedAt;
    private AgreementApprovalStatus approvalStatus;
    private Integer approvedById;
    private LocalDateTime approvedAt;
    private String rejectionReason;

}
