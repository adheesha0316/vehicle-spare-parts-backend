package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDto;
import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.UserStatus;
import com.spareparts.spareparts_backend.service.UserService;
import com.spareparts.spareparts_backend.utill.JWTTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final UserService userService;
    private final JWTTokenGenerator jwtTokenGenerator;
    private final AuthenticationManager authenticationManager;

    // ---------------- REGISTER ---------------- //
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {

        // Check if email already exists
        if (userService.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.status(409) // HTTP 409 Conflict
                    .body(Map.of("error", "Email already exists"));
        }

        // Register the user
        UserDtoReturn registeredUser = userService.registerUser(
                User.builder()
                        .username(userDto.getUsername())
                        .email(userDto.getEmail())
                        .password(userDto.getPassword())
                        .role(userDto.getRole())
                        .build()
        );

        return ResponseEntity.ok(registeredUser);
    }


    // ---------------- LOGIN ---------------- //
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            // Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(),
                            loginRequestDto.getPassword()
                    )
            );

            // Get full User entity
            User user = userService.getUserEntityByEmail(loginRequestDto.getEmail());

            // Check if approved
            if (user.getStatus() != UserStatus.APPROVED) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "User account not approved"));
            }

            // Normal login response
            LoginResponseDto loginResponse = userService.loginUser(loginRequestDto);
            String token = jwtTokenGenerator.generateToken(user);
            loginResponse.setToken(token);

            return ResponseEntity.ok(loginResponse);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

}


