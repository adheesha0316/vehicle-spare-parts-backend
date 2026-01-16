package com.spareparts.spareparts_backend.security;

import com.spareparts.spareparts_backend.repo.PartnerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("partnerSecurity")
@RequiredArgsConstructor
public class PartnerSecurity {
    private final PartnerRepo partnerRepo;

    public boolean isOwner(Integer partnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();


        return partnerRepo.findById(partnerId)
                .map(partner -> partner.getUser().getUserId().equals(userDetails.getUserId()))
                .orElse(false);
    }
}
