package com.spareparts.spareparts_backend.dto;

import lombok.Data;


@Data
public class PartnerRequestDto {
    // Personal info
    private String fullName;
    private String phone;
    private String nicNumber;

    // Shop info
    private String shopName;
    private String shopAddress;
    private String branchName;

    // Agreement
    private Integer agreementId;        // latest agreement ID
    private Boolean agreementAccepted;  // must be true when signing
}
