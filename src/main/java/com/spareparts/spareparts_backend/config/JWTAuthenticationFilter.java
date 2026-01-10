package com.spareparts.spareparts_backend.config;

import com.spareparts.spareparts_backend.entity.Customer;
import com.spareparts.spareparts_backend.entity.Manager;
import com.spareparts.spareparts_backend.entity.Partner;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.*;
import com.spareparts.spareparts_backend.repo.CustomerRepo;
import com.spareparts.spareparts_backend.repo.ManagerRepo;
import com.spareparts.spareparts_backend.repo.PartnerRepo;
import com.spareparts.spareparts_backend.service.Impl.CustomUserDetailsService;

import com.spareparts.spareparts_backend.service.UserService;
import com.spareparts.spareparts_backend.utill.JWTTokenGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTTokenGenerator jwtTokenGenerator;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final ManagerRepo managerRepo;
    private final PartnerRepo partnerRepo;
    private final CustomerRepo customerRepo;


    // -------- PUBLIC ENDPOINTS -------- //
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtTokenGenerator.validateToken(token)) {
            sendError(response, 401, "Invalid JWT token");
            return;
        }


        Integer userId;
        try {
            userId = jwtTokenGenerator.extractUserId(token);
        } catch (Exception e) {
            sendError(response, 401, "Invalid JWT token payload");
            return;
        }

        User user;
        try {
            user = userService.getUserEntityById(userId);
        } catch (Exception e) {
            sendError(response, 403, "User not found");
            return;
        }

        // ---------- USER APPROVAL ----------
        if (user.getStatus() != UserStatus.APPROVED) {
            sendError(response, 403, "User account not approved");
            return;
        }


        // ---------- ROLE-SPECIFIC VALIDATION ----------
        switch (user.getRole()) {
            case MANAGER -> {
                Manager manager = managerRepo.findByUserUserId(user.getUserId()).orElse(null);
                if (manager == null) {
                    sendError(response, 403, "Manager profile not found");
                    return;
                }
                if (manager.getStatus() == ManagerStatus.DELETED) {
                    sendError(response, 403, "Manager account deleted");
                    return;
                }
                if (manager.getStatus() == ManagerStatus.SUSPENDED) {
                    sendError(response, 403, "Manager account suspended");
                    return;
                }
                if (manager.getStatus() != ManagerStatus.APPROVED) {
                    sendError(response, 403, "Manager account not approved");
                    return;
                }
            }
            case PARTNER -> {
                Partner partner = partnerRepo.findByUser_UserId(user.getUserId()).orElse(null);
                if (partner == null) {
                    sendError(response, 403, "Partner profile not found");
                    return;
                }
                if (partner.getStatus() == PartnerStatus.DELETED) {
                    sendError(response, 403, "Partner account deleted");
                    return;
                }
                if (partner.getStatus() == PartnerStatus.SUSPENDED) {
                    sendError(response, 403, "Partner account suspended");
                    return;
                }
                if (partner.getStatus() != PartnerStatus.APPROVED) {
                    sendError(response, 403, "Partner account not approved");
                    return;
                }
            }
            case CUSTOMER -> {
                Customer customer = customerRepo.findByUser_UserId(user.getUserId()).orElse(null);
                String path = request.getServletPath();

                // Allow missing customer profile only for creation endpoint
                if (customer == null && !path.equals("/api/v1/customer/create")) {
                    sendError(response, 403, "Customer profile not found");
                    return;
                }

                if (customer != null) {
                    if (customer.getStatus() == CustomerStatus.DELETED) {
                        sendError(response, 403, "Customer account deleted");
                        return;
                    }
                    if (customer.getStatus() == CustomerStatus.SUSPENDED) {
                        sendError(response, 403, "Customer account suspended");
                        return;
                    }
                    if (customer.getStatus() != CustomerStatus.ACTIVE) {
                        sendError(response, 403, "Customer account not active");
                        return;
                    }
                }
            }
        }


        // ---------- AUTH SUCCESS ----------
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    // ---------- JSON ERROR ----------
    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        response.getWriter().write("""
            {
              "success": false,
              "status": %d,
              "message": "%s"
            }
            """.formatted(status, message));
    }
}
