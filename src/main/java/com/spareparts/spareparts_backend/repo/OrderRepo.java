package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Integer> {

    List<Order> findByCustomer_CustomerId(Integer customerId);

}
