package com.spareparts.spareparts_backend.dto;

import lombok.Data;

@Data
public class AgreementRequestDto {
    private String partnerName;   // Partner-specific name (optional for common agreement)
    private String companyName;   // Partner company name (optional for common agreement)
    private String conditions;    // Agreement conditions text
    private String version;       // Agreement version
}
