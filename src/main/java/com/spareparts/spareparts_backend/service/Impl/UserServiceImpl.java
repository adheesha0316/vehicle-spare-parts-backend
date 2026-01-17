package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDto;
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
import org.modelmapper.ModelMapper;
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
    private  final ModelMapper mapper;

    // ---------------- REGISTER USER ---------------- //
    @Override
    public UserDtoReturn registerUser(UserDto userDto) {
        if (userRepo.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists: " + userDto.getEmail());
        }

        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(userDto.getRole() != null ? userDto.getRole() : Role.CUSTOMER)
                .status(UserStatus.PENDING_APPROVAL)
                .build();

        User savedUser = userRepo.save(user);
        return mapper.map(savedUser, UserDtoReturn.class);
    }

    // ---------------- LOGIN USER ---------------- //
    @Override
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        User user = userRepo.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequestDto.getEmail()));

        return LoginResponseDto.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .userName(user.getUsername())
                .status(user.getStatus().name())
                .build();
    }


    // ---------------- GET USER BY ID ---------------- //
    @Override
    public Optional<UserDtoReturn> getUserById(Integer id) {
        return userRepo.findById(id).map(user -> mapper.map(user, UserDtoReturn.class));
    }


    // ---------------- GET ALL USERS ---------------- //
    @Override
    public List<UserDtoReturn> getAllUsers() {
        return userRepo.findAll().stream()
                .map(user -> mapper.map(user, UserDtoReturn.class))
                .collect(Collectors.toList());
    }

    // ---------------- APPROVE USER ---------------- //
    @Override
    public UserDtoReturn approveUser(Integer userId) {
        User user = getUserEntityById(userId);
        user.setStatus(UserStatus.APPROVED);
        return mapper.map(userRepo.save(user), UserDtoReturn.class);
    }

    // ---------------- DISAPPROVE USER ---------------- //
    @Override
    public UserDtoReturn disapproveUser(Integer userId) {
        User user = getUserEntityById(userId);
        user.setStatus(UserStatus.REJECTED);
        return mapper.map(userRepo.save(user), UserDtoReturn.class);
    }

    // ---------------- CHANGE ROLE ---------------- //
    @Override
    public UserDtoReturn changeUserRole(Integer userId, Role role) {
        User user = getUserEntityById(userId);
        user.setRole(role);

        if (role == Role.PARTNER || role == Role.MANAGER) {
            user.setStatus(UserStatus.PENDING_APPROVAL);
        } else {
            user.setStatus(UserStatus.APPROVED);
        }

        return mapper.map(userRepo.save(user), UserDtoReturn.class);
    }

    @Override
    public User getUserEntityByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public User getCurrentUserEntity() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserEntityByEmail(email);
    }

    @Override
    public User getUserEntityById(Integer userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    @Override
    public User saveUser(User user) {

        return userRepo.save(user);
    }

}
