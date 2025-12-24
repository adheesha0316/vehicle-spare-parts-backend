package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.UserDto;
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
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .role(userDto.getRole())
                .build();

        User registeredUser = userService.registerUser(user);

        return ResponseEntity.ok(registeredUser);
    }

    // ---------------- LOGIN ---------------- //
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDto.getEmail(),
                            userDto.getPassword()
                    )
            );

            User user = userService.loginUser(userDto.getEmail(), userDto.getPassword());
            String token = jwtTokenGenerator.generateToken(user);

            return ResponseEntity.ok().body(
                    new LoginResponse(user.getEmail(), user.getRole().name(), token)
            );

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    // ---------------- LOGIN RESPONSE DTO ---------------- //
    @lombok.Data
    @lombok.AllArgsConstructor
    static class LoginResponse {
        private String email;
        private String role;
        private String token;
    }
}
