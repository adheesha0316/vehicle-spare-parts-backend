package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.enums.SpareItemCategory;
import com.spareparts.spareparts_backend.enums.OwnershipStatus;
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
            SpareItemCategory category,
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

    List<SpareItem> findByCategoryAndBrandIgnoreCaseAndStatus(
            SpareItemCategory category,
            String brand,
            SpareItemStatus status
    );

    List<SpareItem> findByPriceBetweenAndStatus(
            Double minPrice,
            Double maxPrice,
            SpareItemStatus status
    );

    // ================= OWNERSHIP =================

    // All PLATFORM_OWNER items
    List<SpareItem> findByOwnershipAndStatusNot(
            OwnershipStatus ownership,
            SpareItemStatus status
    );

    // All PARTNER items (by partner)
    List<SpareItem> findByOwnershipAndPartner_PartnerIdAndStatusNot(
            OwnershipStatus ownership,
            Integer partnerId,
            SpareItemStatus status
    );

}
