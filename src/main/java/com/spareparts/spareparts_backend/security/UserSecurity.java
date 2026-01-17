package com.spareparts.spareparts_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
    public boolean isSelf(Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();


        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId().equals(id);
        }

        return false;
    }
}
