package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.CourierRegistrationDto;
import com.spareparts.spareparts_backend.dto.CourierResponseDto;
import com.spareparts.spareparts_backend.entity.CourierCompany;
import com.spareparts.spareparts_backend.entity.Order;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.ItemSize;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.enums.VehicleType;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.CourierCompanyRepo;
import com.spareparts.spareparts_backend.repo.OrderRepo;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.CourierService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourierServiceImpl implements CourierService {

    private final CourierCompanyRepo courierRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CourierResponseDto registerCourier(CourierRegistrationDto registrationDto) {
        // 1. Create User account first with Role.COURIER
        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .role(Role.COURIER)
                .isActive(true) // Account is active, but company verification is false
                .build();

        User savedUser = userRepo.save(user);

        // 2. Create Courier Company Profile
        CourierCompany courier = CourierCompany.builder()
                .companyName(registrationDto.getCompanyName())
                .businessRegistrationNumber(registrationDto.getBusinessRegistrationNumber())
                .contactNumber(registrationDto.getContactNumber())
                .address(registrationDto.getAddress())
                .serviceArea(registrationDto.getServiceArea())
                .vehicleTypes(registrationDto.getVehicleTypes())
                .user(savedUser)
                .isVerified(false) // Needs Admin Approval
                .isActive(true)
                .rating(0.0)
                .build();

        CourierCompany savedCourier = courierRepo.save(courier);
        return mapToResponseDto(savedCourier);
    }

    @Override
    public CourierResponseDto updateCourier(Integer courierId, CourierRegistrationDto updateDto) {
        CourierCompany courier = courierRepo.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        // update profile
        courier.setCompanyName(updateDto.getCompanyName());
        courier.setContactNumber(updateDto.getContactNumber());
        courier.setAddress(updateDto.getAddress());
        courier.setServiceArea(updateDto.getServiceArea());
        courier.setVehicleTypes(updateDto.getVehicleTypes());

        User user = courier.getUser();
        if(updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            user.setEmail(updateDto.getEmail());
        }

        // encode new came password
        if(updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        userRepo.save(user);
        CourierCompany updatedCourier = courierRepo.save(courier);
        return mapToResponseDto(updatedCourier);
    }

    @Override
    public void deleteCourier(Integer courierId) {
        CourierCompany courier = courierRepo.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        // delete courier's user account
        Integer userId = courier.getUser().getUserId();

        courierRepo.delete(courier);
        userRepo.deleteById(userId);
    }

    @Override
    public void verifyCourier(Integer courierId, boolean isVerified) {
        CourierCompany courier = courierRepo.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        // Admin approval logic
        courier.setVerified(isVerified);
        courierRepo.save(courier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierResponseDto> getSuitableCouriersForOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Find the maximum item size in the order
        ItemSize maxItemSize = order.getOrderItems().stream()
                .map(item -> item.getSpareItem().getItemSize()) // මෙතන getSpareItem() විය යුතුයි
                .max(Comparator.naturalOrder())
                .orElse(ItemSize.SMALL);

        // Map ItemSize to required VehicleTypes
        Set<VehicleType> requiredVehicles = mapSizeToVehicles(maxItemSize);

        // Filter couriers who are Verified by Admin and have suitable vehicles
        return courierRepo.findSuitableCouriers(requiredVehicles).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierResponseDto> getAllCouriers() {
        return courierRepo.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourierResponseDto getCourierById(Integer courierId) {
        return courierRepo.findById(courierId)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));
    }

    @Override
    public void updateActiveStatus(Integer courierId, boolean isActive) {
        CourierCompany courier = courierRepo.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));
        courier.setActive(isActive);
        courierRepo.save(courier);
    }

    @Override
    public CourierResponseDto getCourierByUserId(Integer userId) {
        CourierCompany courier = courierRepo.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier profile not found for User ID: " + userId));

        return mapToResponseDto(courier);
    }

    // Helper: Map Item Size to suitable Vehicle Types
    private Set<VehicleType> mapSizeToVehicles(ItemSize size) {
        return switch (size) {
            case SMALL -> Set.of(VehicleType.BIKE, VehicleType.THREE_WHEELER, VehicleType.VAN, VehicleType.LORRY);
            case MEDIUM -> Set.of(VehicleType.THREE_WHEELER, VehicleType.VAN, VehicleType.LORRY);
            case LARGE -> Set.of(VehicleType.VAN, VehicleType.LORRY);
            case HEAVY -> Set.of(VehicleType.LORRY);
        };
    }

    private CourierResponseDto mapToResponseDto(CourierCompany courier) {
        return CourierResponseDto.builder()
                .courierId(courier.getCourierId())
                .userId(courier.getUser().getUserId())
                .companyName(courier.getCompanyName())
                .email(courier.getUser().getEmail())
                .contactNumber(courier.getContactNumber())
                .businessRegistrationNumber(courier.getBusinessRegistrationNumber())
                .address(courier.getAddress())
                .serviceArea(courier.getServiceArea())
                .vehicleTypes(courier.getVehicleTypes())
                .rating(courier.getRating())
                .isVerified(courier.isVerified())
                .isActive(courier.isActive())
                .createdAt(courier.getCreatedAt())
                .build();
    }
}
