package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.PartnerAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerAgreementRepo extends JpaRepository<PartnerAgreement, Integer> {

    // Get latest agreement (by creation time)
    Optional<PartnerAgreement> findTopByOrderByCreatedAtDesc();

    // Find by version
    Optional<PartnerAgreement> findByVersion(String version);

    // List all agreements (admin view)
    List<PartnerAgreement> findAllByOrderByCreatedAtDesc();

    // Check existence
    boolean existsByVersion(String version);

}
