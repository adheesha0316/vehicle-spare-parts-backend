package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.SpareItem;
import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpareItemRepo extends JpaRepository<SpareItem, Integer> {

    // Find all spare items except DELETED
    List<SpareItem> findByStatusNot(SpareItemStatus status);

    // Find spare item by ID and not DELETED
    Optional<SpareItem> findBySpareItemIdAndStatusNot(Integer spareItemId, SpareItemStatus status);

    // Find by name containing for search
    List<SpareItem> findByNameContainingAndStatusNot(String name, SpareItemStatus status);

    // Find by category (active only)
    List<SpareItem> findByCategoryAndStatusNot(String category, SpareItemStatus status);

    // Find by stock status (e.g., IN_STOCK, OUT_OF_STOCK)
    List<SpareItem> findByStockStatusAndStatusNot(String stockStatus, SpareItemStatus status);

    // Find by category and stock status (active only)
    List<SpareItem> findByCategoryAndStockStatusAndStatusNot(String category, String stockStatus, SpareItemStatus status);

    // Find by price range
    List<SpareItem> findByPriceBetweenAndStatusNot(Double minPrice, Double maxPrice, SpareItemStatus status);
}
