package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.LoginRequestDto;
import com.spareparts.spareparts_backend.dto.LoginResponseDto;
import com.spareparts.spareparts_backend.dto.UserDto;
import com.spareparts.spareparts_backend.dto.UserDtoReturn;
import com.spareparts.spareparts_backend.entity.Customer;
import com.spareparts.spareparts_backend.entity.Manager;
import com.spareparts.spareparts_backend.entity.Partner;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.*;
import com.spareparts.spareparts_backend.exception.EmailAlreadyExistsException;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.CustomerRepo;
import com.spareparts.spareparts_backend.repo.ManagerRepo;
import com.spareparts.spareparts_backend.repo.PartnerRepo;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PartnerRepo partnerRepo;
    private final ManagerRepo managerRepo;
    private final CustomerRepo customerRepo;
    private final PasswordEncoder passwordEncoder;
    private  final ModelMapper mapper;

    // ---------------- REGISTER USER ---------------- //
    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        User user = userRepo.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequestDto.getEmail()));

        // Password check
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        return LoginResponseDto.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .userName(user.getUsername())
                .status(user.getStatus().name())
                .build();
    }


    // ---------------- GET USER BY ID ---------------- //
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDtoReturn> getUserById(Integer id) {
        return userRepo.findById(id).map(user -> mapper.map(user, UserDtoReturn.class));
    }


    // ---------------- GET ALL USERS ---------------- //
    @Override
    @Transactional(readOnly = true)
    public Page<UserDtoReturn> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").descending());
        return userRepo.findAll(pageable)
                .map(user -> mapper.map(user, UserDtoReturn.class));
    }

    // ---------------- APPROVE USER ---------------- //
    @Override
    @Transactional
    public UserDtoReturn approveUser(Integer userId) {
        User user = getUserEntityById(userId);
        user.setStatus(UserStatus.APPROVED);
        return mapper.map(userRepo.save(user), UserDtoReturn.class);
    }

    // ---------------- DISAPPROVE USER ---------------- //
    @Override
    @Transactional
    public UserDtoReturn disapproveUser(Integer userId) {
        User user = getUserEntityById(userId);
        user.setStatus(UserStatus.REJECTED);
        return mapper.map(userRepo.save(user), UserDtoReturn.class);
    }

    // ---------------- CHANGE ROLE ---------------- //
    @Override
    @Transactional
    public UserDtoReturn changeUserRole(Integer userId, Role role) {
        User user = getUserEntityById(userId);
        user.setRole(role);

        switch (role) {
            case PARTNER -> {
                user.setStatus(UserStatus.PENDING_APPROVAL);
                if (!partnerRepo.existsByUser_UserId(userId)) {
                    partnerRepo.save(Partner.builder().user(user).status(PartnerStatus.PENDING_APPROVAL).build());
                }
            }
            case MANAGER -> {
                user.setStatus(UserStatus.PENDING_APPROVAL);
                if (!managerRepo.existsByUserUserId(userId)) {
                    managerRepo.save(Manager.builder().user(user).status(ManagerStatus.PENDING_APPROVAL).build());
                }
            }
            case CUSTOMER -> {
                user.setStatus(UserStatus.APPROVED);
                if (!customerRepo.existsByUser_UserId(userId)) {
                    customerRepo.save(Customer.builder().user(user).status(CustomerStatus.ACTIVE).build());
                }
            }
            default -> user.setStatus(UserStatus.APPROVED);
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
    @Transactional
    public User saveUser(User user) {
        return userRepo.save(user);
    }

}
