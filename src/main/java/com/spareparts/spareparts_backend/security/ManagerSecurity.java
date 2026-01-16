package com.spareparts.spareparts_backend.security;

import com.spareparts.spareparts_backend.repo.ManagerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("managerSecurity")
@RequiredArgsConstructor
public class ManagerSecurity {
    private  final ManagerRepo managerRepo;

    public boolean isOwner(Integer managerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return managerRepo.findById(managerId)
                .map(manager -> manager.getUser().getUserId().equals(userDetails.getUserId()))
                .orElse(false);
    }
}
