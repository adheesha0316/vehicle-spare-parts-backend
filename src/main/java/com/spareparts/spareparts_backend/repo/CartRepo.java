package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Cart;
import com.spareparts.spareparts_backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByCustomer(Customer customer);

    // Traverse the correct field in Customer
    Optional<Cart> findByCustomerCustomerId(Integer customerId);

    boolean existsByCustomerCustomerId(Integer customerId);


}
