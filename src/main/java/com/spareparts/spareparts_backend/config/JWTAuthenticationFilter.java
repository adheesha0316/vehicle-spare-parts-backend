package com.spareparts.spareparts_backend.config;

import com.spareparts.spareparts_backend.entity.Manager;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.ManagerStatus;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.enums.UserStatus;
import com.spareparts.spareparts_backend.repo.ManagerRepo;
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

    @Autowired
    private final JWTTokenGenerator jwtTokenGenerator;

    @Autowired
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final ManagerRepo managerRepo;


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

        String email = jwtTokenGenerator.extractEmail(token);
        User user = userService.getUserEntityByEmail(email);

        // ---------- USER APPROVAL ----------
        if (user.getStatus() != UserStatus.APPROVED) {
            sendError(response, 403, "User account not approved");
            return;
        }

        // ---------- MANAGER CHECK ----------
        if (user.getRole() == Role.MANAGER) {

            Manager manager = managerRepo.findByUserUserId(user.getUserId())
                    .orElse(null);

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
                sendError(response, 403, "Manager not approved");
                return;
            }
        }

        // ---------- AUTH SUCCESS ----------
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

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
