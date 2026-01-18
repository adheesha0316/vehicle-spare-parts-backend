package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Order;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Integer> {

    List<Order> findByCustomerCustomerIdOrderByCreatedAtDesc(Integer customerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByCustomerCustomerIdAndStatusIn(Integer customerId, List<OrderStatus> statuses);

    // 1. Fetch Orders containing Platform-owned items (where Partner is NULL)
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.spareItem.partner IS NULL")
    List<Order> findPlatformOrders();

    // 2. Fetch Orders containing items belonging to a specific Partner
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.spareItem.partner.partnerId = :partnerId")
    List<Order> findByPartnerId(@Param("partnerId") Integer partnerId);

    // 3. Optional: Find orders assigned to a specific Courier
    List<Order> findByCourierId(Integer courierId);


}
