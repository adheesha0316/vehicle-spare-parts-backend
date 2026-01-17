package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDto;
import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.UserStatus;
import com.spareparts.spareparts_backend.exception.BadRequestException;
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
    public ResponseEntity<UserDtoReturn> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.registerUser(userDto));
    }


    // ---------------- LOGIN ---------------- //
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        // 2. Get User Entity
        User user = userService.getUserEntityByEmail(loginRequestDto.getEmail());

        // 3. Status Validation
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new BadRequestException("User account not approved by Admin");
        }

        // 4. Generate Token & Prepare Response
        LoginResponseDto loginResponse = userService.loginUser(loginRequestDto);
        String token = jwtTokenGenerator.generateToken(user);
        loginResponse.setToken(token);

        return ResponseEntity.ok(loginResponse);
    }

}


