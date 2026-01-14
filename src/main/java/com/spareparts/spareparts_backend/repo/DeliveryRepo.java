package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepo extends JpaRepository<Delivery, Integer> {

    boolean existsByTrackingNumber(String trackingNumber);

    Delivery findByTrackingNumber(String trackingNumber);

}
