package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.LoyaltyPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyPointsRepo extends JpaRepository<LoyaltyPoints, Integer> {
    Optional<LoyaltyPoints> findByCustomerId(Integer customerId);
}
