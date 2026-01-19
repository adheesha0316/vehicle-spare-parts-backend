package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTransactionRepo extends JpaRepository<LoyaltyTransaction, Integer> {
    List<LoyaltyTransaction> findByLoyaltyPoints_PointIdOrderByCreatedAtDesc(Integer pointId);
}
