package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepo extends JpaRepository<WalletTransaction, Long> {
    /**
     * Retrieves the full transaction history for a specific wallet.
     * Sorted by createdAt descending to show latest transactions first.
     */
    List<WalletTransaction> findByWallet_WalletIdOrderByCreatedAtDesc(Integer walletId);
}
