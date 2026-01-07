package com.spareparts.spareparts_backend.enums;

public enum PartnerAgreementStatus {
    PENDING,

    NOT_REQUIRED,        // No agreement exists yet (platform just started)

    REQUIRED,            // New agreement available – partner must agree

    SUBMITTED,           // Partner uploaded signed agreement, waiting for admin approval

    APPROVED,            // Admin approved the signed agreement

    REJECTED,            // Admin rejected the submitted agreement (re-upload required)

    EXPIRED              // Agreement invalid due to version update or policy change
}
