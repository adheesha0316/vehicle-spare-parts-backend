package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDto;
import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.Role;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // Register user (returns DTO for API response)
    UserDtoReturn registerUser(UserDto userDto);

    // ---------------- LOGIN ---------------- //
    LoginResponseDto loginUser(LoginRequestDto loginRequestDto);

    // Get user by ID as DTO
    Optional<UserDtoReturn> getUserById(Integer id);

    // Get all users as DTO
    Page<UserDtoReturn> getAllUsers(int page, int size);

    // Admin approves a user
    UserDtoReturn approveUser(Integer userId);

    // Admin disapproves a user
    UserDtoReturn disapproveUser(Integer userId);

    // Change user role (admin only)
    UserDtoReturn changeUserRole(Integer userId, Role role);

    // --- NEW METHOD ---
    // Get full User entity by email (for JWT token generation)
    User getUserEntityByEmail(String email);

    User getCurrentUserEntity();

    // UserService.java
    // Get full User entity by ID (for manager creation or approval checks)
    User getUserEntityById(Integer userId);

    // New method to check email
    boolean existsByEmail(String email);

    // Save or update a User entity (needed for role updates)
    User saveUser(User user);

}
