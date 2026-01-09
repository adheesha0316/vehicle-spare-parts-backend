package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Customer;
import com.spareparts.spareparts_backend.enums.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Integer> {

    // Find customer by linked user id
    Optional<Customer> findByUser_UserId(Integer userId);

    // Check if customer exists for a user
    boolean existsByUser_UserId(Integer userId);

    // Find active customer by id
    Optional<Customer> findByCustomerIdAndStatus(
            Integer customerId,
            CustomerStatus status
    );

    // Get all customers by status (Admin)
    List<Customer> findByStatus(CustomerStatus status);

    // Exclude deleted customers
    List<Customer> findByStatusNot(CustomerStatus status);
}
