package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.CourierRegistrationDto;
import com.spareparts.spareparts_backend.dto.CourierResponseDto;
import com.spareparts.spareparts_backend.dto.OrderResponseDto;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.service.CourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courier")
@RequiredArgsConstructor
@CrossOrigin
public class CourierController {

    private final CourierService courierService;

    // 1. REGISTER: Public endpoint for new courier companies to join
    @PostMapping("/register")
    public ResponseEntity<CourierResponseDto> register(@RequestBody CourierRegistrationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courierService.registerCourier(dto));
    }

    @GetMapping("/profile/me")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<CourierResponseDto> getMyProfile(Principal principal) {
        // Securely fetch profile using email from JWT token
        return ResponseEntity.ok(courierService.getCourierByUserId(null));
    }

    // 2. GET PROFILE BY COURIER ID: Retrieve specific company details
    @GetMapping("/profile/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CourierResponseDto> getProfile(@PathVariable Integer id) {
        return ResponseEntity.ok(courierService.getCourierById(id));
    }

    // 3. GET PROFILE BY USER ID: Helpful for Frontend to load profile after login
    @GetMapping("/profile/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<CourierResponseDto> getProfileByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(courierService.getCourierByUserId(userId));
    }

    // 4. UPDATE PROFILE: Updates details and triggers "isVerified = false" in Service
    @PutMapping("/profile/update/{id}")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<CourierResponseDto> updateProfile(
            @PathVariable Integer id,
            @RequestBody CourierRegistrationDto dto) {
        return ResponseEntity.ok(courierService.updateCourier(id, dto));
    }

    // ================= DELIVERY OPERATIONS =================
    /**
     * Get all currently active deliveries assigned to the logged-in courier.
     */
    @GetMapping("/my-active-deliveries")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<List<OrderResponseDto>> getActiveDeliveries(Principal principal) {
        return ResponseEntity.ok(courierService.getMyActiveDeliveries(principal.getName()));
    }

    /**
     * Get the history of completed deliveries for the logged-in courier.
     */
    @GetMapping("/my-delivery-history")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<List<OrderResponseDto>> getDeliveryHistory(Principal principal) {
        return ResponseEntity.ok(courierService.getMyDeliveryHistory(principal.getName()));
    }

    /**
     * Update order status (e.g., PICKED_UP, IN_TRANSIT, DELIVERED).
     */
    @PatchMapping("/update-status/{orderId}")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<String> updateDeliveryStatus(
            @PathVariable Integer orderId,
            @RequestParam OrderStatus status,
            Principal principal) {
        courierService.updateDeliveryStatus(orderId, status, principal.getName());
        return ResponseEntity.ok("Order status successfully updated to " + status);
    }



    // 5. DEACTIVATE REQUEST: Courier disables their own account (Pending Admin Deletion)
    @PatchMapping("/profile/request-deactivate/{id}")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<String> requestDeactivation(@PathVariable Integer id) {
        courierService.updateActiveStatus(id, false);
        return ResponseEntity.ok("Your account is now inactive. Admin has been notified for permanent deletion.");
    }

    // 6. RECOMMENDATION: Get suitable couriers based on Order Item Size
    // Usually called by the PARTNER when preparing for dispatch
    @GetMapping("/recommended-for-order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER', 'MANAGER')")
    public ResponseEntity<List<CourierResponseDto>> getRecommendedCouriers(@PathVariable Integer orderId) {
        return ResponseEntity.ok(courierService.getSuitableCouriersForOrder(orderId));
    }

    // 7. GET ALL ACTIVE: List all verified and active couriers
    @GetMapping("/active-list")
    public ResponseEntity<List<CourierResponseDto>> getActiveCouriers() {
        // You can filter this in service to only return isVerified=true & isActive=true
        return ResponseEntity.ok(courierService.getAllCouriers());
    }


}
