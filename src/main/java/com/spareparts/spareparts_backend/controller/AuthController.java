package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDto;
import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.service.UserService;
import com.spareparts.spareparts_backend.utill.JWTTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserDtoReturn> registerUser(@RequestBody UserDto userDto) {
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
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(),
                            loginRequestDto.getPassword()
                    )
            );

            // Get safe login DTO (without JWT)
            LoginResponseDto loginResponse = userService.loginUser(loginRequestDto);

            // Get full User entity to generate JWT
            User user = userService.getUserEntityByEmail(loginRequestDto.getEmail());
            String token = jwtTokenGenerator.generateToken(user);

            // Set token
            loginResponse.setToken(token);

            return ResponseEntity.ok(loginResponse);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();
        }
    }

    // ---------------- LOGIN RESPONSE DTO ---------------- //
    @lombok.Data
    @lombok.AllArgsConstructor
    static class LoginResponse {
        private String email;
        private String status;  // Changed from role → status, matches UserDtoReturn
        private String token;
    }
}
