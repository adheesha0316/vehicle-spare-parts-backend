package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Order;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Integer> {

    List<Order> findByCustomerCustomerIdOrderByCreatedAtDesc(Integer customerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByCustomerCustomerIdAndStatusIn(Integer customerId, List<OrderStatus> statuses);

}
