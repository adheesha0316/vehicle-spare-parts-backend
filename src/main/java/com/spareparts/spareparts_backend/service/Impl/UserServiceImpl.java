package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.enums.UserStatus;
import com.spareparts.spareparts_backend.exception.EmailAlreadyExistsException;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    // ---------------- REGISTER USER ---------------- //
    @Override
    public UserDtoReturn registerUser(User user) {
        // Check if email already exists
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default role if null
        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        // Status is automatically set by @PrePersist in User entity
        User savedUser = userRepo.save(user);
        return toUserDtoReturn(savedUser);
    }

    // ---------------- LOGIN USER ---------------- //
    @Override
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        User user = userRepo.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getStatus() != UserStatus.APPROVED) {
            throw new RuntimeException("User is not approved yet");
        }

        return LoginResponseDto.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .userName(user.getUsername())
                .status(user.getStatus().name())
                .token(null) // JWT added in controller
                .build();
    }


    // ---------------- GET USER BY ID ---------------- //
    @Override
    public Optional<UserDtoReturn> getUserById(Integer id) {
        return userRepo.findById(id).map(this::toUserDtoReturn);
    }


    // ---------------- GET ALL USERS ---------------- //
    @Override
    public List<UserDtoReturn> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::toUserDtoReturn)
                .collect(Collectors.toList());
    }

    // ---------------- APPROVE USER ---------------- //
    @Override
    public UserDtoReturn approveUser(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.APPROVED);
        return toUserDtoReturn(userRepo.save(user));
    }

    // ---------------- DISAPPROVE USER ---------------- //
    @Override
    public UserDtoReturn disapproveUser(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.REJECTED);
        return toUserDtoReturn(userRepo.save(user));
    }

    // ---------------- CHANGE ROLE ---------------- //
    @Override
    public UserDtoReturn changeUserRole(Integer userId, Role role) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(role);

        // Update status depending on role
        if (role == Role.PARTNER || role == Role.MANAGER) {
            user.setStatus(UserStatus.PENDING_APPROVAL);
        } else {
            user.setStatus(UserStatus.APPROVED);
        }

        return toUserDtoReturn(userRepo.save(user));
    }



    @Override
    public User getUserEntityByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public User getCurrentUserEntity() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Logged-in user not found")
                );
    }

    @Override
    public User getUserEntityById(Integer userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    @Override
    public User saveUser(User user) {
        return userRepo.save(user);
    }

    // ---------------- HELPER: Convert User to DTO ---------------- //
    private UserDtoReturn toUserDtoReturn(User user) {
        return new UserDtoReturn(user.getEmail(), user.getStatus());
    }
}
