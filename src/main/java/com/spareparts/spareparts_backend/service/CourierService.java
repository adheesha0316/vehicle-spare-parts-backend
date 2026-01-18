package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.CourierRegistrationDto;
import com.spareparts.spareparts_backend.dto.CourierResponseDto;

import java.util.List;

public interface CourierService {
    /**
     * Registers a new courier company and creates a corresponding User account.
     * @param registrationDto Details of the company and login credentials.
     * @return The created courier profile.
     */
    CourierResponseDto registerCourier(CourierRegistrationDto registrationDto);

    /**
     * Updates the verification status of a courier.
     * Only verified couriers are recommended to partners.
     * @param courierId ID of the courier.
     * @param isVerified Status to be set.
     */
    void verifyCourier(Integer courierId, boolean isVerified);

    /**
     * Smart Recommendation: Returns a list of couriers who have vehicles
     * capable of carrying the specific order items.
     * @param orderId ID of the order to be delivered.
     * @return List of suitable courier companies.
     */
    List<CourierResponseDto> getSuitableCouriersForOrder(Integer orderId);

    /**
     * Retrieves all active courier companies for admin purposes.
     * @return List of all couriers.
     */
    List<CourierResponseDto> getAllCouriers();

    /**
     * Retrieves details of a specific courier company.
     * @param courierId ID of the courier.
     * @return Courier profile details.
     */
    CourierResponseDto getCourierById(Integer courierId);

    /**
     * Toggle the active status of a courier (e.g., suspend service).
     * @param courierId ID of the courier.
     * @param isActive Status.
     */
    void updateActiveStatus(Integer courierId, boolean isActive);
}
