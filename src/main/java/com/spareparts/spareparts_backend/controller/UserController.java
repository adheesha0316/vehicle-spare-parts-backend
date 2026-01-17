package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.security.CustomUserDetails;
import com.spareparts.spareparts_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    // ---------------- GET MY PROFILE ---------------- //
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDtoReturn> getMyProfile(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userService.getUserById(userDetails.getUserId())
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // ---------------- GET USER BY ID ---------------- //
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    public ResponseEntity<UserDtoReturn> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }


}
