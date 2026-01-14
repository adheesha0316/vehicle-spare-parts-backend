package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Integer> {

    List<Review> findBySpareItem_SpareItemId(Integer spareItemId);

    List<Review> findByCustomer_CustomerId(Integer customerId);

    boolean existsByCustomer_CustomerIdAndSpareItem_SpareItemId(
            Integer customerId,
            Integer spareItemId
    );
}
