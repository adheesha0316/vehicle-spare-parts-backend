package com.spareparts.spareparts_backend.security;

import com.spareparts.spareparts_backend.repo.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("customerSecurity")
@RequiredArgsConstructor
public class CustomerSecurity {

    private final CustomerRepo customerRepo;

    /**
     * Checks whether the logged-in user owns the given customerId
     */
    public boolean isOwner(Integer customerId) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName(); // JWT subject = email

        return customerRepo.existsByCustomerIdAndUser_Email(customerId, email);
    }
}
