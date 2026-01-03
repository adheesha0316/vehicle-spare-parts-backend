package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import com.spareparts.spareparts_backend.enums.PartnerAgreementStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerAgreementRepo extends JpaRepository<PartnerAgreement, Integer> {

    // Get latest agreement (by creation time)
    Optional<PartnerAgreement> findTopByOrderByCreatedAtDesc();

    Optional<PartnerAgreement> findByIsLatestTrueAndStatus(PartnerAgreementStatus status);

    // Find by version
    Optional<PartnerAgreement> findByVersion(String version);

    // List all agreements (admin view)
    List<PartnerAgreement> findAllByOrderByCreatedAtDesc();

    // Check existence
    boolean existsByVersion(String version);

    // 🔥 Custom method: mark all agreements as not latest
    @Modifying
    @Transactional
    @Query("UPDATE PartnerAgreement pa SET pa.isLatest = false WHERE pa.isLatest = true")
    void updateLatestFalse();

}
