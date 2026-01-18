package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.CourierCompany;
import com.spareparts.spareparts_backend.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CourierCompanyRepo extends JpaRepository<CourierCompany, Integer> {
    // ================= SECURITY & PROFILE =================
    Optional<CourierCompany> findByUserEmail(String email);

    Optional<CourierCompany> findByUserUserId(Integer userId);

    // ================= VALIDATION (Perfect for Registration) =================
    // Used to prevent multiple companies using the same Business Registration Number
    boolean existsByBusinessRegistrationNumber(String brNumber);

    // ================= SEARCH & FILTER =================
    List<CourierCompany> findByIsActiveTrue();

    List<CourierCompany> findByIsVerifiedTrueAndIsActiveTrue();

    // ================= SMART RECOMMENDATION =================
    @Query("SELECT DISTINCT c FROM CourierCompany c JOIN c.vehicleTypes v " +
            "WHERE v IN :requiredVehicles AND c.isActive = true AND c.isVerified = true")
    List<CourierCompany> findSuitableCouriers(@Param("requiredVehicles") Set<VehicleType> requiredVehicles);

    // ================= ANALYTICS (Optional but Perfect for Dashboards) =================
    // Find top-rated couriers for a specific service area
    List<CourierCompany> findByServiceAreaAndIsActiveTrueOrderByRatingDesc(String serviceArea);
}
