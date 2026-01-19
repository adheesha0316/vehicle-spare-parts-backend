package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepo extends JpaRepository<Wallet, Integer> {
    /**
     * Finds the wallet associated with a specific customer ID.
     * Path: Wallet -> Customer -> customerId
     */
    Optional<Wallet> findByCustomer_CustomerId(Integer customerId);
}
