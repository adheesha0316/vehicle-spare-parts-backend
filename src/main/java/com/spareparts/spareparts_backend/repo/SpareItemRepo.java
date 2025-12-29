package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import com.spareparts.spareparts_backend.enums.StockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpareItemRepo extends JpaRepository<SpareItem, Integer> {


    // ================= BASIC =================

    List<SpareItem> findByStatusNot(SpareItemStatus status);

    Optional<SpareItem> findBySpareItemIdAndStatusNot(
            Integer spareItemId,
            SpareItemStatus status
    );

    // ================= STATUS BASED =================

    // Used for:
    // - Customer view (APPROVED)
    // - Admin pending approval (PENDING)
    List<SpareItem> findByStatus(SpareItemStatus status);

    Optional<SpareItem> findBySpareItemIdAndStatus(
            Integer spareItemId,
            SpareItemStatus status
    );

    // ================= SEARCH / FILTER =================

    List<SpareItem> findByNameContainingIgnoreCaseAndStatus(
            String name,
            SpareItemStatus status
    );

    List<SpareItem> findByCategoryAndStatus(
            String category,
            SpareItemStatus status
    );

    List<SpareItem> findByBrandAndStatus(
            String brand,
            SpareItemStatus status
    );

    List<SpareItem> findByStockStatusAndStatus(
            StockStatus stockStatus,
            SpareItemStatus status
    );

    List<SpareItem> findByCategoryAndBrandAndStatus(
            String category,
            String brand,
            SpareItemStatus status
    );

    List<SpareItem> findByPriceBetweenAndStatus(
            Double minPrice,
            Double maxPrice,
            SpareItemStatus status
    );

    // ================= MANAGER =================

    List<SpareItem> findByManager_ManagerIdAndStatusNot(
            Integer managerId,
            SpareItemStatus status
    );
}
