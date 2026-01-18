package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.CourierRegistrationDto;
import com.spareparts.spareparts_backend.dto.CourierResponseDto;
import com.spareparts.spareparts_backend.dto.OrderResponseDto;
import com.spareparts.spareparts_backend.entity.CourierCompany;
import com.spareparts.spareparts_backend.entity.Order;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.ItemSize;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.enums.VehicleType;
import com.spareparts.spareparts_backend.exception.BadRequestException;
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
        if (userRepo.existsByEmail(registrationDto.getEmail())) {
            throw new BadRequestException("Email is already registered.");
        }
        if (courierRepo.existsByBusinessRegistrationNumber(registrationDto.getBusinessRegistrationNumber())) {
            throw new BadRequestException("Business Registration Number already exists.");
        }
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

        return mapToResponseDto(courierRepo.save(courier));
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
    public void updateDeliveryStatus(Integer orderId, OrderStatus status, String courierEmail) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        CourierCompany courier = courierRepo.findByUserEmail(courierEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Courier profile not found"));

        // 1. SECURITY: Ensure this order is actually assigned to the courier making the request
        if (order.getCourierId() == null || !order.getCourierId().equals(courier.getCourierId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized: This order is not assigned to you.");
        }

        // 2. PERFECT LOGIC: Validate status flow (Prevent skipping steps)
        validateStatusTransition(order.getStatus(), status);

        // 3. Update Order and Add Audit Log
        order.setStatus(status);

        // Assuming OrderStatusUpdate is linked via the order's list
        order.getStatusUpdates().add(
                com.spareparts.spareparts_backend.entity.OrderStatusUpdate.builder()
                        .order(order)
                        .status(status.name())
                        .timestamp(java.time.LocalDateTime.now())
                        .reason("Status updated by courier: " + courier.getCompanyName())
                        .build()
        );

        orderRepo.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyActiveDeliveries(String courierEmail) {
        CourierCompany courier = courierRepo.findByUserEmail(courierEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Courier profile not found"));

        // Fetch orders assigned to this courier that are NOT in final states
        return orderRepo.findByCourierId(courier.getCourierId()).stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED &&
                        o.getStatus() != OrderStatus.CANCELLED &&
                        o.getStatus() != OrderStatus.RETURNED)
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyDeliveryHistory(String courierEmail) {
        CourierCompany courier = courierRepo.findByUserEmail(courierEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Courier profile not found"));

        // Fetch completed or cancelled orders
        return orderRepo.findByCourierId(courier.getCourierId()).stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED ||
                        o.getStatus() == OrderStatus.CANCELLED)
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateCourierRating(Integer courierId, Double newRating) {
        CourierCompany courier = courierRepo.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        // In a perfect system, you'd calculate average: (old_rating + new_rating) / 2
        // For now, we update it directly
        courier.setRating(newRating);
        courierRepo.save(courier);
    }

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

    // ================= PRIVATE HELPERS =================

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean isValid = switch (current) {
            case COURIER_ASSIGNED -> (next == OrderStatus.PICKED_UP || next == OrderStatus.CANCELLED);
            case PICKED_UP -> (next == OrderStatus.IN_TRANSIT || next == OrderStatus.FAILED);
            case IN_TRANSIT -> (next == OrderStatus.OUT_FOR_DELIVERY || next == OrderStatus.FAILED);
            case OUT_FOR_DELIVERY -> (next == OrderStatus.DELIVERED || next == OrderStatus.FAILED);
            default -> true;
        };

        if (!isValid) {
            throw new BadRequestException("Invalid status move from " + current + " to " + next);
        }
    }

    private OrderResponseDto mapOrderToResponse(Order order) {
        // Use your existing Order mapping logic here to keep DTOs consistent
        return OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .customerName(order.getCustomer().getUser().getUsername())
                .orderStatus(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getCreatedAt())
                .build();
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
