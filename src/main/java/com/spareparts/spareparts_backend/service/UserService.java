package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // Register user (returns DTO for API response)
    UserDtoReturn registerUser(User user);

    // ---------------- LOGIN ---------------- //
    LoginResponseDto loginUser(LoginRequestDto loginRequestDto);

    // Get user by ID as DTO
    Optional<UserDtoReturn> getUserById(Integer id);

    // Get all users as DTO
    List<UserDtoReturn> getAllUsers();

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

}
