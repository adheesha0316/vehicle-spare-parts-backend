package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.CustomerRequestDto;
import com.spareparts.spareparts_backend.dto.CustomerResponseDto;
import com.spareparts.spareparts_backend.entity.Customer;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.CustomerStatus;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.CustomerRepo;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.CustomerService;
import com.spareparts.spareparts_backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo customerRepo;
    private final UserService userService;

    // ================= CUSTOMER PROFILE =================


    @Override
    public CustomerResponseDto createCustomer(CustomerRequestDto requestDto, MultipartFile profileImage) {
        User currentUser = userService.getCurrentUserEntity();

        if (customerRepo.existsByUser_UserId(currentUser.getUserId())) {
            throw new IllegalStateException("Customer profile already exists");
        }

        String imagePath = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            imagePath = storeProfileImage(profileImage, null);
        }

        Customer customer = Customer.builder()
                .user(currentUser)
                .fullName(requestDto.getFullName())
                .nicNumber(requestDto.getNicNumber())
                .phone(requestDto.getPhone())
                .address(requestDto.getAddress())
                .profileImagePath(imagePath)
                .status(CustomerStatus.ACTIVE)
                .build();

        Customer savedCustomer = customerRepo.save(customer);

        return mapToResponse(savedCustomer);
    }

    @Override
    public CustomerResponseDto updateCustomerProfile(Integer customerId, CustomerRequestDto requestDto, MultipartFile profileImage) {
        Customer customer = getActiveCustomer(customerId);

        customer.setFullName(requestDto.getFullName());
        customer.setNicNumber(requestDto.getNicNumber());
        customer.setPhone(requestDto.getPhone());
        customer.setAddress(requestDto.getAddress());

        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = storeProfileImage(profileImage, customerId);
            customer.setProfileImagePath(imagePath);
        }

        return mapToResponse(customerRepo.save(customer));
    }


    // ================= GET PROFILE =================

    @Override
    public CustomerResponseDto getCustomerProfile(Integer customerId) {
        return mapToResponse(getActiveCustomer(customerId));
    }

    // ================= ADMIN : GET ALL =================
    @Override
    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= DELETE FLOW =================
    @Override
    public void requestDeleteCustomer(Integer customerId) {
        Customer customer = getActiveCustomer(customerId);
        customer.setStatus(CustomerStatus.DELETE_REQUESTED);
        customerRepo.save(customer);
    }

    @Override
    public void approveDeleteCustomer(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found")
                );

        customer.setStatus(CustomerStatus.DELETED);
        customerRepo.save(customer);
    }

    @Override
    public void rejectDeleteCustomer(Integer customerId, String reason) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found")
                );

        customer.setStatus(CustomerStatus.ACTIVE);
        customerRepo.save(customer);
    }

    // ================= VALIDATION =================
    @Override
    public void validateActiveCustomer(Integer customerId) {
        getActiveCustomer(customerId);
    }

    @Override
    public boolean existsByUserId(Integer userId) {
        return customerRepo.existsByUser_UserId(userId);
    }


    // ================= INTERNAL HELPERS =================
    private Customer getActiveCustomer(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer is not active");
        }
        return customer;
    }

    private String storeProfileImage(MultipartFile file, Integer customerId) {
        try {
            String folder = "uploads/customer/profileImg/";
            File directory = new File(folder);
            if (!directory.exists()) directory.mkdirs();

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) originalFilename = "profile_" + customerId + ".jpg";

            String filename = System.currentTimeMillis() + "_" + originalFilename.replaceAll("\\s+", "_");
            Path filePath = Paths.get(folder, filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store profile image", e);
        }
    }



    private CustomerResponseDto mapToResponse(Customer customer) {
        return CustomerResponseDto.builder()
                .customerId(customer.getCustomerId())
                .userId(customer.getUser() != null ? customer.getUser().getUserId() : null) // <-- link to User
                .fullName(customer.getFullName())
                .nicNumber(customer.getNicNumber())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .profileImagePath(customer.getProfileImagePath())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }


}
